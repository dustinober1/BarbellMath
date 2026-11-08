package com.example.data.repository

import com.example.data.dao.LoadingPlanDao
import com.example.data.entity.LoadingPlanEntity
import kotlinx.coroutines.flow.Flow

class LoadingPlanRepository(private val dao: LoadingPlanDao) {

  val recentPlans: Flow<List<LoadingPlanEntity>> = dao.getRecentPlans(10)

  suspend fun savePlan(plan: LoadingPlanEntity) {
    // Avoid saving if immediately identical to the latest saved plan
    val latest = dao.getLatestPlan()
    if (latest != null &&
      latest.targetWeight == plan.targetWeight &&
      latest.unit == plan.unit &&
      latest.barId == plan.barId &&
      latest.collarsEnabled == plan.collarsEnabled &&
      latest.platesSummary == plan.platesSummary
    ) {
      return
    }

    dao.insertPlan(plan)
    dao.trimOldPlans(10)
  }

  suspend fun deletePlanById(id: Long) {
    dao.deletePlanById(id)
  }

  suspend fun clearHistory() {
    dao.clearAllPlans()
  }
}
