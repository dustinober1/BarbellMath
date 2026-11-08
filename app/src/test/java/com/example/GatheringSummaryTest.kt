package com.example

import com.example.calculator.BarbellCalculator
import com.example.model.Plate
import com.example.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GatheringSummaryTest {

  @Test
  fun testGatheringCountsFor225Lbs() {
    // 225 lbs on standard 45 lb bar:
    // 90 lbs per side = 2x 45lb plates per side
    // Total plates to gather across barbell = 4x 45lb plates
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 225.0,
      barWeight = 45.0,
      collarsWeight = 0.0,
      availablePlates = Plate.DEFAULT_LBS_PLATES,
      unit = WeightUnit.LBS
    )

    assertEquals(4, result.totalPlatesOnBar)
    assertEquals(1, result.platesPerSide.size)
    assertEquals(45.0, result.platesPerSide[0].plate.weight, 0.001)
    assertEquals(2, result.platesPerSide[0].quantity) // per side

    // Total count for this plate type to gather = quantity * 2
    val totalToGather45s = result.platesPerSide[0].quantity * 2
    assertEquals(4, totalToGather45s)
  }

  @Test
  fun testGatheringCountsFor185Lbs() {
    // 185 lbs on 45 lb bar:
    // 70 lbs per side = 1x 45lb + 1x 25lb per side
    // Total plates to gather = 2x 45lb + 2x 25lb = 4 plates total
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 185.0,
      barWeight = 45.0,
      collarsWeight = 0.0,
      availablePlates = Plate.DEFAULT_LBS_PLATES,
      unit = WeightUnit.LBS
    )

    assertEquals(4, result.totalPlatesOnBar)
    assertEquals(2, result.platesPerSide.size)

    val mapTotalToGather = result.platesPerSide.associate { it.plate.weight to it.quantity * 2 }
    assertEquals(2, mapTotalToGather[45.0])
    assertEquals(2, mapTotalToGather[25.0])
  }

  @Test
  fun testGatheringCountsFor315Lbs() {
    // 315 lbs on 45 lb bar:
    // 135 lbs per side = 3x 45lb per side
    // Total plates to gather = 6x 45lb = 6 plates total
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 315.0,
      barWeight = 45.0,
      collarsWeight = 0.0,
      availablePlates = Plate.DEFAULT_LBS_PLATES,
      unit = WeightUnit.LBS
    )

    assertEquals(6, result.totalPlatesOnBar)
    val mapTotalToGather = result.platesPerSide.associate { it.plate.weight to it.quantity * 2 }
    assertEquals(6, mapTotalToGather[45.0])
  }
}
