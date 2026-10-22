package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "menu_items",
    foreignKeys = [
        ForeignKey(
            entity = RestaurantEntity::class,
            parentColumns = ["id"],
            childColumns = ["restaurantId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MenuCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["restaurantId"]),
        Index(value = ["categoryId"])
    ]
)
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val restaurantId: String,
    val categoryId: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String = "INR",
    val dietaryType: String, // "VEG", "NON_VEG", "EGG"
    val servingSize: String, // e.g. "Full Thali (1 person)", "Handi (serves 2-3)", "Basket of 4"
    val servesPeople: Int = 1, // Number of adults this portion satisfies
    val minimumQuantity: Int = 1,
    val maximumQuantity: Int = 20,
    val isAvailable: Boolean = true,
    val priceSource: String = "Verified Menu 2026",
    val lastVerifiedAt: String,
    val imageUrl: String = ""
)
