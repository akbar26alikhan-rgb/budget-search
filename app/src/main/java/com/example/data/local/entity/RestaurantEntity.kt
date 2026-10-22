package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val slug: String,
    val description: String,
    val address: String,
    val locality: String,
    val city: String = "Vasai",
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val website: String = "",
    val cuisineTypes: String, // comma-separated e.g. "Maharashtrian, North Indian, Thalis"
    val outletType: String, // "Family Restaurant", "Dhaba", "Local Eatery", "Cafe", "Cloud Kitchen"
    val diningModes: String, // "Dine-in, Takeaway, Delivery"
    val verifiedRating: Double,
    val ratingCount: Int,
    val openingHours: String,
    val isOpenNow: Boolean = true,
    val dataSource: String = "Vasai Local Food Index (Verified)",
    val verificationStatus: String = "VERIFIED", // "VERIFIED", "DEMO_DATA", "PENDING_REVIEW"
    val lastVerifiedAt: String,
    val gstPercentage: Double = 5.0,
    val packagingFee: Double = 20.0,
    val deliveryFee: Double = 35.0,
    val familySeatingConfirmed: Boolean = true,
    val imageUrl: String = ""
)
