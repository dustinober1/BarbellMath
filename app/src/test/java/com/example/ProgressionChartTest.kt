package com.example

import com.example.data.entity.WorkoutSessionEntity
import com.example.data.repository.ChartTimeRange
import com.example.data.repository.ProgressionChartAnalyzer
import com.example.model.BarType
import com.example.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionChartTest {

  @Test
  fun analyzerSeparatesUnitsFiltersByLiftAndComputesMetrics() {
    val now = 100L * 86_400_000L
    val sessions = listOf(
      session(id = 1, timestamp = now - 40L * 86_400_000L, lift = "Squat", weight = 225.0, unit = WeightUnit.LBS),
      session(id = 2, timestamp = now - 10L * 86_400_000L, lift = "Squat", weight = 245.0, unit = WeightUnit.LBS),
      session(id = 3, timestamp = now - 2L * 86_400_000L, lift = "Squat", weight = 235.0, unit = WeightUnit.LBS),
      session(id = 4, timestamp = now - 5L * 86_400_000L, lift = "Bench Press", weight = 185.0, unit = WeightUnit.LBS),
      session(id = 5, timestamp = now - 3L * 86_400_000L, lift = "Deadlift", weight = 140.0, unit = WeightUnit.KG)
    )

    val allLbs = ProgressionChartAnalyzer.analyze(
      sessions = sessions,
      unit = WeightUnit.LBS,
      selectedLift = "All",
      timeRange = ChartTimeRange.ALL,
      nowMillis = now
    )
    assertEquals(4, allLbs.lbsCount)
    assertEquals(1, allLbs.kgCount)
    assertEquals(listOf("All", "Squat", "Bench Press"), allLbs.availableLifts)
    assertEquals(4, allLbs.points.size)
    assertEquals(245.0, allLbs.personalBestSession!!.achievedWeight, 0.001)
    assertEquals(235.0, allLbs.latestSession!!.achievedWeight, 0.001)
    assertEquals(10.0, allLbs.netChange!!, 0.001)
    assertEquals(2, allLbs.liftSummaries.size)

    val squat30Days = ProgressionChartAnalyzer.analyze(
      sessions = sessions,
      unit = WeightUnit.LBS,
      selectedLift = "Squat",
      timeRange = ChartTimeRange.DAYS_30,
      nowMillis = now
    )
    assertEquals(2, squat30Days.points.size)
    assertEquals(245.0, squat30Days.points[0].weight, 0.001)
    assertTrue(squat30Days.points[0].isPersonalBest)
    assertEquals(235.0, squat30Days.points[1].weight, 0.001)
    assertEquals(-10.0, squat30Days.netChange!!, 0.001)
    assertEquals(240.0, squat30Days.averageWeight!!, 0.001)

    val kgState = ProgressionChartAnalyzer.analyze(
      sessions = sessions,
      unit = WeightUnit.KG,
      selectedLift = "Squat", // not in KG -> falls back to All
      timeRange = ChartTimeRange.ALL,
      nowMillis = now
    )
    assertEquals("All", kgState.selectedLift)
    assertEquals(1, kgState.points.size)
    assertNull(kgState.netChange)
    assertNotNull(kgState.personalBestSession)
    assertTrue(kgState.maxWeight > kgState.minWeight)
  }

  private fun session(
    id: Long,
    timestamp: Long,
    lift: String,
    weight: Double,
    unit: WeightUnit
  ): WorkoutSessionEntity {
    return WorkoutSessionEntity(
      id = id,
      timestamp = timestamp,
      liftName = lift,
      reps = "5x5",
      notes = "",
      targetWeight = weight,
      achievedWeight = weight,
      unit = unit.name,
      barId = BarType.OLYMPIC_MENS.id,
      barName = BarType.OLYMPIC_MENS.name,
      barWeight = if (unit == WeightUnit.LBS) 45.0 else 20.0,
      collarsEnabled = false,
      platesSummary = "45.0:2",
      totalPlatesOnBar = 4,
      isExact = true
    )
  }
}
