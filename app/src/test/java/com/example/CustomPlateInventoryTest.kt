package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.BarbellCalculator
import com.example.data.AppDatabase
import com.example.data.repository.LoadingPlanRepository
import com.example.data.repository.PlateInventoryRepository
import com.example.model.Plate
import com.example.model.WeightUnit
import com.example.ui.BarbellViewModel
import com.example.ui.theme.PlateCyan
import com.example.ui.theme.PlatePurple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CustomPlateInventoryTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var database: AppDatabase
  private lateinit var plateRepository: PlateInventoryRepository
  private lateinit var planRepository: LoadingPlanRepository

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    plateRepository = PlateInventoryRepository(database.plateInventoryDao())
    planRepository = LoadingPlanRepository(database.loadingPlanDao())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    database.close()
  }

  @Test
  fun testOptimalSolverBeatsGreedyWithLimitedPairs() {
    // 145 lbs on 45 lb bar = 50 lbs per side.
    // Plates available: 45 lb (1 pair), 25 lb (2 pairs), 10 lb (1 pair).
    // Greedy picks 1x 45 and stalls at 45; optimal solver finds 2x 25 = 50 exact.
    val limitedPlates = listOf(
      Plate.createCustom(45.0, WeightUnit.LBS, Color.Red, availablePairs = 1),
      Plate.createCustom(25.0, WeightUnit.LBS, Color.Yellow, availablePairs = 2),
      Plate.createCustom(10.0, WeightUnit.LBS, Color.Green, availablePairs = 1)
    )

    val result = BarbellCalculator.calculatePlates(
      targetWeight = 145.0,
      barWeight = 45.0,
      availablePlates = limitedPlates,
      unit = WeightUnit.LBS
    )

    assertTrue(result.isExact)
    assertEquals(145.0, result.achievedTotalWeight, 0.001)
    assertEquals(1, result.platesPerSide.size)
    assertEquals(25.0, result.platesPerSide[0].plate.weight, 0.001)
    assertEquals(2, result.platesPerSide[0].quantity)
  }

  @Test
  fun testOptimalSolverWithCustomFractionalPlates() {
    // Custom KG fractional plates: 20, 1.25, 0.5
    // Target: 63.5 kg on 20 kg bar -> 21.75 kg per side = 20 + 1.25 + 0.5
    val customKgPlates = listOf(
      Plate.createCustom(20.0, WeightUnit.KG, Color.Blue, availablePairs = 2),
      Plate.createCustom(1.25, WeightUnit.KG, PlatePurple, availablePairs = 1),
      Plate.createCustom(0.5, WeightUnit.KG, PlateCyan, availablePairs = 1)
    )

    val result = BarbellCalculator.calculatePlates(
      targetWeight = 63.5,
      barWeight = 20.0,
      availablePlates = customKgPlates,
      unit = WeightUnit.KG
    )

    assertTrue(result.isExact)
    assertEquals(63.5, result.achievedTotalWeight, 0.001)
    assertEquals(listOf(20.0, 1.25, 0.5), result.platesPerSide.map { it.plate.weight })
  }

  @Test
  fun testClosestMatchFallbackWhenExactImpossible() {
    val scarcePlates = listOf(
      Plate.createCustom(20.0, WeightUnit.KG, Color.Blue, availablePairs = 1),
      Plate.createCustom(10.0, WeightUnit.KG, Color.Green, availablePairs = 1)
    )

    // 90 kg on 20 kg bar needs 35 kg/side, max available is 30 kg/side -> 80 kg total
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 90.0,
      barWeight = 20.0,
      availablePlates = scarcePlates,
      unit = WeightUnit.KG
    )

    assertFalse(result.isExact)
    assertTrue(result.hasActiveInventory)
    assertEquals(80.0, result.achievedTotalWeight, 0.001)
    assertEquals(10.0, result.remainder, 0.001)
  }

  @Test
  fun testEmptyActiveInventoryHandledGracefully() {
    val disabledPlates = Plate.DEFAULT_LBS_PLATES.map { it.copy(isEnabled = false) }
    val result = BarbellCalculator.calculatePlates(
      targetWeight = 135.0,
      barWeight = 45.0,
      availablePlates = disabledPlates,
      unit = WeightUnit.LBS
    )

    assertFalse(result.hasActiveInventory)
    assertFalse(result.isExact)
    assertEquals(45.0, result.achievedTotalWeight, 0.001)
    assertTrue(result.platesPerSide.isEmpty())
  }

  @Test
  fun testWarmupSetsRespectCustomPlateInventory() {
    // Only 45 lb and 25 lb plates available (no 10s, 5s, 2.5s)
    val bigPlatesOnly = listOf(
      Plate.createCustom(45.0, WeightUnit.LBS, Color.Red, availablePairs = 4),
      Plate.createCustom(25.0, WeightUnit.LBS, Color.Yellow, availablePairs = 2)
    )

    val warmups = BarbellCalculator.generateWarmupSets(
      workingWeight = 225.0,
      barWeight = 45.0,
      availablePlates = bigPlatesOnly,
      unit = WeightUnit.LBS
    )

    // Every plate used in warmups must belong to {45.0, 25.0}
    warmups.forEach { set ->
      set.platesPerSide.forEach { count ->
        assertTrue(count.plate.weight in listOf(45.0, 25.0))
      }
    }
  }

  @Test
  fun testRoomRepositorySeedsAndPersistsCustomPlateCrud() = runTest {
    plateRepository.ensureSeeded()

    val initialLbs = plateRepository.lbsPlates.first()
    assertEquals(Plate.DEFAULT_LBS_PLATES.size, initialLbs.size)

    // Add a custom 0.25 lb fractional plate
    val addResult = plateRepository.addCustomPlate(
      weight = 0.25,
      unit = WeightUnit.LBS,
      color = PlateCyan,
      availablePairs = 2
    )
    assertTrue(addResult.isSuccess)
    val created = addResult.getOrThrow()
    assertTrue(created.isCustom)
    assertEquals(0.25, created.weight, 0.001)
    assertEquals(2, created.availablePairs)

    // Reject duplicate weight in same unit
    val duplicateResult = plateRepository.addCustomPlate(
      weight = 0.25,
      unit = WeightUnit.LBS,
      color = PlatePurple,
      availablePairs = 1
    )
    assertTrue(duplicateResult.isFailure)

    // Update custom plate weight and pair count
    val updateResult = plateRepository.updateCustomPlate(
      plateId = created.id,
      originalWeight = 0.25,
      newWeight = 0.35,
      unit = WeightUnit.LBS,
      color = PlatePurple,
      availablePairs = 3,
      isEnabled = true
    )
    assertTrue(updateResult.isSuccess)

    val updatedList = plateRepository.lbsPlates.first()
    val updatedPlate = updatedList.firstOrNull { it.id == created.id }
    assertNotNull(updatedPlate)
    assertEquals(0.35, updatedPlate!!.weight, 0.001)
    assertEquals(3, updatedPlate.availablePairs)

    // Delete custom plate
    plateRepository.deleteCustomPlate(updatedPlate)
    val afterDelete = plateRepository.lbsPlates.first()
    assertNull(afterDelete.firstOrNull { it.id == created.id })
  }

  @Test
  fun testViewModelCustomPlateAddEditDeleteRecalculatesTarget() = runTest {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BarbellViewModel(application, planRepository, plateRepository)

    // Target 135.5 lbs on 45 lb bar -> 45.25 lbs per side.
    // Without a 0.25 lb plate, 135.5 is not exact (closest is 135.0)
    viewModel.setTargetWeight(135.5)
    assertFalse(viewModel.uiState.value.calculationResult.isExact)

    // Add custom 0.25 lb plate
    val err = viewModel.addCustomPlate(weight = 0.25, color = PlateCyan, availablePairs = 1)
    assertNull(err)

    // Now 135.5 lbs is an exact match using 45 + 0.25 per side!
    val resultAfterAdd = viewModel.uiState.value.calculationResult
    assertTrue(resultAfterAdd.isExact)
    assertEquals(135.5, resultAfterAdd.achievedTotalWeight, 0.001)
    assertTrue(resultAfterAdd.platesPerSide.any { it.plate.weight == 0.25 })

    // Duplicate weight should return a validation error string
    val dupErr = viewModel.addCustomPlate(weight = 0.25, color = PlatePurple, availablePairs = 1)
    assertNotNull(dupErr)

    // Delete custom plate -> reverts to closest match 135.0
    val customPlate = viewModel.uiState.value.lbsPlates.first { it.weight == 0.25 }
    viewModel.deleteCustomPlate(customPlate)
    assertFalse(viewModel.uiState.value.calculationResult.isExact)
    assertEquals(135.0, viewModel.uiState.value.calculationResult.achievedTotalWeight, 0.001)
  }
}
