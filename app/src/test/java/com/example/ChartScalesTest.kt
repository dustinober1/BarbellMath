package com.example

import com.example.data.entity.WorkoutSessionEntity
import com.example.data.repository.ChartPoint
import com.example.model.BarType
import com.example.model.WeightUnit
import com.example.ui.components.LinearChartScale
import com.example.ui.components.TimeChartScale
import com.example.ui.components.resolvePrMilestoneIndices
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartScalesTest {

  @Test
  fun linearScaleProducesNiceRoundedDomainAndTicks() {
    val scale = LinearChartScale.nice(rawMin = 183.0, rawMax = 247.0, desiredTickCount = 5)
    assertTrue(scale.domainMin <= 183.0)
    assertTrue(scale.domainMax >= 247.0)
    assertTrue(scale.ticks.size >= 4)

    // Verify uniform nice intervals
    val step = scale.ticks[1] - scale.ticks[0]
    assertTrue(step > 0.0)
    for (i in 1 until scale.ticks.size) {
      assertEquals(step, scale.ticks[i] - scale.ticks[i - 1], 0.001)
    }

    val yBottom = scale.yFor(scale.domainMin, plotBottom = 200f, plotHeight = 160f)
    val yTop = scale.yFor(scale.domainMax, plotBottom = 200f, plotHeight = 160f)
    assertEquals(200f, yBottom, 0.01f)
    assertEquals(40f, yTop, 0.01f)
  }

  @Test
  fun timeChartScaleMapsProportionalTimestampsAndResolvesNearestPoint() {
    val dayMs = 86_400_000L
    val points = listOf(
      chartPoint(id = 1, timestamp = 0L, weight = 200.0),
      chartPoint(id = 2, timestamp = 2L * dayMs, weight = 225.0),
      chartPoint(id = 3, timestamp = 10L * dayMs, weight = 245.0)
    )

    val scale = TimeChartScale(points = points, leftPad = 50f, plotWidth = 500f)
    assertEquals(50f, scale.xFor(0), 0.1f)
    assertEquals(150f, scale.xFor(1), 0.1f) // 2 / 10 * 500 + 50 = 150
    assertEquals(550f, scale.xFor(2), 0.1f)

    assertEquals(0, scale.nearestIndex(60f))
    assertEquals(1, scale.nearestIndex(165f))
    assertEquals(2, scale.nearestIndex(510f))
  }

  @Test
  fun resolvePrMilestoneIndicesHighlightsMilestonesAndFallsBackToPeak() {
    val ascendingThenDrop = listOf(
      chartPoint(id = 1, timestamp = 1000L, weight = 205.0, isPr = false),
      chartPoint(id = 2, timestamp = 2000L, weight = 225.0, isPr = true),
      chartPoint(id = 3, timestamp = 3000L, weight = 245.0, isPr = true),
      chartPoint(id = 4, timestamp = 4000L, weight = 235.0, isPr = false)
    )
    assertEquals(setOf(1, 2), resolvePrMilestoneIndices(ascendingThenDrop))

    val descendingOnly = listOf(
      chartPoint(id = 1, timestamp = 1000L, weight = 250.0, isPr = false),
      chartPoint(id = 2, timestamp = 2000L, weight = 225.0, isPr = false)
    )
    assertEquals(setOf(0), resolvePrMilestoneIndices(descendingOnly))
  }

  private fun chartPoint(
    id: Long,
    timestamp: Long,
    weight: Double,
    isPr: Boolean = false
  ): ChartPoint {
    val session = WorkoutSessionEntity(
      id = id,
      timestamp = timestamp,
      liftName = "Squat",
      reps = "5×5",
      targetWeight = weight,
      achievedWeight = weight,
      unit = WeightUnit.LBS.name,
      barId = BarType.OLYMPIC_MENS.id,
      barName = BarType.OLYMPIC_MENS.name,
      barWeight = 45.0,
      platesSummary = "45.0:2",
      totalPlatesOnBar = 4,
      isExact = true
    )
    return ChartPoint(
      session = session,
      index = id.toInt(),
      weight = weight,
      dateLabel = "Nov 14",
      deltaFromPrevious = null,
      isPersonalBest = isPr
    )
  }
}
