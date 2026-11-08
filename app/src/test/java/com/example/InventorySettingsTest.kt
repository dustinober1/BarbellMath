package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.BarbellCalculator
import com.example.model.BarType
import com.example.model.Plate
import com.example.model.WeightUnit
import com.example.ui.BarbellViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class InventorySettingsTest {

  @Test
  fun testExclude35LbPlatesForces25Plus10() {
    // Target 115 lbs total on 45 lb bar:
    // Plate weight per side needed = (115 - 45) / 2 = 35 lbs per side.
    // With 35s enabled, uses 1x 35lb plate per side.
    val platesWith35s = Plate.DEFAULT_LBS_PLATES
    val resultWith35s = BarbellCalculator.calculatePlates(
      targetWeight = 115.0,
      barWeight = 45.0,
      collarsWeight = 0.0,
      availablePlates = platesWith35s,
      unit = WeightUnit.LBS
    )
    assertTrue(resultWith35s.isExact)
    assertEquals(1, resultWith35s.platesPerSide.size)
    assertEquals(35.0, resultWith35s.platesPerSide[0].plate.weight, 0.001)

    // With 35s EXCLUDED:
    val platesWithout35s = platesWith35s.map {
      if (it.weight == 35.0) it.copy(isEnabled = false) else it
    }
    val resultWithout35s = BarbellCalculator.calculatePlates(
      targetWeight = 115.0,
      barWeight = 45.0,
      collarsWeight = 0.0,
      availablePlates = platesWithout35s,
      unit = WeightUnit.LBS
    )
    assertTrue(resultWithout35s.isExact)
    assertEquals(115.0, resultWithout35s.achievedTotalWeight, 0.001)

    // Must NOT contain 35lb plate!
    assertFalse(resultWithout35s.platesPerSide.any { it.plate.weight == 35.0 })

    // Must be 1x 25lb plate + 1x 10lb plate = 35 lbs per side
    assertEquals(2, resultWithout35s.platesPerSide.size)
    assertEquals(25.0, resultWithout35s.platesPerSide[0].plate.weight, 0.001)
    assertEquals(1, resultWithout35s.platesPerSide[0].quantity)
    assertEquals(10.0, resultWithout35s.platesPerSide[1].plate.weight, 0.001)
    assertEquals(1, resultWithout35s.platesPerSide[1].quantity)
  }

  @Test
  fun testTarget205LbsWithout35LbPlates() {
    // 205 lbs on 45 lb bar = 160 lbs plates total = 80 lbs per side.
    // Normal: 1x 45lb + 1x 35lb = 80 lbs.
    // Without 35s: 1x 45lb + 1x 25lb + 1x 10lb = 80 lbs.
    val platesWithout35s = Plate.DEFAULT_LBS_PLATES.map {
      if (it.weight == 35.0) it.copy(isEnabled = false) else it
    }
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 205.0,
      barWeight = 45.0,
      collarsWeight = 0.0,
      availablePlates = platesWithout35s,
      unit = WeightUnit.LBS
    )
    assertTrue(result.isExact)
    assertEquals(205.0, result.achievedTotalWeight, 0.001)
    assertFalse(result.platesPerSide.any { it.plate.weight == 35.0 })

    val plateMap = result.platesPerSide.associate { it.plate.weight to it.quantity }
    assertEquals(1, plateMap[45.0])
    assertEquals(1, plateMap[25.0])
    assertEquals(1, plateMap[10.0])
  }

  @Test
  fun testViewModelExclude35LbPlatesPreset() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BarbellViewModel(application)

    viewModel.setTargetWeight(115.0)
    // Initially 35s are enabled by default
    assertTrue(viewModel.uiState.value.lbsPlates.first { it.weight == 35.0 }.isEnabled)
    assertEquals(35.0, viewModel.uiState.value.calculationResult.platesPerSide[0].plate.weight, 0.001)

    // Trigger exclude 35s
    viewModel.exclude35LbPlates()

    val state = viewModel.uiState.value
    assertFalse(state.lbsPlates.first { it.weight == 35.0 }.isEnabled)
    assertFalse(state.calculationResult.platesPerSide.any { it.plate.weight == 35.0 })
    assertEquals(115.0, state.calculationResult.achievedTotalWeight, 0.001)
  }

  @Test
  fun testTogglePlateIndividualDenomination() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BarbellViewModel(application)

    // Toggle off 25 lb plates
    viewModel.togglePlateEnabled(25.0)
    assertFalse(viewModel.uiState.value.lbsPlates.first { it.weight == 25.0 }.isEnabled)

    // Toggle back on
    viewModel.togglePlateEnabled(25.0)
    assertTrue(viewModel.uiState.value.lbsPlates.first { it.weight == 25.0 }.isEnabled)
  }
}
