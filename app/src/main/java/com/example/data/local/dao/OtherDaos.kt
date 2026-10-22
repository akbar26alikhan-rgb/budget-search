package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.MealPlanEntity
import com.example.data.local.entity.MealPlanItemEntity
import com.example.data.local.entity.PriceHistoryEntity
import com.example.data.local.entity.RestaurantClaimEntity
import com.example.data.local.entity.UserFavouriteEntity
import kotlinx.coroutines.flow.Flow

data class MealPlanWithItems(
    val plan: MealPlanEntity,
    val items: List<MealPlanItemEntity>
)

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans ORDER BY createdAt DESC")
    fun getAllMealPlans(): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plan_items WHERE mealPlanId = :planId")
    suspend fun getItemsForPlan(planId: Long): List<MealPlanItemEntity>

    @Query("SELECT * FROM meal_plan_items WHERE mealPlanId = :planId")
    fun getItemsForPlanFlow(planId: Long): Flow<List<MealPlanItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(plan: MealPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlanItems(items: List<MealPlanItemEntity>)

    @Query("DELETE FROM meal_plans WHERE id = :planId")
    suspend fun deleteMealPlan(planId: Long)

    @Query("DELETE FROM meal_plan_items WHERE mealPlanId = :planId")
    suspend fun deleteItemsForPlan(planId: Long)

    @Transaction
    suspend fun saveFullMealPlan(plan: MealPlanEntity, items: List<MealPlanItemEntity>): Long {
        val planId = insertMealPlan(plan)
        val itemsWithPlanId = items.map { it.copy(mealPlanId = planId) }
        insertMealPlanItems(itemsWithPlanId)
        return planId
    }
}

@Dao
interface UserFavouriteDao {
    @Query("SELECT * FROM user_favourites ORDER BY createdAt DESC")
    fun getAllFavourites(): Flow<List<UserFavouriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM user_favourites WHERE restaurantId = :restaurantId)")
    fun isFavourite(restaurantId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(fav: UserFavouriteEntity)

    @Query("DELETE FROM user_favourites WHERE restaurantId = :restaurantId")
    suspend fun removeFavourite(restaurantId: String)
}

@Dao
interface AdminDao {
    @Query("SELECT * FROM restaurant_claims ORDER BY createdAt DESC")
    fun getAllClaims(): Flow<List<RestaurantClaimEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: RestaurantClaimEntity)

    @Query("UPDATE restaurant_claims SET claimStatus = :status WHERE id = :claimId")
    suspend fun updateClaimStatus(claimId: Long, status: String)

    @Query("SELECT * FROM restaurant_price_history ORDER BY id DESC")
    fun getAllPriceHistory(): Flow<List<PriceHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(history: PriceHistoryEntity)
}
