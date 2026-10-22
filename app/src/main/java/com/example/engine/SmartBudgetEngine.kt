package com.example.engine

import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.RestaurantEntity
import kotlin.math.ceil
import kotlin.math.max

data class CalculatedMealPlan(
    val title: String,
    val subtitle: String,
    val items: List<CalculatedItem>,
    val subtotal: Double,
    val taxAmount: Double,
    val packagingFee: Double,
    val deliveryFee: Double,
    val totalEstimatedBill: Double,
    val perPersonCost: Double,
    val remainingBudget: Double,
    val totalServings: Int,
    val isWithinBudget: Boolean,
    val tag: String // "BEST_VALUE", "FAMILY_FAVORITE", "MAX_VARIETY", "FEAST"
)

data class CalculatedItem(
    val menuItem: MenuItemEntity,
    val quantity: Int,
    val lineTotal: Double,
    val servesPeople: Int
)

sealed class BudgetMatchResult {
    data class Success(
        val restaurant: RestaurantEntity,
        val recommendedCombos: List<CalculatedMealPlan>,
        val bestFit: CalculatedMealPlan
    ) : BudgetMatchResult()

    data class BudgetInsufficient(
        val restaurant: RestaurantEntity,
        val minimumTotal: Double,
        val shortfall: Double,
        val explanation: String
    ) : BudgetMatchResult()

    data class NoMatchingDishes(
        val restaurant: RestaurantEntity,
        val reason: String
    ) : BudgetMatchResult()
}

object SmartBudgetEngine {

    fun calculateMatchesForRestaurant(
        restaurant: RestaurantEntity,
        menuItems: List<MenuItemEntity>,
        groupSize: Int,
        totalBudget: Double,
        dietaryPreference: String, // "ALL", "VEG", "NON_VEG"
        diningMode: String // "Dine-in", "Takeaway", "Delivery"
    ): BudgetMatchResult {
        // Filter active items matching dietary preference
        val availableItems = menuItems.filter { it.isAvailable }
        val filteredItems = when (dietaryPreference) {
            "VEG" -> availableItems.filter { it.dietaryType == "VEG" }
            "NON_VEG" -> availableItems // Can include non-veg + veg staples
            else -> availableItems
        }

        if (filteredItems.isEmpty()) {
            return BudgetMatchResult.NoMatchingDishes(
                restaurant = restaurant,
                reason = "No available dishes matching $dietaryPreference preference."
            )
        }

        // Categorize items
        val thaliItems = filteredItems.filter {
            it.name.contains("Thali", ignoreCase = true) ||
            it.servingSize.contains("Thali", ignoreCase = true) ||
            it.servingSize.contains("1 person", ignoreCase = true) && it.price > 100
        }
        val curries = filteredItems.filter {
            it.name.contains("Masala", ignoreCase = true) ||
            it.name.contains("Handi", ignoreCase = true) ||
            it.name.contains("Curry", ignoreCase = true) ||
            it.name.contains("Kadai", ignoreCase = true) ||
            it.name.contains("Tadka", ignoreCase = true) ||
            it.name.contains("Fry", ignoreCase = true)
        }
        val breads = filteredItems.filter {
            it.name.contains("Roti", ignoreCase = true) ||
            it.name.contains("Bhakri", ignoreCase = true) ||
            it.name.contains("Chapati", ignoreCase = true) ||
            it.name.contains("Naan", ignoreCase = true) ||
            it.name.contains("Pav", ignoreCase = true)
        }
        val rices = filteredItems.filter {
            it.name.contains("Rice", ignoreCase = true) ||
            it.name.contains("Biryani", ignoreCase = true) ||
            it.name.contains("Pulao", ignoreCase = true) ||
            it.name.contains("Khichdi", ignoreCase = true)
        }
        val drinksAndSweets = filteredItems.filter {
            it.name.contains("Chaas", ignoreCase = true) ||
            it.name.contains("Jamun", ignoreCase = true) ||
            it.name.contains("Solkadhi", ignoreCase = true) ||
            it.name.contains("Lassi", ignoreCase = true)
        }

        val generatedCombos = mutableListOf<CalculatedMealPlan>()

        // 1. Individual Thali Feast Strategy (Exact group coverage)
        if (thaliItems.isNotEmpty()) {
            val primaryThali = thaliItems.minByOrNull { it.price } ?: thaliItems.first()
            val comboItems = mutableListOf<CalculatedItem>()
            comboItems.add(
                CalculatedItem(
                    menuItem = primaryThali,
                    quantity = groupSize,
                    lineTotal = primaryThali.price * groupSize,
                    servesPeople = groupSize
                )
            )

            // If budget remains, add complementary beverage or sweet
            val currentSubtotal = primaryThali.price * groupSize
            var tempSub = currentSubtotal
            drinksAndSweets.firstOrNull()?.let { beverage ->
                if ((tempSub + beverage.price * groupSize) * 1.05 <= totalBudget) {
                    comboItems.add(
                        CalculatedItem(
                            menuItem = beverage,
                            quantity = groupSize,
                            lineTotal = beverage.price * groupSize,
                            servesPeople = groupSize
                        )
                    )
                    tempSub += beverage.price * groupSize
                }
            }

            val plan = buildMealPlan(
                title = "Individual Thali Feast",
                subtitle = "$groupSize x ${primaryThali.name}",
                items = comboItems,
                restaurant = restaurant,
                groupSize = groupSize,
                totalBudget = totalBudget,
                diningMode = diningMode,
                tag = "FAMILY_FAVORITE"
            )
            generatedCombos.add(plan)
        }

        // 2. Shared Handi Curry & Roti Banquet Strategy
        if (curries.isNotEmpty() && breads.isNotEmpty()) {
            val comboItems = mutableListOf<CalculatedItem>()
            // Need enough curry servings for groupSize
            // Typically 2-3 people per handi
            val neededCurries = max(2, ceil(groupSize / 2.2).toInt())
            val sortedCurries = curries.sortedBy { it.price }
            val mainCurry = sortedCurries.first()
            val secondCurry = sortedCurries.getOrNull(1) ?: mainCurry

            val halfCurries = neededCurries / 2
            val otherHalf = neededCurries - halfCurries

            comboItems.add(
                CalculatedItem(
                    menuItem = mainCurry,
                    quantity = otherHalf,
                    lineTotal = mainCurry.price * otherHalf,
                    servesPeople = otherHalf * mainCurry.servesPeople
                )
            )
            if (secondCurry.id != mainCurry.id || halfCurries > 0) {
                comboItems.add(
                    CalculatedItem(
                        menuItem = secondCurry,
                        quantity = max(1, halfCurries),
                        lineTotal = secondCurry.price * max(1, halfCurries),
                        servesPeople = max(1, halfCurries) * secondCurry.servesPeople
                    )
                )
            }

            // Rotis: ~2.5 rotis per person
            val bread = breads.minByOrNull { it.price } ?: breads.first()
            val breadQty = if (bread.name.contains("Basket", ignoreCase = true)) {
                max(2, ceil(groupSize / 3.0).toInt())
            } else {
                groupSize * 2 + 2 // e.g. 14 rotis for 6 people
            }
            comboItems.add(
                CalculatedItem(
                    menuItem = bread,
                    quantity = breadQty,
                    lineTotal = bread.price * breadQty,
                    servesPeople = groupSize
                )
            )

            // Rice portion for group
            if (rices.isNotEmpty()) {
                val rice = rices.minByOrNull { it.price } ?: rices.first()
                val riceQty = max(1, ceil(groupSize / 2.5).toInt()) // 2 portions for 6 people
                comboItems.add(
                    CalculatedItem(
                        menuItem = rice,
                        quantity = riceQty,
                        lineTotal = rice.price * riceQty,
                        servesPeople = riceQty * rice.servesPeople
                    )
                )
            }

            val plan = buildMealPlan(
                title = "Family Curry & Roti Banquet",
                subtitle = "Shared Handi curries, tawa breads & fragrant rice",
                items = comboItems,
                restaurant = restaurant,
                groupSize = groupSize,
                totalBudget = totalBudget,
                diningMode = diningMode,
                tag = "BEST_VALUE"
            )
            generatedCombos.add(plan)
        }

        // 3. Biryani & Dum Pulao Combo Strategy
        val biryanis = rices.filter { it.name.contains("Biryani", ignoreCase = true) || it.name.contains("Pulao", ignoreCase = true) }
        if (biryanis.isNotEmpty()) {
            val biryani = biryanis.first()
            val neededPots = max(1, ceil(groupSize.toDouble() / biryani.servesPeople).toInt())
            val comboItems = mutableListOf<CalculatedItem>()
            comboItems.add(
                CalculatedItem(
                    menuItem = biryani,
                    quantity = neededPots,
                    lineTotal = biryani.price * neededPots,
                    servesPeople = neededPots * biryani.servesPeople
                )
            )

            // Complement with starter or gravy or drinks
            curries.firstOrNull()?.let { sideCurry ->
                comboItems.add(
                    CalculatedItem(
                        menuItem = sideCurry,
                        quantity = 1,
                        lineTotal = sideCurry.price,
                        servesPeople = sideCurry.servesPeople
                    )
                )
            }

            val plan = buildMealPlan(
                title = "Dum Biryani & Gravy Feast",
                subtitle = "$neededPots x ${biryani.name} with accompaniments",
                items = comboItems,
                restaurant = restaurant,
                groupSize = groupSize,
                totalBudget = totalBudget,
                diningMode = diningMode,
                tag = "FEAST"
            )
            generatedCombos.add(plan)
        }

        // If no pre-built strategy worked, bounded integer knapsack fallback
        if (generatedCombos.isEmpty()) {
            val comboItems = mutableListOf<CalculatedItem>()
            var currentServings = 0
            val sortedByValue = filteredItems.sortedBy { it.price / max(1, it.servesPeople) }
            for (item in sortedByValue) {
                if (currentServings >= groupSize) break
                val needed = ceil((groupSize - currentServings).toDouble() / max(1, item.servesPeople)).toInt()
                val qty = max(1, minOf(needed, item.maximumQuantity))
                comboItems.add(
                    CalculatedItem(
                        menuItem = item,
                        quantity = qty,
                        lineTotal = item.price * qty,
                        servesPeople = qty * item.servesPeople
                    )
                )
                currentServings += qty * item.servesPeople
            }

            if (comboItems.isNotEmpty()) {
                val plan = buildMealPlan(
                    title = "Economy Group Selection",
                    subtitle = "Optimized dishes to satisfy group size",
                    items = comboItems,
                    restaurant = restaurant,
                    groupSize = groupSize,
                    totalBudget = totalBudget,
                    diningMode = diningMode,
                    tag = "BEST_VALUE"
                )
                generatedCombos.add(plan)
            }
        }

        if (generatedCombos.isEmpty()) {
            return BudgetMatchResult.NoMatchingDishes(
                restaurant = restaurant,
                reason = "Unable to compute a valid group meal combination."
            )
        }

        // Check if any combo is within budget
        val withinBudgetCombos = generatedCombos.filter { it.isWithinBudget }
        if (withinBudgetCombos.isNotEmpty()) {
            // Sort by remaining budget closeness or best balance
            val bestFit = withinBudgetCombos.minByOrNull { kotlin.math.abs(it.remainingBudget) } ?: withinBudgetCombos.first()
            return BudgetMatchResult.Success(
                restaurant = restaurant,
                recommendedCombos = withinBudgetCombos,
                bestFit = bestFit
            )
        } else {
            val cheapestCombo = generatedCombos.minByOrNull { it.totalEstimatedBill } ?: generatedCombos.first()
            val shortfall = cheapestCombo.totalEstimatedBill - totalBudget
            return BudgetMatchResult.BudgetInsufficient(
                restaurant = restaurant,
                minimumTotal = cheapestCombo.totalEstimatedBill,
                shortfall = shortfall,
                explanation = "Minimum verified meal for $groupSize people here costs ₹${cheapestCombo.totalEstimatedBill.toInt()} (exceeds budget by ₹${shortfall.toInt()}). Try increasing budget to ₹${ceil(cheapestCombo.totalEstimatedBill / 100).toInt() * 100} or switching to Takeaway/Dhaba."
            )
        }
    }

    private fun buildMealPlan(
        title: String,
        subtitle: String,
        items: List<CalculatedItem>,
        restaurant: RestaurantEntity,
        groupSize: Int,
        totalBudget: Double,
        diningMode: String,
        tag: String
    ): CalculatedMealPlan {
        val subtotal = items.sumOf { it.lineTotal }
        val taxAmount = subtotal * (restaurant.gstPercentage / 100.0)
        val packagingFee = if (diningMode != "Dine-in") restaurant.packagingFee else 0.0
        val deliveryFee = if (diningMode == "Delivery") restaurant.deliveryFee else 0.0

        val totalBill = subtotal + taxAmount + packagingFee + deliveryFee
        val perPersonCost = totalBill / max(1, groupSize)
        val remainingBudget = totalBudget - totalBill
        val totalServings = items.sumOf { it.servesPeople }

        return CalculatedMealPlan(
            title = title,
            subtitle = subtitle,
            items = items,
            subtotal = subtotal,
            taxAmount = taxAmount,
            packagingFee = packagingFee,
            deliveryFee = deliveryFee,
            totalEstimatedBill = totalBill,
            perPersonCost = perPersonCost,
            remainingBudget = remainingBudget,
            totalServings = totalServings,
            isWithinBudget = totalBill <= totalBudget,
            tag = tag
        )
    }
}
