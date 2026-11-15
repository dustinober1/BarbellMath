package com.example.data.repository

import com.example.data.entity.WorkoutSessionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt

object ProgressionCsvExporter {

  val CSV_HEADERS: List<String> = listOf(
    "Date",
    "Time",
    "Lift",
    "Achieved Weight",
    "Target Weight",
    "Unit",
    "Delta From Previous",
    "Personal Best",
    "Sets/Reps",
    "Bar",
    "Bar Weight",
    "Collars Enabled",
    "Plates Per Side",
    "Total Plates",
    "Exact Match",
    "Notes"
  )

  fun buildCsv(
    points: List<ChartPoint>,
    timeZone: TimeZone = TimeZone.getDefault()
  ): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
      this.timeZone = timeZone
    }
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US).apply {
      this.timeZone = timeZone
    }

    val builder = StringBuilder()
    builder.appendLine(CSV_HEADERS.joinToString(",") { escapeCsvField(it) })

    points.forEach { point ->
      val session = point.session
      val date = Date(session.timestamp)
      val deltaFormatted = point.deltaFromPrevious?.let { delta ->
        when {
          delta > 0.0 -> "+${formatDecimal(delta)}"
          else -> formatDecimal(delta)
        }
      } ?: ""

      val platesFormatted = formatPlatesPerSide(session)

      val row = listOf(
        dateFormat.format(date),
        timeFormat.format(date),
        session.liftName.trim().ifBlank { "General" },
        formatDecimal(session.achievedWeight),
        formatDecimal(session.targetWeight),
        session.weightUnit.shortLabel,
        deltaFormatted,
        if (point.isPersonalBest) "Yes" else "No",
        session.reps.trim(),
        session.barName,
        formatDecimal(session.barWeight),
        if (session.collarsEnabled) "Yes" else "No",
        platesFormatted,
        session.totalPlatesOnBar.toString(),
        if (session.isExact) "Yes" else "No",
        session.notes.trim()
      )
      builder.appendLine(row.joinToString(",") { escapeCsvField(it) })
    }

    return builder.toString()
  }

  fun suggestedFileName(
    chartState: ProgressionChartState,
    nowMillis: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault()
  ): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
      this.timeZone = timeZone
    }
    val liftSlug = if (chartState.selectedLift.equals("All", ignoreCase = true)) {
      "progression"
    } else {
      chartState.selectedLift
        .lowercase(Locale.US)
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
        .ifBlank { "progression" }
    }
    val rangeSlug = if (chartState.selectedTimeRange == ChartTimeRange.ALL) {
      ""
    } else {
      "_${chartState.selectedTimeRange.label.lowercase(Locale.US)}"
    }
    val unitSlug = chartState.unit.shortLabel.lowercase(Locale.US)
    val dateSlug = dateFormat.format(Date(nowMillis))
    return "plate_master_${liftSlug}_${unitSlug}${rangeSlug}_${dateSlug}.csv"
  }

  internal fun escapeCsvField(value: String): String {
    val needsQuoting = value.contains(',') ||
      value.contains('"') ||
      value.contains('\n') ||
      value.contains('\r')
    return if (needsQuoting) {
      "\"${value.replace("\"", "\"\"")}\""
    } else {
      value
    }
  }

  private fun formatPlatesPerSide(session: WorkoutSessionEntity): String {
    val parsed = session.parsePlates()
    if (parsed.isEmpty()) return ""
    return parsed.joinToString("; ") { (weight, qty) ->
      "${formatDecimal(weight)}x$qty"
    }
  }

  private fun formatDecimal(value: Double): String {
    val rounded = (value * 100.0).roundToInt() / 100.0
    return if (abs(rounded - rounded.toLong()) < 0.001) {
      rounded.toLong().toString()
    } else {
      rounded.toString()
    }
  }
}
