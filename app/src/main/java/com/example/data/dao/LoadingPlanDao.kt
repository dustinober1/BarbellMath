package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.LoadingPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoadingPlanDao {

  @Query("SELECT * FROM loading_plans ORDER BY timestamp DESC LIMIT :limit")
  fun getRecentPlans(limit: Int = 10): Flow<List<LoadingPlanEntity>>

  @Query("SELECT * FROM loading_plans ORDER BY timestamp DESC LIMIT 1")
  suspend fun getLatestPlan(): LoadingPlanEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlan(plan: LoadingPlanEntity): Long

  @Query("DELETE FROM loading_plans WHERE id NOT IN (SELECT id FROM loading_plans ORDER BY timestamp DESC LIMIT :keepCount)")
  suspend fun trimOldPlans(keepCount: Int = 10)

  @Query("DELETE FROM loading_plans WHERE id = :id")
  suspend fun deletePlanById(id: Long)

  @Query("DELETE FROM loading_plans")
  suspend fun clearAllPlans()

  @Query("SELECT COUNT(*) FROM loading_plans")
  suspend fun getPlanCount(): Int
}
