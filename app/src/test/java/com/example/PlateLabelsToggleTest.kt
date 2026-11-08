package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.BarbellCalculator
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
class PlateLabelsToggleTest {

  @Test
  fun testDefaultShowPlateLabelsIsTrue() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BarbellViewModel(application)

    // Verify initial default is true so weight labels are visible at a glance
    assertTrue(viewModel.uiState.value.showPlateLabels)
  }

  @Test
  fun testTogglePlateLabels() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BarbellViewModel(application)

    assertTrue(viewModel.uiState.value.showPlateLabels)

    // Toggle off
    viewModel.togglePlateLabels()
    assertFalse(viewModel.uiState.value.showPlateLabels)

    // Toggle back on
    viewModel.togglePlateLabels()
    assertTrue(viewModel.uiState.value.showPlateLabels)
  }

  @Test
  fun testSetShowPlateLabelsExplicitly() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BarbellViewModel(application)

    viewModel.setShowPlateLabels(false)
    assertFalse(viewModel.uiState.value.showPlateLabels)

    viewModel.setShowPlateLabels(true)
    assertTrue(viewModel.uiState.value.showPlateLabels)
  }

  @Test
  fun testPlateFormattedWeightsForVisualLabels() {
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 315.0,
      barWeight = 45.0,
      collarsWeight = 0.0,
      availablePlates = Plate.DEFAULT_LBS_PLATES,
      unit = WeightUnit.LBS
    )

    assertEquals(1, result.platesPerSide.size)
    assertEquals("45", result.platesPerSide[0].plate.formattedWeight)
    assertEquals(3, result.platesPerSide[0].quantity)

    // Test fractional plate labeling
    val resultFractional = BarbellCalculator.calculatePlates(
      targetWeight = 140.0,
      barWeight = 45.0,
      collarsWeight = 0.0,
      availablePlates = Plate.DEFAULT_LBS_PLATES,
      unit = WeightUnit.LBS
    )
    // 140 - 45 = 95 -> 47.5 per side -> 1x 45lb + 1x 2.5lb
    val plateWeights = resultFractional.platesPerSide.map { it.plate.formattedWeight }
    assertTrue(plateWeights.contains("45"))
    assertTrue(plateWeights.contains("2.5"))
  }
}
