package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.MealPlanEntity
import com.example.data.local.entity.MealPlanItemEntity
import com.example.data.local.entity.MenuCategoryEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.PriceHistoryEntity
import com.example.data.local.entity.RestaurantClaimEntity
import com.example.data.local.entity.RestaurantEntity
import com.example.data.local.entity.UserFavouriteEntity
import com.example.engine.BudgetMatchResult
import com.example.engine.SmartBudgetEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class RestaurantWithMatch(
    val restaurant: RestaurantEntity,
    val distanceKm: Double,
    val matchResult: BudgetMatchResult?,
    val isFavourite: Boolean = false
)

class RestaurantRepository(private val db: AppDatabase) {

    fun getAllRestaurants(): Flow<List<RestaurantEntity>> =
        db.restaurantDao().getAllRestaurants()

    fun getRestaurantById(id: String): Flow<RestaurantEntity?> =
        db.restaurantDao().getRestaurantById(id)

    suspend fun getRestaurantDirect(id: String): RestaurantEntity? =
        db.restaurantDao().getRestaurantDirect(id)

    fun getCategories(restaurantId: String): Flow<List<MenuCategoryEntity>> =
        db.menuDao().getCategoriesForRestaurant(restaurantId)

    fun getMenuItems(restaurantId: String): Flow<List<MenuItemEntity>> =
        db.menuDao().getMenuItemsForRestaurant(restaurantId)

    suspend fun getMenuItemsDirect(restaurantId: String): List<MenuItemEntity> =
        db.menuDao().getMenuItemsForRestaurantDirect(restaurantId)

    fun getAllFavourites(): Flow<List<UserFavouriteEntity>> =
        db.userFavouriteDao().getAllFavourites()

    fun isFavourite(restaurantId: String): Flow<Boolean> =
        db.userFavouriteDao().isFavourite(restaurantId)

    suspend fun toggleFavourite(restaurantId: String, currentFav: Boolean) = withContext(Dispatchers.IO) {
        if (currentFav) {
            db.userFavouriteDao().removeFavourite(restaurantId)
        } else {
            db.userFavouriteDao().insertFavourite(UserFavouriteEntity(restaurantId = restaurantId))
        }
    }

    fun getAllMealPlans(): Flow<List<MealPlanEntity>> =
        db.mealPlanDao().getAllMealPlans()

    suspend fun getItemsForPlan(planId: Long): List<MealPlanItemEntity> = withContext(Dispatchers.IO) {
        db.mealPlanDao().getItemsForPlan(planId)
    }

    suspend fun saveMealPlan(plan: MealPlanEntity, items: List<MealPlanItemEntity>): Long = withContext(Dispatchers.IO) {
        db.mealPlanDao().saveFullMealPlan(plan, items)
    }

    suspend fun deleteMealPlan(planId: Long) = withContext(Dispatchers.IO) {
        db.mealPlanDao().deleteMealPlan(planId)
        db.mealPlanDao().deleteItemsForPlan(planId)
    }

    // Admin & Owner Portal
    suspend fun addNewRestaurant(restaurant: RestaurantEntity) = withContext(Dispatchers.IO) {
        db.restaurantDao().insertRestaurant(restaurant)
    }

    suspend fun updateRestaurant(restaurant: RestaurantEntity) = withContext(Dispatchers.IO) {
        db.restaurantDao().updateRestaurant(restaurant)
    }

    suspend fun updateMenuItemPrice(
        itemId: String,
        dishName: String,
        previousPrice: Double,
        newPrice: Double,
        source: String
    ) = withContext(Dispatchers.IO) {
        val now = "2026-10-22"
        db.menuDao().updatePrice(itemId, newPrice, now)
        db.adminDao().insertPriceHistory(
            PriceHistoryEntity(
                menuItemId = itemId,
                dishName = dishName,
                previousPrice = previousPrice,
                newPrice = newPrice,
                source = source,
                verifiedAt = now
            )
        )
    }

    suspend fun updateDishAvailability(itemId: String, isAvailable: Boolean) = withContext(Dispatchers.IO) {
        db.menuDao().updateAvailability(itemId, isAvailable)
    }

    suspend fun submitRestaurantClaim(restaurantId: String, restaurantName: String, ownerName: String, phone: String) = withContext(Dispatchers.IO) {
        db.adminDao().insertClaim(
            RestaurantClaimEntity(
                restaurantId = restaurantId,
                restaurantName = restaurantName,
                ownerName = ownerName,
                ownerPhone = phone
            )
        )
    }

    fun getAllClaims(): Flow<List<RestaurantClaimEntity>> =
        db.adminDao().getAllClaims()

    fun getAllPriceHistory(): Flow<List<PriceHistoryEntity>> =
        db.adminDao().getAllPriceHistory()

    // Haversine formula for real Vasai coordinates
    fun calculateDistanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val dist = r * c
        return kotlin.math.round(dist * 10.0) / 10.0
    }
}
