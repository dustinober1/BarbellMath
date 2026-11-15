package com.example

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.repository.LoadingPlanRepository
import com.example.data.repository.PlateInventoryRepository
import com.example.data.repository.WorkoutSessionRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.ui.BarbellAppScreen
import com.example.ui.BarbellViewModel
import com.example.ui.theme.BarbellTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class InventorySettingsUiTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private lateinit var database: AppDatabase
  private lateinit var viewModelStore: ViewModelStore
  private lateinit var viewModel: BarbellViewModel

  @Before
  fun setUp() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    database = Room.inMemoryDatabaseBuilder(application, AppDatabase::class.java)
      .allowMainThreadQueries()
      .setQueryExecutor { it.run() }
      .setTransactionExecutor { it.run() }
      .build()
    viewModelStore = ViewModelStore()
    val factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BarbellViewModel(
          application = application,
          customRepository = LoadingPlanRepository(database.loadingPlanDao()),
          customPlateRepository = PlateInventoryRepository(database.plateInventoryDao()),
          customSessionRepository = WorkoutSessionRepository(database.workoutSessionDao())
        ) as T
      }
    }
    viewModel = ViewModelProvider(viewModelStore, factory)[BarbellViewModel::class.java]
  }

  @After
  fun tearDown() {
    viewModelStore.clear()
    database.close()
  }

  @Test
  fun excluding35LbPlatesInSettingsUpdatesCalculatorAndStatusChip() {
    composeTestRule.setContent {
      BarbellTheme {
        BarbellAppScreen(viewModel = viewModel)
      }
    }
    composeTestRule.waitForIdle()

    // Set target weight to 115 lbs (35 lbs per side)
    viewModel.setTargetWeight(115.0)
    composeTestRule.waitForIdle()
    assertEquals(listOf(35.0), viewModel.uiState.value.calculationResult.platesPerSide.map { it.plate.weight })

    // Navigate to Home Gym Settings via the breakdown gym settings chip
    composeTestRule.onNodeWithTag("main_lazy_column")
      .performScrollToNode(hasTestTag("breakdown_gym_settings_chip"))
    composeTestRule.onNodeWithTag("breakdown_gym_settings_chip").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    assertEquals(4, viewModel.uiState.value.activeTab)
    composeTestRule.onNodeWithTag("inventory_settings_screen").assertIsDisplayed()

    // Toggle off 35 lb plate using its switch
    composeTestRule.onNodeWithTag("switch_plate_35").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      !viewModel.uiState.value.lbsPlates.first { it.weight == 35.0 }.isEnabled
    }
    composeTestRule.waitForIdle()

    assertFalse(viewModel.uiState.value.lbsPlates.first { it.weight == 35.0 }.isEnabled)

    // Return to Calculator tab and verify 115 lbs now suggests 25 + 10 per side
    composeTestRule.onNodeWithTag("nav_tab_calculator").performClick()
    composeTestRule.waitForIdle()
    assertEquals(0, viewModel.uiState.value.activeTab)
    assertEquals(
      listOf(25.0, 10.0),
      viewModel.uiState.value.calculationResult.platesPerSide.map { it.plate.weight }
    )

    composeTestRule.onNodeWithTag("main_lazy_column")
      .performScrollToNode(hasTestTag("breakdown_gym_settings_chip"))
    composeTestRule.onNodeWithTag("breakdown_gym_settings_chip")
      .assertTextContains("35lbs off", substring = true)
  }

  @Test
  fun enablingFractionalPlateInSettingsAllowsExactMicroLoadCalculation() {
    composeTestRule.setContent {
      BarbellTheme {
        BarbellAppScreen(viewModel = viewModel)
      }
    }
    composeTestRule.waitForIdle()

    // Target 136 lbs = 45 bar + 2 * (45 + 0.5). With 0.5 lb initially disabled, it is not exact.
    viewModel.setTargetWeight(136.0)
    composeTestRule.waitForIdle()
    assertFalse(viewModel.uiState.value.calculationResult.isExact)

    // Open Settings tab and enable 0.5 lb fractional plate
    composeTestRule.onNodeWithTag("nav_tab_settings").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("switch_plate_0.5").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      viewModel.uiState.value.lbsPlates.first { it.weight == 0.5 }.isEnabled
    }
    composeTestRule.waitForIdle()

    assertTrue(viewModel.uiState.value.lbsPlates.first { it.weight == 0.5 }.isEnabled)
    assertTrue(viewModel.uiState.value.calculationResult.isExact)
    assertEquals(
      listOf(45.0, 0.5),
      viewModel.uiState.value.calculationResult.platesPerSide.map { it.plate.weight }
    )
  }
}
