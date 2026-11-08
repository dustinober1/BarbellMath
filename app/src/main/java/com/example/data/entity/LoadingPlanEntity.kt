package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Plate
import com.example.model.WeightUnit

@Entity(tableName = "loading_plans")
data class LoadingPlanEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val targetWeight: Double,
  val achievedWeight: Double,
  val unit: String, // "LBS" or "KG"
  val barId: String,
  val barName: String,
  val barWeight: Double,
  val customBarWeight: Double = 0.0,
  val collarsEnabled: Boolean = false,
  val platesSummary: String, // e.g. "45.0:2,25.0:1" (weight:quantity)
  val totalPlatesOnBar: Int,
  val isExact: Boolean
) {
  val weightUnit: WeightUnit
    get() = if (unit == "KG") WeightUnit.KG else WeightUnit.LBS

  fun formattedWeight(): String {
    return weightUnit.format(targetWeight)
  }

  fun parsePlates(): List<Pair<Double, Int>> {
    if (platesSummary.isBlank()) return emptyList()
    return try {
      platesSummary.split(",").mapNotNull { token ->
        val parts = token.trim().split(":")
        if (parts.size == 2) {
          val weight = parts[0].toDoubleOrNull()
          val qty = parts[1].toIntOrNull()
          if (weight != null && qty != null) Pair(weight, qty) else null
        } else null
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun getRelativeTimeSpan(): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    if (diff < 0) return "Just now"
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
      minutes < 1 -> "Just now"
      minutes < 60 -> "${minutes}m ago"
      hours < 24 -> "${hours}h ago"
      days < 7 -> "${days}d ago"
      else -> {
        val date = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
        date.format(java.util.Date(timestamp))
      }
    }
  }
}
