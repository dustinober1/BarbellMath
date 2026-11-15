package com.example

import com.example.data.entity.WorkoutSessionEntity
import com.example.data.repository.ChartTimeRange
import com.example.data.repository.ProgressionChartAnalyzer
import com.example.data.repository.ProgressionCsvExporter
import com.example.model.BarType
import com.example.model.WeightUnit
import java.util.TimeZone
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

    val squatOneMonth = ProgressionChartAnalyzer.analyze(
      sessions = sessions,
      unit = WeightUnit.LBS,
      selectedLift = "Squat",
      timeRange = ChartTimeRange.MONTH_1,
      nowMillis = now
    )
    assertEquals(2, squatOneMonth.points.size)
    assertEquals(245.0, squatOneMonth.points[0].weight, 0.001)
    assertTrue(squatOneMonth.points[0].isPersonalBest)
    assertEquals(235.0, squatOneMonth.points[1].weight, 0.001)
    assertEquals(-10.0, squatOneMonth.netChange!!, 0.001)
    assertEquals(240.0, squatOneMonth.averageWeight!!, 0.001)

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

  @Test
  fun dateRangeFilterNarrowsBy1M3M6M1YAndAll() {
    val dayMillis = 86_400_000L
    val now = 500L * dayMillis
    val sessions = listOf(
      session(id = 1, timestamp = now - 400L * dayMillis, lift = "Squat", weight = 185.0, unit = WeightUnit.LBS), // > 1Y
      session(id = 2, timestamp = now - 250L * dayMillis, lift = "Squat", weight = 205.0, unit = WeightUnit.LBS), // within 1Y
      session(id = 3, timestamp = now - 120L * dayMillis, lift = "Squat", weight = 215.0, unit = WeightUnit.LBS), // within 6M
      session(id = 4, timestamp = now - 50L * dayMillis, lift = "Squat", weight = 225.0, unit = WeightUnit.LBS),  // within 3M
      session(id = 5, timestamp = now - 10L * dayMillis, lift = "Squat", weight = 245.0, unit = WeightUnit.LBS)   // within 1M
    )

    assertEquals(
      1,
      ProgressionChartAnalyzer.analyze(sessions, WeightUnit.LBS, "Squat", ChartTimeRange.MONTH_1, now).points.size
    )
    assertEquals(
      2,
      ProgressionChartAnalyzer.analyze(sessions, WeightUnit.LBS, "Squat", ChartTimeRange.MONTH_3, now).points.size
    )
    assertEquals(
      3,
      ProgressionChartAnalyzer.analyze(sessions, WeightUnit.LBS, "Squat", ChartTimeRange.MONTH_6, now).points.size
    )
    assertEquals(
      4,
      ProgressionChartAnalyzer.analyze(sessions, WeightUnit.LBS, "Squat", ChartTimeRange.YEAR_1, now).points.size
    )
    assertEquals(
      5,
      ProgressionChartAnalyzer.analyze(sessions, WeightUnit.LBS, "Squat", ChartTimeRange.ALL, now).points.size
    )
  }

  @Test
  fun csvExporterFormatsRowsAndEscapesRfc4180Fields() {
    val utc = TimeZone.getTimeZone("UTC")
    val sessions = listOf(
      session(
        id = 1,
        timestamp = 1_700_000_000_000L, // 2023-11-14 22:13:20 UTC
        lift = "Squat",
        weight = 225.0,
        unit = WeightUnit.LBS,
        notes = "Belt, sleeves, \"paused\" rep\nsecond line",
        collars = true,
        platesSummary = "45.0:2,25.0:1"
      ),
      session(
        id = 2,
        timestamp = 1_700_086_400_000L, // 2023-11-15 22:13:20 UTC
        lift = "Squat",
        weight = 245.5,
        unit = WeightUnit.LBS,
        notes = "Felt strong",
        collars = false,
        platesSummary = "45.0:2,25.0:1,2.5:1"
      )
    )

    val state = ProgressionChartAnalyzer.analyze(
      sessions = sessions,
      unit = WeightUnit.LBS,
      selectedLift = "Squat",
      timeRange = ChartTimeRange.MONTH_3,
      nowMillis = 1_700_100_000_000L
    )

    val csv = ProgressionCsvExporter.buildCsv(state.points, timeZone = utc)
    val lines = csv.trimEnd().lines()
    assertTrue(lines.first().startsWith("Date,Time,Lift,Achieved Weight,Target Weight,Unit,"))
    assertTrue(csv.contains("\"Belt, sleeves, \"\"paused\"\" rep\nsecond line\""))
    assertTrue(csv.contains("2023-11-14,22:13:20,Squat,225,225,lbs,,No,5x5,"))
    assertTrue(csv.contains("2023-11-15,22:13:20,Squat,245.5,245.5,lbs,+20.5,Yes,5x5,"))
    assertTrue(csv.contains("45x2; 25x1"))
    assertTrue(csv.contains("45x2; 25x1; 2.5x1"))

    val fileName = ProgressionCsvExporter.suggestedFileName(
      chartState = state,
      nowMillis = 1_700_086_400_000L,
      timeZone = utc
    )
    assertEquals("plate_master_squat_lbs_3m_2023-11-15.csv", fileName)
  }

  @Test
  fun csvExporterHandlesEmptyPointsWithHeaderOnly() {
    val csv = ProgressionCsvExporter.buildCsv(emptyList())
    val lines = csv.trimEnd().lines()
    assertEquals(1, lines.size)
    assertEquals(ProgressionCsvExporter.CSV_HEADERS.joinToString(","), lines[0])
  }

  private fun session(
    id: Long,
    timestamp: Long,
    lift: String,
    weight: Double,
    unit: WeightUnit,
    notes: String = "",
    collars: Boolean = false,
    platesSummary: String = "45.0:2"
  ): WorkoutSessionEntity {
    return WorkoutSessionEntity(
      id = id,
      timestamp = timestamp,
      liftName = lift,
      reps = "5x5",
      notes = notes,
      targetWeight = weight,
      achievedWeight = weight,
      unit = unit.name,
      barId = BarType.OLYMPIC_MENS.id,
      barName = BarType.OLYMPIC_MENS.name,
      barWeight = if (unit == WeightUnit.LBS) 45.0 else 20.0,
      collarsEnabled = collars,
      platesSummary = platesSummary,
      totalPlatesOnBar = 4,
      isExact = true
    )
  }
}
