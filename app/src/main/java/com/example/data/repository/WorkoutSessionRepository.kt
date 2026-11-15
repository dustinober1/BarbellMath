package com.example.data.repository

import com.example.data.dao.WorkoutSessionDao
import com.example.data.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

data class SessionProgression(
  val deltaFromPrevious: Double? = null,
  val isPersonalBest: Boolean = false
)

class WorkoutSessionRepository(private val dao: WorkoutSessionDao) {

  val recentSessions: Flow<List<WorkoutSessionEntity>> = dao.getRecentSessions(50)
  val allSessions: Flow<List<WorkoutSessionEntity>> = dao.getAllSessions()

  suspend fun saveSession(session: WorkoutSessionEntity): Long {
    return dao.insertSession(session)
  }

  suspend fun deleteSessionById(id: Long) {
    dao.deleteSessionById(id)
  }

  suspend fun clearSessions() {
    dao.clearAllSessions()
  }

  companion object {
    fun computeSessionProgressions(
      sessions: List<WorkoutSessionEntity>
    ): Map<Long, SessionProgression> {
      if (sessions.isEmpty()) return emptyMap()

      val result = mutableMapOf<Long, SessionProgression>()
      val grouped = sessions.groupBy {
        it.liftName.trim().lowercase() to it.unit
      }

      grouped.values.forEach { group ->
        val chronological = group.sortedWith(
          compareBy<WorkoutSessionEntity> { it.timestamp }.thenBy { it.id }
        )
        var previousWeight: Double? = null
        var runningBest: Double? = null

        chronological.forEach { session ->
          val delta = previousWeight?.let { prev ->
            ((session.achievedWeight - prev) * 100.0).roundToInt() / 100.0
          }
          val isPr = runningBest != null && session.achievedWeight > runningBest!!
          result[session.id] = SessionProgression(
            deltaFromPrevious = delta,
            isPersonalBest = isPr
          )
          previousWeight = session.achievedWeight
          runningBest = maxOf(runningBest ?: session.achievedWeight, session.achievedWeight)
        }
      }

      return result
    }
  }
}
