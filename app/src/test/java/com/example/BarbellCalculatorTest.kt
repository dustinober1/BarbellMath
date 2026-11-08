package com.example

import com.example.calculator.BarbellCalculator
import com.example.model.BarType
import com.example.model.Plate
import com.example.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BarbellCalculatorTest {

  @Test
  fun test135LbsOnStandardOlympicBar() {
    // 135 lbs total on 45 lb bar = 90 lbs plates = 45 lbs per side = 1x 45lb plate per side
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 135.0,
      barWeight = 45.0,
      collarsWeight = 0.0,
      availablePlates = Plate.DEFAULT_LBS_PLATES,
      unit = WeightUnit.LBS
    )

    assertTrue(result.isExact)
    assertEquals(135.0, result.achievedTotalWeight, 0.001)
    assertEquals(45.0, result.plateWeightPerSide, 0.001)
    assertEquals(1, result.platesPerSide.size)
    assertEquals(45.0, result.platesPerSide[0].plate.weight, 0.001)
    assertEquals(1, result.platesPerSide[0].quantity)
    assertEquals(2, result.totalPlatesOnBar)
  }

  @Test
  fun test225LbsOnStandardOlympicBar() {
    // 225 lbs total on 45 lb bar = 180 lbs plates = 90 lbs per side = 2x 45lb plate per side
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 225.0,
      barWeight = 45.0,
      collarsWeight = 0.0,
      availablePlates = Plate.DEFAULT_LBS_PLATES,
      unit = WeightUnit.LBS
    )

    assertTrue(result.isExact)
    assertEquals(225.0, result.achievedTotalWeight, 0.001)
    assertEquals(90.0, result.plateWeightPerSide, 0.001)
    assertEquals(1, result.platesPerSide.size)
    assertEquals(45.0, result.platesPerSide[0].plate.weight, 0.001)
    assertEquals(2, result.platesPerSide[0].quantity)
    assertEquals(4, result.totalPlatesOnBar)
  }

  @Test
  fun test315LbsOnStandardOlympicBar() {
    // 315 lbs = 3x 45lb plate per side
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 315.0,
      barWeight = 45.0,
      collarsWeight = 0.0,
      availablePlates = Plate.DEFAULT_LBS_PLATES,
      unit = WeightUnit.LBS
    )

    assertTrue(result.isExact)
    assertEquals(315.0, result.achievedTotalWeight, 0.001)
    assertEquals(135.0, result.plateWeightPerSide, 0.001)
    assertEquals(3, result.platesPerSide[0].quantity)
  }

  @Test
  fun test185LbsWithOlympicBar() {
    // 185 lbs on 45 lb bar = 140 lbs plates = 70 lbs per side = 1x45, 1x25 per side
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 185.0,
      barWeight = 45.0,
      collarsWeight = 0.0,
      availablePlates = Plate.DEFAULT_LBS_PLATES,
      unit = WeightUnit.LBS
    )

    assertTrue(result.isExact)
    assertEquals(185.0, result.achievedTotalWeight, 0.001)
    assertEquals(70.0, result.plateWeightPerSide, 0.001)
    assertEquals(2, result.platesPerSide.size)
    assertEquals(45.0, result.platesPerSide[0].plate.weight, 0.001)
    assertEquals(1, result.platesPerSide[0].quantity)
    assertEquals(25.0, result.platesPerSide[1].plate.weight, 0.001)
    assertEquals(1, result.platesPerSide[1].quantity)
  }

  @Test
  fun testKgCalculation100KgOn20KgBar() {
    // 100 kg on 20 kg bar = 80 kg plates = 40 kg per side = 1x25 kg, 1x15 kg (or 1x25, 1x10, 1x5)
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 100.0,
      barWeight = 20.0,
      collarsWeight = 0.0,
      availablePlates = Plate.DEFAULT_KG_PLATES,
      unit = WeightUnit.KG
    )

    assertTrue(result.isExact)
    assertEquals(100.0, result.achievedTotalWeight, 0.001)
    assertEquals(40.0, result.plateWeightPerSide, 0.001)
  }

  @Test
  fun testWomenBarbell35Lbs() {
    // Target 95 lbs on 35 lb bar = 60 lbs plates = 30 lbs per side = 1x25, 1x5 per side
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 95.0,
      barWeight = BarType.OLYMPIC_WOMENS.weightLbs,
      collarsWeight = 0.0,
      availablePlates = Plate.DEFAULT_LBS_PLATES,
      unit = WeightUnit.LBS
    )

    assertTrue(result.isExact)
    assertEquals(95.0, result.achievedTotalWeight, 0.001)
    assertEquals(30.0, result.plateWeightPerSide, 0.001)
  }
}
