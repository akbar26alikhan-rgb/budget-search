package com.example

import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.RestaurantEntity
import com.example.engine.BudgetMatchResult
import com.example.engine.SmartBudgetEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartBudgetEngineTest {

    private val testRestaurant = RestaurantEntity(
        id = "vasai_rajeshwar_veg",
        name = "Hotel Rajeshwar Pure Veg",
        slug = "hotel-rajeshwar-pure-veg",
        description = "Family veg restaurant in Vasai West",
        address = "Station Road, Vasai West",
        locality = "Vasai West",
        city = "Vasai",
        latitude = 19.3820,
        longitude = 72.8315,
        phone = "+91 250 234 1189",
        cuisineTypes = "Maharashtrian, North Indian, Thalis",
        outletType = "Family Restaurant",
        diningModes = "Dine-in, Takeaway, Delivery",
        verifiedRating = 4.6,
        ratingCount = 840,
        openingHours = "11:00 AM - 11:00 PM",
        lastVerifiedAt = "2026-10-18",
        gstPercentage = 5.0,
        packagingFee = 20.0,
        deliveryFee = 30.0
    )

    private val testMenuItems = listOf(
        MenuItemEntity(
            id = "thali_01",
            restaurantId = "vasai_rajeshwar_veg",
            categoryId = "thalis",
            name = "Special Veg Thali",
            description = "Complete lunch meal for 1 person",
            price = 150.0,
            dietaryType = "VEG",
            servingSize = "1 person",
            servesPeople = 1,
            lastVerifiedAt = "2026-10-18"
        ),
        MenuItemEntity(
            id = "curry_01",
            restaurantId = "vasai_rajeshwar_veg",
            categoryId = "curries",
            name = "Paneer Butter Masala Handi",
            description = "Serves 2-3 people",
            price = 180.0,
            dietaryType = "VEG",
            servingSize = "Handi (serves 2-3)",
            servesPeople = 2,
            lastVerifiedAt = "2026-10-18"
        ),
        MenuItemEntity(
            id = "bread_01",
            restaurantId = "vasai_rajeshwar_veg",
            categoryId = "breads",
            name = "Butter Roti",
            description = "Tawa roti with butter",
            price = 18.0,
            dietaryType = "VEG",
            servingSize = "1 pc",
            servesPeople = 1,
            lastVerifiedAt = "2026-10-18"
        ),
        MenuItemEntity(
            id = "rice_01",
            restaurantId = "vasai_rajeshwar_veg",
            categoryId = "rice",
            name = "Jeera Rice",
            description = "Basmati rice",
            price = 110.0,
            dietaryType = "VEG",
            servingSize = "Serves 2",
            servesPeople = 2,
            lastVerifiedAt = "2026-10-18"
        ),
        MenuItemEntity(
            id = "nonveg_01",
            restaurantId = "vasai_rajeshwar_veg",
            categoryId = "curries",
            name = "Chicken Handi",
            description = "Non-veg chicken",
            price = 280.0,
            dietaryType = "NON_VEG",
            servingSize = "Serves 2-3",
            servesPeople = 2,
            lastVerifiedAt = "2026-10-18"
        )
    )

    @Test
    fun testFamilyOfSixWith1500BudgetScenario() {
        // Test scenario required by specification:
        // Family of six, ₹1,500 budget, vegetarian lunch in Vasai.
        val result = SmartBudgetEngine.calculateMatchesForRestaurant(
            restaurant = testRestaurant,
            menuItems = testMenuItems,
            groupSize = 6,
            totalBudget = 1500.0,
            dietaryPreference = "VEG",
            diningMode = "Dine-in"
        )

        assertTrue("Result must be Success for valid budget", result is BudgetMatchResult.Success)
        val success = result as BudgetMatchResult.Success
        val bestFit = success.bestFit

        // Assert total never silently exceeds budget
        assertTrue("Total must not exceed ₹1,500", bestFit.totalEstimatedBill <= 1500.0)
        assertTrue("Remaining budget must be >= 0", bestFit.remainingBudget >= 0.0)

        // Find Thali combo in recommendedCombos
        val thaliCombo = success.recommendedCombos.firstOrNull { it.title.contains("Thali") }
        assertNotNull("Must generate Thali feast combo", thaliCombo)
        // Subtotal for 6 thalis = 6 * 150 = 900
        // GST at 5% = 45 -> Total = 945
        assertEquals(900.0, thaliCombo!!.subtotal, 1.0)
        assertEquals(945.0, thaliCombo.totalEstimatedBill, 1.0)
        assertEquals(555.0, thaliCombo.remainingBudget, 1.0)
        assertEquals(157.5, thaliCombo.perPersonCost, 1.0)
        assertEquals(6, thaliCombo.totalServings)
    }

    @Test
    fun testBudgetOverrunDetectionWhenBudgetIsTooLow() {
        // Test scenario where budget is ₹500 for 6 people
        val result = SmartBudgetEngine.calculateMatchesForRestaurant(
            restaurant = testRestaurant,
            menuItems = testMenuItems,
            groupSize = 6,
            totalBudget = 500.0,
            dietaryPreference = "VEG",
            diningMode = "Dine-in"
        )

        assertTrue("Result must indicate insufficient budget", result is BudgetMatchResult.BudgetInsufficient)
        val insufficient = result as BudgetMatchResult.BudgetInsufficient
        assertTrue("Shortfall must be greater than 0", insufficient.shortfall > 0)
        assertTrue("Explanation must warn user clearly", insufficient.explanation.contains("exceeds budget"))
    }

    @Test
    fun testDietaryFilterExcludesNonVeg() {
        val nonVegOnlyList = listOf(
            MenuItemEntity(
                id = "chicken_01",
                restaurantId = "vasai_rajeshwar_veg",
                categoryId = "curries",
                name = "Butter Chicken",
                description = "Non-veg",
                price = 280.0,
                dietaryType = "NON_VEG",
                servingSize = "Serves 2",
                servesPeople = 2,
                lastVerifiedAt = "2026-10-18"
            )
        )

        val result = SmartBudgetEngine.calculateMatchesForRestaurant(
            restaurant = testRestaurant,
            menuItems = nonVegOnlyList,
            groupSize = 4,
            totalBudget = 2000.0,
            dietaryPreference = "VEG",
            diningMode = "Dine-in"
        )

        assertTrue("Result should report no matching veg dishes", result is BudgetMatchResult.NoMatchingDishes)
    }

    @Test
    fun testTakeawayAndDeliveryExtraCharges() {
        val takeawayResult = SmartBudgetEngine.calculateMatchesForRestaurant(
            restaurant = testRestaurant,
            menuItems = testMenuItems,
            groupSize = 6,
            totalBudget = 1500.0,
            dietaryPreference = "VEG",
            diningMode = "Takeaway"
        ) as BudgetMatchResult.Success

        // Packaging fee of 20 should be included
        assertEquals(20.0, takeawayResult.bestFit.packagingFee, 0.01)

        val deliveryResult = SmartBudgetEngine.calculateMatchesForRestaurant(
            restaurant = testRestaurant,
            menuItems = testMenuItems,
            groupSize = 6,
            totalBudget = 1500.0,
            dietaryPreference = "VEG",
            diningMode = "Delivery"
        ) as BudgetMatchResult.Success

        // Packaging (20) + Delivery (30) should be included
        assertEquals(20.0, deliveryResult.bestFit.packagingFee, 0.01)
        assertEquals(30.0, deliveryResult.bestFit.deliveryFee, 0.01)
    }
}
