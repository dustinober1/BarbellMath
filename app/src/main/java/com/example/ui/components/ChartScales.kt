package com.example.ui.components

import com.example.data.repository.ChartPoint
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * D3-inspired linear scale that snaps domain bounds and tick intervals
 * to clean, human-readable numeric increments (1, 2, 2.5, 5, 10 * 10^n).
 */
data class LinearChartScale(
  val domainMin: Double,
  val domainMax: Double,
  val ticks: List<Double>
) {
  val span: Double = (domainMax - domainMin).coerceAtLeast(1.0)

  fun yFor(value: Double, plotBottom: Float, plotHeight: Float): Float {
    val normalized = ((value - domainMin) / span).toFloat().coerceIn(0f, 1f)
    return plotBottom - normalized * plotHeight
  }

  companion object {
    fun nice(rawMin: Double, rawMax: Double, desiredTickCount: Int = 5): LinearChartScale {
      val safeMin = minOf(rawMin, rawMax)
      val safeMax = maxOf(rawMin, rawMax)
      val rawSpan = (safeMax - safeMin).let { if (it < 0.5) maxOf(10.0, safeMax * 0.2) else it }
      val paddedMin = maxOf(0.0, safeMin - rawSpan * 0.12)
      val paddedMax = safeMax + rawSpan * 0.16

      val step = niceStep((paddedMax - paddedMin) / (desiredTickCount - 1).coerceAtLeast(1))
      val niceMin = maxOf(0.0, floor(paddedMin / step) * step)
      val niceMax = ceil(paddedMax / step) * step

      val generatedTicks = mutableListOf<Double>()
      var current = niceMin
      var guard = 0
      while (current <= niceMax + step * 0.001 && guard < 12) {
        generatedTicks.add((current * 100.0).let { kotlin.math.round(it) / 100.0 })
        current += step
        guard++
      }
      if (generatedTicks.size < 2) {
        generatedTicks.clear()
        generatedTicks.add(niceMin)
        generatedTicks.add(niceMin + step)
      }

      return LinearChartScale(
        domainMin = generatedTicks.first(),
        domainMax = generatedTicks.last(),
        ticks = generatedTicks
      )
    }

    private fun niceStep(roughStep: Double): Double {
      val safeStep = roughStep.coerceAtLeast(0.5)
      val exponent = floor(log10(safeStep))
      val power = 10.0.pow(exponent)
      val fraction = safeStep / power
      val niceFraction = when {
        fraction <= 1.0 -> 1.0
        fraction <= 2.0 -> 2.0
        fraction <= 2.5 -> 2.5
        fraction <= 5.0 -> 5.0
        else -> 10.0
      }
      return niceFraction * power
    }
  }
}

/**
 * D3-inspired time scale mapping chronological session timestamps onto Canvas X coordinates,
 * with smart clustering prevention if multiple sessions share identical timestamps.
 */
class TimeChartScale(
  points: List<ChartPoint>,
  leftPad: Float,
  plotWidth: Float
) {
  val xPositions: List<Float>

  init {
    xPositions = when {
      points.isEmpty() -> emptyList()
      points.size == 1 -> listOf(leftPad + plotWidth / 2f)
      else -> {
        val minTs = points.minOf { it.session.timestamp }
        val maxTs = points.maxOf { it.session.timestamp }
        val timeSpan = maxTs - minTs
        if (timeSpan <= 0L) {
          points.indices.map { idx ->
            leftPad + (idx.toFloat() / (points.size - 1).toFloat()) * plotWidth
          }
        } else {
          // Blend 85% proportional time with 15% rank order if adjacent timestamps are nearly identical
          // so same-day sessions remain distinct and individually tappable while preserving time gaps.
          val rawXs = points.mapIndexed { idx, pt ->
            val timeFrac = ((pt.session.timestamp - minTs).toDouble() / timeSpan.toDouble()).toFloat()
            val indexFrac = idx.toFloat() / (points.size - 1).toFloat()
            val minGapNeeded = points.zipWithNext().any { (a, b) ->
              abs(b.session.timestamp - a.session.timestamp) < 60_000L
            }
            val blended = if (minGapNeeded) timeFrac * 0.85f + indexFrac * 0.15f else timeFrac
            leftPad + blended.coerceIn(0f, 1f) * plotWidth
          }
          rawXs
        }
      }
    }
  }

  fun xFor(index: Int): Float = xPositions.getOrElse(index) { 0f }

  fun nearestIndex(touchX: Float): Int {
    if (xPositions.isEmpty()) return 0
    var bestIdx = 0
    var bestDist = Float.MAX_VALUE
    xPositions.forEachIndexed { idx, x ->
      val d = abs(x - touchX)
      if (d < bestDist) {
        bestDist = d
        bestIdx = idx
      }
    }
    return bestIdx
  }
}

/**
 * Identifies all indices in [points] that represent a Personal Record milestone,
 * ensuring the all-time highest point in the visible series is always highlighted even
 * if no subsequent session exceeded it.
 */
fun resolvePrMilestoneIndices(points: List<ChartPoint>): Set<Int> {
  if (points.isEmpty()) return emptySet()
  val flagged = points.mapIndexedNotNull { index, point ->
    if (point.isPersonalBest) index else null
  }.toMutableSet()

  if (flagged.isEmpty() && points.isNotEmpty()) {
    val maxWeight = points.maxOf { it.weight }
    val firstPeakIdx = points.indexOfFirst { abs(it.weight - maxWeight) < 0.001 }
    if (firstPeakIdx >= 0) {
      flagged.add(firstPeakIdx)
    }
  }
  return flagged
}
