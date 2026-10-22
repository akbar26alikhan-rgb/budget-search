package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.RestaurantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants ORDER BY verifiedRating DESC")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE id = :id LIMIT 1")
    fun getRestaurantById(id: String): Flow<RestaurantEntity?>

    @Query("SELECT * FROM restaurants WHERE id = :id LIMIT 1")
    suspend fun getRestaurantDirect(id: String): RestaurantEntity?

    @Query("SELECT * FROM restaurants WHERE locality LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' OR cuisineTypes LIKE '%' || :query || '%'")
    fun searchRestaurants(query: String): Flow<List<RestaurantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<RestaurantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: RestaurantEntity)

    @Update
    suspend fun updateRestaurant(restaurant: RestaurantEntity)

    @Query("SELECT COUNT(*) FROM restaurants")
    suspend fun getRestaurantCount(): Int

    @Query("DELETE FROM restaurants WHERE id = :id")
    suspend fun deleteRestaurant(id: String)
}
