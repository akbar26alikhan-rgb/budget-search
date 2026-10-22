package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AdminDao
import com.example.data.local.dao.MealPlanDao
import com.example.data.local.dao.MenuDao
import com.example.data.local.dao.RestaurantDao
import com.example.data.local.dao.UserFavouriteDao
import com.example.data.local.entity.MealPlanEntity
import com.example.data.local.entity.MealPlanItemEntity
import com.example.data.local.entity.MenuCategoryEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.PriceHistoryEntity
import com.example.data.local.entity.RestaurantClaimEntity
import com.example.data.local.entity.RestaurantEntity
import com.example.data.local.entity.UserFavouriteEntity

@Database(
    entities = [
        RestaurantEntity::class,
        MenuCategoryEntity::class,
        MenuItemEntity::class,
        MealPlanEntity::class,
        MealPlanItemEntity::class,
        UserFavouriteEntity::class,
        RestaurantClaimEntity::class,
        PriceHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
    abstract fun menuDao(): MenuDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun userFavouriteDao(): UserFavouriteDao
    abstract fun adminDao(): AdminDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_bites_vasai.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
