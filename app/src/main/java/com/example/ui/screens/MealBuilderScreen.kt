package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MenuCategoryEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.RestaurantEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.DietaryBadge
import com.example.ui.components.QuickFilterChip
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.OnOrangeContainer
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarmOrange
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealBuilderScreen(
    viewModel: MainViewModel,
    categories: List<MenuCategoryEntity>,
    menuItems: List<MenuItemEntity>,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.handleBack()
    }

    val context = LocalContext.current
    val builderState by viewModel.mealBuilderState.collectAsState()
    val restaurant = builderState.restaurant

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var planNameInput by remember { mutableStateOf("") }

    if (restaurant == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No restaurant selected for meal building", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.navigateTo(Screen.SEARCH_RESULTS) }) {
                    Text("Select a Restaurant")
                }
            }
        }
        return
    }

    // Calculations
    val selectedItemEntities = menuItems.filter { builderState.selectedQuantities.containsKey(it.id) }
    val subtotal = selectedItemEntities.sumOf { item ->
        val qty = builderState.selectedQuantities[item.id] ?: 0
        item.price * qty
    }
    val taxAmount = subtotal * (restaurant.gstPercentage / 100.0)
    val packagingFee = if (builderState.diningMode != "Dine-in") restaurant.packagingFee else 0.0
    val deliveryFee = if (builderState.diningMode == "Delivery") restaurant.deliveryFee else 0.0
    val totalBill = subtotal + taxAmount + packagingFee + deliveryFee
    val perPersonCost = totalBill / max(1, builderState.groupSize)
    val remainingBudget = builderState.targetBudget - totalBill
    val totalServings = selectedItemEntities.sumOf { item ->
        val qty = builderState.selectedQuantities[item.id] ?: 0
        item.servesPeople * qty
    }

    val isOverBudget = totalBill > builderState.targetBudget
    val budgetProgress = (totalBill / builderState.targetBudget).coerceIn(0.0, 1.0).toFloat()

    // Smart suggestions: items that fit within remaining budget
    val affordableAdditions = if (remainingBudget >= 20.0) {
        menuItems.filter { !builderState.selectedQuantities.containsKey(it.id) && it.price * 1.05 <= remainingBudget }
            .sortedByDescending { it.price }
            .take(3)
    } else emptyList()

    // Cheaper substitutions if over budget
    val cheaperSubstitutions = if (isOverBudget && selectedItemEntities.isNotEmpty()) {
        val mostExpensiveInCart = selectedItemEntities.maxByOrNull { it.price }
        if (mostExpensiveInCart != null) {
            menuItems.filter { it.categoryId == mostExpensiveInCart.categoryId && it.price < mostExpensiveInCart.price }
        } else emptyList()
    } else emptyList()

    Column(modifier = modifier.fillMaxSize().testTag("meal_builder_screen")) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Smart Meal Builder",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${restaurant.name} • ${builderState.groupSize} Persons",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.handleBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = {
                    val shareText = buildString {
                        appendLine("🍽️ BudgetBites Vasai Meal Plan")
                        appendLine("Restaurant: ${restaurant.name}")
                        appendLine("Group: ${builderState.groupSize} people | Budget: ₹${builderState.targetBudget.toInt()}")
                        appendLine("--------------------------")
                        selectedItemEntities.forEach { item ->
                            val qty = builderState.selectedQuantities[item.id] ?: 0
                            appendLine("• ${qty}x ${item.name} = ₹${(item.price * qty).toInt()}")
                        }
                        appendLine("--------------------------")
                        appendLine("Subtotal: ₹${subtotal.toInt()}")
                        appendLine("GST (5%): ₹${taxAmount.toInt()}")
                        if (packagingFee > 0) appendLine("Packaging: ₹${packagingFee.toInt()}")
                        if (deliveryFee > 0) appendLine("Delivery: ₹${deliveryFee.toInt()}")
                        appendLine("Total Estimated: ₹${totalBill.toInt()} (₹${perPersonCost.toInt()}/person)")
                        if (remainingBudget >= 0) {
                            appendLine("Remaining In Budget: +₹${remainingBudget.toInt()}")
                        } else {
                            appendLine("Over Budget by: ₹${(-remainingBudget).toInt()}")
                        }
                    }
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Vasai Meal Plan"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share Plan")
                }
                IconButton(onClick = { viewModel.clearMealBuilder() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear All")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        // LIVE BUDGET TRACKER STICKY HEADER
        Surface(
            color = if (isOverBudget) Color(0xFFFFEBEE) else GreenContainer,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth().testTag("budget_tracker_header")
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isOverBudget) "BUDGET OVERRUN!" else "BUDGET HEALTH: ON TRACK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverBudget) AlertRed else ForestGreenPrimary
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${totalBill.toInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isOverBudget) AlertRed else ForestGreenPrimary
                            )
                            Text(
                                text = " / ₹${builderState.targetBudget.toInt()}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOverBudget) AlertRed else ForestGreenPrimary
                        ) {
                            Text(
                                text = if (isOverBudget) "-₹${(-remainingBudget).toInt()} OVER" else "+₹${remainingBudget.toInt()} LEFT",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹${perPersonCost.toInt()} / person",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { budgetProgress },
                    color = if (isOverBudget) AlertRed else ForestGreenPrimary,
                    trackColor = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Portion Adequacy Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val adequate = totalServings >= builderState.groupSize
                    Icon(
                        imageVector = if (adequate) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (adequate) ForestGreenPrimary else WarmOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (adequate) "Provides ~$totalServings servings for group of ${builderState.groupSize} (Adequate)"
                        else "Provides ~$totalServings servings (Add more food for ${builderState.groupSize} people)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Suggestions / Warning Alert Cards
        if (isOverBudget) {
            Surface(
                color = AlertRed.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Total exceeds your ₹${builderState.targetBudget.toInt()} budget. Decrease quantities or consider cheaper substitutes.",
                        fontSize = 11.sp,
                        color = AlertRed
                    )
                }
            }
        } else if (affordableAdditions.isNotEmpty()) {
            Surface(
                color = OrangeContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "₹${remainingBudget.toInt()} left! You can add ${affordableAdditions.first().name} (₹${affordableAdditions.first().price.toInt()})",
                        fontSize = 11.sp,
                        color = OnOrangeContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickFilterChip(
                text = "All Dishes",
                isSelected = selectedCategoryId == null,
                onClick = { selectedCategoryId = null }
            )
            categories.forEach { cat ->
                QuickFilterChip(
                    text = cat.name,
                    isSelected = selectedCategoryId == cat.id,
                    onClick = { selectedCategoryId = cat.id }
                )
            }
        }

        // Menu Items List with Stepper (+/-)
        val filteredItems = if (selectedCategoryId == null) {
            menuItems
        } else {
            menuItems.filter { it.categoryId == selectedCategoryId }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredItems) { item ->
                val qty = builderState.selectedQuantities[item.id] ?: 0
                val inCart = qty > 0

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (inCart) GreenContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier.fillMaxWidth().testTag("builder_item_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DietaryBadge(dietaryType = item.dietaryType)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${item.price.toInt()} • ${item.servingSize}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreenPrimary
                            )
                            if (item.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Stepper (- / Qty / +)
                        if (inCart) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ForestGreenPrimary,
                                modifier = Modifier.border(1.dp, ForestGreenDark, RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateMealItemQuantity(item.id, -1) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = "$qty",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateMealItemQuantity(item.id, 1) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.updateMealItemQuantity(item.id, 1) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp).testTag("add_item_btn_${item.id}")
                            ) {
                                Text("Add +", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForestGreenPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Sticky Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total Bill: ₹${totalBill.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (isOverBudget) AlertRed else ForestGreenPrimary
                    )
                    Text(
                        text = "Subtotal ₹${subtotal.toInt()} + GST ₹${taxAmount.toInt()}" +
                                if (packagingFee > 0) " + Pack ₹${packagingFee.toInt()}" else "",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        planNameInput = "${restaurant.name} - ${builderState.groupSize} People"
                        showSaveDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isOverBudget) WarmOrange else ForestGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(46.dp).testTag("save_meal_plan_button")
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Save Meal Plan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Save Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Meal Plan") },
            text = {
                Column {
                    Text(
                        text = "Enter a name for this ${builderState.groupSize}-person meal plan at ${restaurant.name} (Total: ₹${totalBill.toInt()}):",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = planNameInput,
                        onValueChange = { planNameInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("save_plan_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentMealPlan(planNameInput, menuItems)
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                ) {
                    Text("Save to Favorites")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
