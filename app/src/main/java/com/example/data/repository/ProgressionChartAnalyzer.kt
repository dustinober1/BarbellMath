package com.example.data.repository

import com.example.data.entity.WorkoutSessionEntity
import com.example.model.WeightUnit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class ChartTimeRange(val label: String, val months: Int?) {
  MONTH_1("1M", 1),
  MONTH_3("3M", 3),
  MONTH_6("6M", 6),
  YEAR_1("1Y", 12),
  ALL("All", null)
}

data class ChartPoint(
  val session: WorkoutSessionEntity,
  val index: Int,
  val weight: Double,
  val dateLabel: String,
  val deltaFromPrevious: Double?,
  val isPersonalBest: Boolean
)

data class LiftPrSummary(
  val liftName: String,
  val bestSession: WorkoutSessionEntity,
  val sessionCount: Int,
  val latestDelta: Double?
)

data class ProgressionChartState(
  val unit: WeightUnit = WeightUnit.LBS,
  val selectedLift: String = "All",
  val selectedTimeRange: ChartTimeRange = ChartTimeRange.ALL,
  val availableLifts: List<String> = listOf("All"),
  val points: List<ChartPoint> = emptyList(),
  val personalBestSession: WorkoutSessionEntity? = null,
  val latestSession: WorkoutSessionEntity? = null,
  val netChange: Double? = null,
  val averageWeight: Double? = null,
  val prCount: Int = 0,
  val lbsCount: Int = 0,
  val kgCount: Int = 0,
  val totalInUnitCount: Int = 0,
  val minWeight: Double = 0.0,
  val maxWeight: Double = 100.0,
  val liftSummaries: List<LiftPrSummary> = emptyList()
)

object ProgressionChartAnalyzer {

  fun analyze(
    sessions: List<WorkoutSessionEntity>,
    unit: WeightUnit,
    selectedLift: String = "All",
    timeRange: ChartTimeRange = ChartTimeRange.ALL,
    nowMillis: Long = System.currentTimeMillis()
  ): ProgressionChartState {
    val lbsCount = sessions.count { it.weightUnit == WeightUnit.LBS }
    val kgCount = sessions.count { it.weightUnit == WeightUnit.KG }

    val allProgressions = WorkoutSessionRepository.computeSessionProgressions(sessions)
    val unitSessions = sessions.filter { it.weightUnit == unit }

    // Canonical display lift names ordered by most recently logged
    val distinctLifts = mutableListOf<String>()
    val seenLower = mutableSetOf<String>()
    unitSessions
      .sortedWith(compareByDescending<WorkoutSessionEntity> { it.timestamp }.thenByDescending { it.id })
      .forEach { session ->
        val trimmed = session.liftName.trim().ifBlank { "General" }
        val key = trimmed.lowercase(Locale.getDefault())
        if (seenLower.add(key)) {
          distinctLifts.add(trimmed)
        }
      }

    val availableLifts = listOf("All") + distinctLifts
    val resolvedLift = availableLifts.firstOrNull {
      it.equals(selectedLift, ignoreCase = true)
    } ?: "All"

    val cutoffMillis = timeRange.months?.let { months ->
      Calendar.getInstance().apply {
        timeInMillis = nowMillis
        add(Calendar.MONTH, -months)
      }.timeInMillis
    }
    val timeFilteredUnitSessions = if (cutoffMillis != null) {
      unitSessions.filter { it.timestamp >= cutoffMillis }
    } else {
      unitSessions
    }

    val liftSummaries = buildLiftSummaries(timeFilteredUnitSessions, allProgressions)

    val filtered = if (resolvedLift == "All") {
      timeFilteredUnitSessions
    } else {
      timeFilteredUnitSessions.filter {
        it.liftName.trim().ifBlank { "General" }.equals(resolvedLift, ignoreCase = true)
      }
    }

    val chronological = filtered.sortedWith(
      compareBy<WorkoutSessionEntity> { it.timestamp }.thenBy { it.id }
    )

    val sameCalendarDay = isSameCalendarDay(chronological)
    val sameCalendarYear = isSameCalendarYear(chronological)
    val datePattern = when {
      sameCalendarDay -> "h:mm a"
      sameCalendarYear -> "MMM d"
      else -> "MMM d ''yy"
    }
    val dateFormatter = SimpleDateFormat(datePattern, Locale.getDefault())

    val points = chronological.mapIndexed { index, session ->
      val progression = allProgressions[session.id]
      val prevDelta = if (resolvedLift == "All" && index > 0) {
        val prev = chronological[index - 1]
        ((session.achievedWeight - prev.achievedWeight) * 100.0).roundToInt() / 100.0
      } else {
        progression?.deltaFromPrevious
      }

      ChartPoint(
        session = session,
        index = index,
        weight = session.achievedWeight,
        dateLabel = dateFormatter.format(Date(session.timestamp)),
        deltaFromPrevious = prevDelta,
        isPersonalBest = progression?.isPersonalBest == true
      )
    }

    val personalBestSession = chronological.maxWithOrNull(
      compareBy<WorkoutSessionEntity> { it.achievedWeight }
        .thenBy { it.timestamp }
        .thenBy { it.id }
    )
    val latestSession = chronological.lastOrNull()
    val netChange = if (points.size >= 2) {
      ((points.last().weight - points.first().weight) * 100.0).roundToInt() / 100.0
    } else {
      null
    }
    val averageWeight = if (points.isNotEmpty()) {
      ((points.sumOf { it.weight } / points.size) * 10.0).roundToInt() / 10.0
    } else {
      null
    }
    val prCount = points.count { it.isPersonalBest }

    val (minBound, maxBound) = computeAxisBounds(points.map { it.weight })

    return ProgressionChartState(
      unit = unit,
      selectedLift = resolvedLift,
      selectedTimeRange = timeRange,
      availableLifts = availableLifts,
      points = points,
      personalBestSession = personalBestSession,
      latestSession = latestSession,
      netChange = netChange,
      averageWeight = averageWeight,
      prCount = prCount,
      lbsCount = lbsCount,
      kgCount = kgCount,
      totalInUnitCount = unitSessions.size,
      minWeight = minBound,
      maxWeight = maxBound,
      liftSummaries = liftSummaries
    )
  }

  private fun buildLiftSummaries(
    sessions: List<WorkoutSessionEntity>,
    progressions: Map<Long, SessionProgression>
  ): List<LiftPrSummary> {
    if (sessions.isEmpty()) return emptyList()
    return sessions
      .groupBy { it.liftName.trim().ifBlank { "General" }.lowercase(Locale.getDefault()) }
      .values
      .map { group ->
        val sortedDesc = group.sortedWith(
          compareByDescending<WorkoutSessionEntity> { it.timestamp }.thenByDescending { it.id }
        )
        val best = group.maxWith(
          compareBy<WorkoutSessionEntity> { it.achievedWeight }
            .thenBy { it.timestamp }
            .thenBy { it.id }
        )
        val latest = sortedDesc.first()
        LiftPrSummary(
          liftName = latest.liftName.trim().ifBlank { "General" },
          bestSession = best,
          sessionCount = group.size,
          latestDelta = progressions[latest.id]?.deltaFromPrevious
        )
      }
      .sortedByDescending { it.bestSession.achievedWeight }
  }

  private fun isSameCalendarDay(sessions: List<WorkoutSessionEntity>): Boolean {
    if (sessions.size <= 1) return false
    val cal = Calendar.getInstance()
    cal.timeInMillis = sessions.first().timestamp
    val firstYear = cal.get(Calendar.YEAR)
    val firstDay = cal.get(Calendar.DAY_OF_YEAR)
    return sessions.all { session ->
      cal.timeInMillis = session.timestamp
      cal.get(Calendar.YEAR) == firstYear && cal.get(Calendar.DAY_OF_YEAR) == firstDay
    }
  }

  private fun isSameCalendarYear(sessions: List<WorkoutSessionEntity>): Boolean {
    if (sessions.size <= 1) return true
    val cal = Calendar.getInstance()
    cal.timeInMillis = sessions.first().timestamp
    val firstYear = cal.get(Calendar.YEAR)
    return sessions.all { session ->
      cal.timeInMillis = session.timestamp
      cal.get(Calendar.YEAR) == firstYear
    }
  }

  private fun computeAxisBounds(weights: List<Double>): Pair<Double, Double> {
    if (weights.isEmpty()) return 0.0 to 100.0
    val rawMin = weights.min()
    val rawMax = weights.max()
    val span = rawMax - rawMin
    return if (span < 0.01) {
      val pad = maxOf(5.0, (rawMax * 0.1).roundToInt().toDouble())
      maxOf(0.0, rawMin - pad) to (rawMax + pad)
    } else {
      val pad = maxOf(2.5, (span * 0.15))
      maxOf(0.0, rawMin - pad) to (rawMax + pad)
    }
  }
}
