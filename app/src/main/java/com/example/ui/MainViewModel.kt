package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseSeeder
import com.example.data.local.entity.MealPlanEntity
import com.example.data.local.entity.MealPlanItemEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.RestaurantEntity
import com.example.data.repository.RestaurantRepository
import com.example.data.repository.RestaurantWithMatch
import com.example.engine.BudgetMatchResult
import com.example.engine.CalculatedMealPlan
import com.example.engine.SmartBudgetEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max

enum class Screen {
    HOME,
    SEARCH_RESULTS,
    RESTAURANT_DETAIL,
    MEAL_BUILDER,
    COMPARE,
    MAP_VIEW,
    SAVED_PLANS,
    ADMIN_PORTAL
}

enum class SortOption {
    BEST_BUDGET_FIT,
    LOWEST_PRICE,
    SHORTEST_DISTANCE,
    HIGHEST_RATING
}

data class MealBuilderState(
    val restaurant: RestaurantEntity? = null,
    val selectedQuantities: Map<String, Int> = emptyMap(),
    val groupSize: Int = 6,
    val targetBudget: Double = 1500.0,
    val diningMode: String = "Dine-in"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = RestaurantRepository(db)
    val apiKeyManager = com.example.data.api.ApiKeyManager.getInstance(application)
    val placesService = com.example.data.api.GooglePlacesService(db)

    val showGoogleApiDialog = MutableStateFlow(false)

    // Current Active Screen
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation state history for BackHandler
    private val screenBackStack = mutableListOf<Screen>()

    // Filter & Budget Search Parameters
    val totalBudget = MutableStateFlow(1500.0)
    val groupSize = MutableStateFlow(6)
    val mealTiming = MutableStateFlow("LUNCH") // LUNCH or DINNER
    val dietaryPreference = MutableStateFlow("ALL") // ALL, VEG, NON_VEG
    val diningMode = MutableStateFlow("Dine-in") // Dine-in, Takeaway, Delivery
    val searchRadiusKm = MutableStateFlow(5.0) // 2.0, 5.0, 10.0, 15.0
    val selectedLocality = MutableStateFlow("All Vasai")
    val searchQuery = MutableStateFlow("")
    val selectedCuisine = MutableStateFlow<String?>(null)
    val outletTypeFilter = MutableStateFlow<String?>(null)
    val sortOption = MutableStateFlow(SortOption.BEST_BUDGET_FIT)

    // Reference Location (Vasai West Railway Station)
    val userLatitude = MutableStateFlow(19.3820)
    val userLongitude = MutableStateFlow(72.8315)

    // Selected Restaurant for Details / Builder
    private val _selectedRestaurantId = MutableStateFlow<String?>("vasai_rajeshwar_veg")
    val selectedRestaurantId: StateFlow<String?> = _selectedRestaurantId.asStateFlow()

    // Interactive Meal Builder State
    private val _mealBuilderState = MutableStateFlow(MealBuilderState())
    val mealBuilderState: StateFlow<MealBuilderState> = _mealBuilderState.asStateFlow()

    // Compare List (Up to 5 restaurants)
    private val _compareRestaurantIds = MutableStateFlow<Set<String>>(emptySet())
    val compareRestaurantIds: StateFlow<Set<String>> = _compareRestaurantIds.asStateFlow()

    // Success/Notification Toast Message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            DatabaseSeeder.seedIfEmpty(db)
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun handleBack(): Boolean {
        return if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeAt(screenBackStack.size - 1)
            true
        } else if (_currentScreen.value != Screen.HOME) {
            _currentScreen.value = Screen.HOME
            true
        } else {
            false
        }
    }

    fun selectRestaurant(id: String, openBuilder: Boolean = false) {
        _selectedRestaurantId.value = id
        viewModelScope.launch {
            val r = repository.getRestaurantDirect(id)
            if (openBuilder && r != null) {
                initMealBuilder(r)
                navigateTo(Screen.MEAL_BUILDER)
            } else {
                navigateTo(Screen.RESTAURANT_DETAIL)
            }
        }
    }

    fun initMealBuilder(restaurant: RestaurantEntity) {
        _mealBuilderState.value = MealBuilderState(
            restaurant = restaurant,
            selectedQuantities = emptyMap(),
            groupSize = groupSize.value,
            targetBudget = totalBudget.value,
            diningMode = diningMode.value
        )
    }

    fun loadPlanIntoBuilder(plan: MealPlanEntity, items: List<MealPlanItemEntity>, restaurant: RestaurantEntity) {
        val qtyMap = items.associate { it.menuItemId to it.quantity }
        _mealBuilderState.value = MealBuilderState(
            restaurant = restaurant,
            selectedQuantities = qtyMap,
            groupSize = plan.groupSize,
            targetBudget = plan.budget,
            diningMode = plan.diningMode
        )
        _selectedRestaurantId.value = restaurant.id
        navigateTo(Screen.MEAL_BUILDER)
    }

    fun updateMealItemQuantity(itemId: String, delta: Int) {
        val current = _mealBuilderState.value.selectedQuantities.toMutableMap()
        val currentQty = current[itemId] ?: 0
        val newQty = currentQty + delta
        if (newQty <= 0) {
            current.remove(itemId)
        } else {
            current[itemId] = minOf(30, newQty)
        }
        _mealBuilderState.value = _mealBuilderState.value.copy(selectedQuantities = current)
    }

    fun clearMealBuilder() {
        _mealBuilderState.value = _mealBuilderState.value.copy(selectedQuantities = emptyMap())
    }

    fun toggleCompare(restaurantId: String) {
        val current = _compareRestaurantIds.value.toMutableSet()
        if (current.contains(restaurantId)) {
            current.remove(restaurantId)
            showToast("Removed from comparison")
        } else {
            if (current.size >= 5) {
                showToast("Maximum 5 restaurants can be compared")
                return
            }
            current.add(restaurantId)
            showToast("Added to comparison (${current.size}/5)")
        }
        _compareRestaurantIds.value = current
    }

    fun saveCurrentMealPlan(name: String, allMenuItems: List<MenuItemEntity>) {
        val state = _mealBuilderState.value
        val r = state.restaurant ?: return
        viewModelScope.launch {
            val selectedItems = allMenuItems.filter { state.selectedQuantities.containsKey(it.id) }
            val itemEntities = selectedItems.map { item ->
                val qty = state.selectedQuantities[item.id] ?: 1
                MealPlanItemEntity(
                    mealPlanId = 0,
                    menuItemId = item.id,
                    menuItemName = item.name,
                    quantity = qty,
                    unitPriceSnapshot = item.price,
                    lineTotal = item.price * qty,
                    servesPeople = item.servesPeople * qty
                )
            }
            val subtotal = itemEntities.sumOf { it.lineTotal }
            val tax = subtotal * (r.gstPercentage / 100.0)
            val packaging = if (state.diningMode != "Dine-in") r.packagingFee else 0.0
            val delivery = if (state.diningMode == "Delivery") r.deliveryFee else 0.0
            val total = subtotal + tax + packaging + delivery
            val remaining = state.targetBudget - total

            val plan = MealPlanEntity(
                name = name.ifBlank { "${r.name} - Family of ${state.groupSize}" },
                restaurantId = r.id,
                restaurantName = r.name,
                groupSize = state.groupSize,
                budget = state.targetBudget,
                diningMode = state.diningMode,
                estimatedSubtotal = subtotal,
                taxAmount = tax,
                extraFees = packaging + delivery,
                estimatedTotal = total,
                remainingBudget = remaining
            )

            repository.saveMealPlan(plan, itemEntities)
            showToast("Meal Plan saved successfully!")
        }
    }
}
