package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MenuCategoryEntity
import com.example.data.local.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    @Query("SELECT * FROM menu_categories WHERE restaurantId = :restaurantId ORDER BY displayOrder ASC")
    fun getCategoriesForRestaurant(restaurantId: String): Flow<List<MenuCategoryEntity>>

    @Query("SELECT * FROM menu_items WHERE restaurantId = :restaurantId")
    fun getMenuItemsForRestaurant(restaurantId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE restaurantId = :restaurantId")
    suspend fun getMenuItemsForRestaurantDirect(restaurantId: String): List<MenuItemEntity>

    @Query("SELECT * FROM menu_items WHERE categoryId = :categoryId")
    fun getMenuItemsByCategory(categoryId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    suspend fun getMenuItemById(id: String): MenuItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<MenuCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity)

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Query("UPDATE menu_items SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateAvailability(id: String, isAvailable: Boolean)

    @Query("UPDATE menu_items SET price = :newPrice, lastVerifiedAt = :verifiedAt WHERE id = :id")
    suspend fun updatePrice(id: String, newPrice: Double, verifiedAt: String)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItem(id: String)
}
