package com.example.calculator

import com.example.model.Plate
import com.example.model.PlateCalculationResult
import com.example.model.PlateCount
import com.example.model.WarmupSet
import com.example.model.WeightUnit
import kotlin.math.abs
import kotlin.math.roundToInt

object BarbellCalculator {

  fun calculatePlates(
    targetWeight: Double,
    barWeight: Double,
    collarsWeight: Double = 0.0,
    availablePlates: List<Plate>,
    unit: WeightUnit
  ): PlateCalculationResult {
    val baseWeight = barWeight + collarsWeight
    val roundedTarget = (targetWeight * 100).roundToInt() / 100.0

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
        achievedTotalWeight = baseWeight,
        remainder = if (isUnder) roundedTarget - baseWeight else 0.0,
        isExact = !isUnder,
        isUnderBarWeight = isUnder
      )
    }

    val neededPlatesTotal = roundedTarget - baseWeight
    var remainingPerSide = neededPlatesTotal / 2.0

    // Filter enabled plates and sort largest first
    val sortedPlates = availablePlates
      .filter { it.isEnabled && it.availablePairs > 0 }
      .sortedByDescending { it.weight }

    val resultCounts = mutableListOf<PlateCount>()

    for (plate in sortedPlates) {
      if (remainingPerSide <= 0.0001) break
      val countPossible = ((remainingPerSide + 0.0001) / plate.weight).toInt()
      val countToUse = minOf(countPossible, plate.availablePairs)

      if (countToUse > 0) {
        resultCounts.add(PlateCount(plate = plate, quantity = countToUse))
        remainingPerSide -= (countToUse * plate.weight)
        // Clean floating point errors
        remainingPerSide = (remainingPerSide * 10000.0).roundToInt() / 10000.0
      }
    }

    val plateWeightPerSide = resultCounts.sumOf { it.quantity * it.plate.weight }
    val achievedTotal = baseWeight + (plateWeightPerSide * 2.0)
    val remainder = (roundedTarget - achievedTotal)
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
      achievedTotalWeight = (achievedTotal * 100.0).roundToInt() / 100.0,
      remainder = cleanRemainder,
      isExact = isExact,
      isUnderBarWeight = false
    )
  }

  fun calculateReverseWeight(
    platesPerSide: List<PlateCount>,
    barWeight: Double,
    collarsWeight: Double = 0.0
  ): Double {
    val plateWeight = platesPerSide.sumOf { it.plate.weight * it.quantity }
    return barWeight + collarsWeight + (plateWeight * 2.0)
  }

  fun generateWarmupSets(
    workingWeight: Double,
    barWeight: Double,
    collarsWeight: Double = 0.0,
    availablePlates: List<Plate>,
    unit: WeightUnit
  ): List<WarmupSet> {
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

    val ladder = listOf(
      Triple(0, "Warm-up 1 (Empty Bar)", "10 reps"),
      Triple(40, "Warm-up 2 (40%)", "8 reps"),
      Triple(55, "Warm-up 3 (55%)", "5 reps"),
      Triple(70, "Warm-up 4 (70%)", "3 reps"),
      Triple(85, "Warm-up 5 (85%)", "1-2 reps"),
      Triple(100, "Working Set (100%)", "Target Reps")
    )

    return ladder.map { (percent, title, reps) ->
      val rawWeight = if (percent == 0) {
        barWeight + collarsWeight
      } else {
        maxOf(barWeight + collarsWeight, (workingWeight * (percent / 100.0)))
      }

      // Round raw weight to reasonable plate increments (e.g. nearest 2.5 or 5)
      val step = if (unit == WeightUnit.LBS) 2.5 else 1.0
      val rounded = (rawWeight / step).roundToInt() * step
      val result = calculatePlates(rounded, barWeight, collarsWeight, availablePlates, unit)

      WarmupSet(
        percentage = percent,
        title = title,
        reps = reps,
        targetWeight = rounded,
        platesPerSide = result.platesPerSide,
        achievedTotalWeight = result.achievedTotalWeight,
        isExact = result.isExact
      )
    }
  }
}
