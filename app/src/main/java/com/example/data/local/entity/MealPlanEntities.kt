package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "guest_user",
    val name: String,
    val restaurantId: String,
    val restaurantName: String,
    val groupSize: Int,
    val budget: Double,
    val diningMode: String,
    val estimatedSubtotal: Double,
    val taxAmount: Double,
    val extraFees: Double,
    val estimatedTotal: Double,
    val remainingBudget: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "meal_plan_items")
data class MealPlanItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealPlanId: Long,
    val menuItemId: String,
    val menuItemName: String,
    val quantity: Int,
    val unitPriceSnapshot: Double,
    val lineTotal: Double,
    val servesPeople: Int
)

@Entity(tableName = "user_favourites")
data class UserFavouriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val restaurantId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "restaurant_claims")
data class RestaurantClaimEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val restaurantId: String,
    val restaurantName: String,
    val ownerName: String,
    val ownerPhone: String,
    val claimStatus: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "restaurant_price_history")
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val menuItemId: String,
    val dishName: String,
    val previousPrice: Double,
    val newPrice: Double,
    val source: String,
    val verifiedAt: String
)
