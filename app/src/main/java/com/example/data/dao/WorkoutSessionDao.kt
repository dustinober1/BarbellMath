package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

  @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC, id DESC LIMIT :limit")
  fun getRecentSessions(limit: Int = 50): Flow<List<WorkoutSessionEntity>>

  @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC, id DESC")
  fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: WorkoutSessionEntity): Long

  @Query("DELETE FROM workout_sessions WHERE id = :id")
  suspend fun deleteSessionById(id: Long)

  @Query("DELETE FROM workout_sessions")
  suspend fun clearAllSessions()

  @Query("SELECT COUNT(*) FROM workout_sessions")
  suspend fun getSessionCount(): Int
}
