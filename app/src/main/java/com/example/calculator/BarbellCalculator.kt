package com.example.calculator

import com.example.model.Plate
import com.example.model.PlateCalculationResult
import com.example.model.PlateCount
import com.example.model.WarmupSet
import com.example.model.WeightUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

object BarbellCalculator {

  private const val SCALE_FACTOR = 1000.0
  private const val MAX_SEARCH_NODES = 250_000

  fun calculatePlates(
    targetWeight: Double,
    barWeight: Double,
    collarsWeight: Double = 0.0,
    availablePlates: List<Plate>,
    unit: WeightUnit
  ): PlateCalculationResult {
    val baseWeight = barWeight + collarsWeight
    val roundedTarget = (targetWeight * 100.0).roundToInt() / 100.0
    val activePlates = availablePlates
      .filter { it.isEnabled && it.availablePairs > 0 && it.weight > 0.0 }
      .sortedByDescending { it.weight }

    if (roundedTarget <= baseWeight) {
      val isUnder = roundedTarget < baseWeight
      return PlateCalculationResult(
        targetWeight = roundedTarget,
        barWeight = barWeight,
        collarsWeight = collarsWeight,
        unit = unit,
        platesPerSide = emptyList(),
        totalPlatesOnBar = 0,
        plateWeightPerSide = 0.0,
        achievedTotalWeight = (baseWeight * 100.0).roundToInt() / 100.0,
        remainder = if (isUnder) ((roundedTarget - baseWeight) * 100.0).roundToInt() / 100.0 else 0.0,
        isExact = !isUnder,
        isUnderBarWeight = isUnder,
        hasActiveInventory = activePlates.isNotEmpty()
      )
    }

    if (activePlates.isEmpty()) {
      val cleanBase = (baseWeight * 100.0).roundToInt() / 100.0
      val cleanRemainder = ((roundedTarget - cleanBase) * 100.0).roundToInt() / 100.0
      return PlateCalculationResult(
        targetWeight = roundedTarget,
        barWeight = barWeight,
        collarsWeight = collarsWeight,
        unit = unit,
        platesPerSide = emptyList(),
        totalPlatesOnBar = 0,
        plateWeightPerSide = 0.0,
        achievedTotalWeight = cleanBase,
        remainder = cleanRemainder,
        isExact = false,
        isUnderBarWeight = false,
        hasActiveInventory = false
      )
    }

    val targetPerSideUnits = (((roundedTarget - baseWeight) / 2.0) * SCALE_FACTOR).roundToLong()
    val weights = LongArray(activePlates.size) { (activePlates[it].weight * SCALE_FACTOR).roundToLong() }
    val caps = IntArray(activePlates.size) { activePlates[it].availablePairs }
    val suffixMax = LongArray(activePlates.size + 1)
    for (i in activePlates.indices.reversed()) {
      suffixMax[i] = suffixMax[i + 1] + weights[i] * caps[i]
    }

    val bestCounts = IntArray(activePlates.size)
    if (suffixMax[0] <= targetPerSideUnits) {
      for (i in activePlates.indices) {
        bestCounts[i] = caps[i]
      }
    } else {
      val currentCounts = IntArray(activePlates.size)
      var bestSum = -1L
      var bestPlateCount = Int.MAX_VALUE
      var nodesVisited = 0

      fun search(idx: Int, currentSum: Long, currentPlates: Int) {
        if (nodesVisited >= MAX_SEARCH_NODES) return
        nodesVisited++

        if (currentSum > bestSum || (currentSum == bestSum && currentPlates < bestPlateCount)) {
          bestSum = currentSum
          bestPlateCount = currentPlates
          currentCounts.copyInto(bestCounts)
        }

        if (idx >= activePlates.size) return
        if (bestSum == targetPerSideUnits && currentPlates >= bestPlateCount) return
        if (currentSum + suffixMax[idx] < bestSum) return
        if (currentSum + suffixMax[idx] == bestSum && currentPlates >= bestPlateCount) return

        val plateWeight = weights[idx]
        if (plateWeight <= 0L) {
          search(idx + 1, currentSum, currentPlates)
          return
        }

        val maxTake = minOf(caps[idx], ((targetPerSideUnits - currentSum) / plateWeight).toInt())
        for (take in maxTake downTo 0) {
          if (nodesVisited >= MAX_SEARCH_NODES) break
          val nextPlates = currentPlates + take
          if (bestSum == targetPerSideUnits && nextPlates >= bestPlateCount) continue
          currentCounts[idx] = take
          search(idx + 1, currentSum + take * plateWeight, nextPlates)
        }
        currentCounts[idx] = 0
      }

      search(0, 0L, 0)
    }

    val resultCounts = mutableListOf<PlateCount>()
    for (i in activePlates.indices) {
      if (bestCounts[i] > 0) {
        resultCounts.add(PlateCount(plate = activePlates[i], quantity = bestCounts[i]))
      }
    }

    val plateWeightPerSide = resultCounts.sumOf { it.quantity * it.plate.weight }
    val achievedTotal = baseWeight + (plateWeightPerSide * 2.0)
    val cleanAchievedTotal = (achievedTotal * 100.0).roundToInt() / 100.0
    val remainder = roundedTarget - cleanAchievedTotal
    val cleanRemainder = (remainder * 100.0).roundToInt() / 100.0
    val isExact = abs(cleanRemainder) < 0.001
    val totalPlatesCount = resultCounts.sumOf { it.quantity * 2 }

    return PlateCalculationResult(
      targetWeight = roundedTarget,
      barWeight = barWeight,
      collarsWeight = collarsWeight,
      unit = unit,
      platesPerSide = resultCounts,
      totalPlatesOnBar = totalPlatesCount,
      plateWeightPerSide = (plateWeightPerSide * 100.0).roundToInt() / 100.0,
      achievedTotalWeight = cleanAchievedTotal,
      remainder = cleanRemainder,
      isExact = isExact,
      isUnderBarWeight = false,
      hasActiveInventory = true
    )
  }

  fun calculateReverseWeight(
    platesPerSide: List<PlateCount>,
    barWeight: Double,
    collarsWeight: Double = 0.0
  ): Double {
    val plateWeight = platesPerSide.sumOf { it.plate.weight * it.quantity }
    return ((barWeight + collarsWeight + (plateWeight * 2.0)) * 100.0).roundToInt() / 100.0
  }

  fun generateWarmupSets(
    workingWeight: Double,
    barWeight: Double,
    collarsWeight: Double = 0.0,
    availablePlates: List<Plate>,
    unit: WeightUnit
  ): List<WarmupSet> {
    val baseWeight = barWeight + collarsWeight
    if (workingWeight <= barWeight) {
      val singleResult = calculatePlates(workingWeight, barWeight, collarsWeight, availablePlates, unit)
      return listOf(
        WarmupSet(
          percentage = 100,
          title = "Working Set",
          reps = "Target",
          targetWeight = workingWeight,
          platesPerSide = singleResult.platesPerSide,
          achievedTotalWeight = singleResult.achievedTotalWeight,
          isExact = singleResult.isExact
        )
      )
    }

    val activePlates = availablePlates.filter { it.isEnabled && it.availablePairs > 0 && it.weight > 0.0 }
    val smallestPlateWeight = activePlates.minOfOrNull { it.weight } ?: 0.0

    val ladder = listOf(
      Triple(0, "Warm-up 1 (Empty Bar)", "10 reps"),
      Triple(40, "Warm-up 2 (40%)", "8 reps"),
      Triple(55, "Warm-up 3 (55%)", "5 reps"),
      Triple(70, "Warm-up 4 (70%)", "3 reps"),
      Triple(85, "Warm-up 5 (85%)", "1-2 reps"),
      Triple(100, "Working Set (100%)", "Target Reps")
    )

    return ladder.map { (percent, title, reps) ->
      if (percent == 0) {
        val result = calculatePlates(baseWeight, barWeight, collarsWeight, availablePlates, unit)
        WarmupSet(
          percentage = percent,
          title = title,
          reps = reps,
          targetWeight = result.achievedTotalWeight,
          platesPerSide = result.platesPerSide,
          achievedTotalWeight = result.achievedTotalWeight,
          isExact = result.isExact
        )
      } else if (percent == 100) {
        val result = calculatePlates(workingWeight, barWeight, collarsWeight, availablePlates, unit)
        WarmupSet(
          percentage = percent,
          title = title,
          reps = reps,
          targetWeight = workingWeight,
          platesPerSide = result.platesPerSide,
          achievedTotalWeight = result.achievedTotalWeight,
          isExact = result.isExact
        )
      } else {
        val rawWeight = maxOf(baseWeight, workingWeight * (percent / 100.0))
        val lowerResult = calculatePlates(rawWeight, barWeight, collarsWeight, availablePlates, unit)
        val upperProbe = rawWeight + (smallestPlateWeight * 2.0)
        val upperResult = if (smallestPlateWeight > 0.0 && upperProbe <= workingWeight) {
          calculatePlates(upperProbe, barWeight, collarsWeight, availablePlates, unit)
        } else {
          lowerResult
        }

        val chosen = if (
          abs(upperResult.achievedTotalWeight - rawWeight) < abs(lowerResult.achievedTotalWeight - rawWeight)
        ) {
          upperResult
        } else {
          lowerResult
        }

        WarmupSet(
          percentage = percent,
          title = title,
          reps = reps,
          targetWeight = chosen.achievedTotalWeight,
          platesPerSide = chosen.platesPerSide,
          achievedTotalWeight = chosen.achievedTotalWeight,
          isExact = true
        )
      }
    }
  }
}
