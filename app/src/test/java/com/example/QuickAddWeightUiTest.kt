package com.example

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.repository.LoadingPlanRepository
import com.example.data.repository.PlateInventoryRepository
import com.example.data.repository.WorkoutSessionRepository
import com.example.model.WeightUnit
import com.example.ui.BarbellAppScreen
import com.example.ui.BarbellViewModel
import com.example.ui.theme.BarbellTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class QuickAddWeightUiTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private lateinit var database: AppDatabase
  private lateinit var viewModel: BarbellViewModel

  @Before
  fun setUp() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    database = Room.inMemoryDatabaseBuilder(application, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    viewModel = BarbellViewModel(
      application = application,
      customRepository = LoadingPlanRepository(database.loadingPlanDao()),
      customPlateRepository = PlateInventoryRepository(database.plateInventoryDao()),
      customSessionRepository = WorkoutSessionRepository(database.workoutSessionDao())
    )
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun quickAddButtonsAppearOnMainScreenAndUpdateTargetWeight() {
    composeTestRule.setContent {
      BarbellTheme {
        BarbellAppScreen(viewModel = viewModel)
      }
    }
    composeTestRule.waitForIdle()

    // Initial target weight is 135 lbs
    assertEquals(135.0, viewModel.uiState.value.targetWeight, 0.001)

    composeTestRule.onNodeWithTag("quick_add_section").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add 10lb").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add 25lb").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add 90lb").assertIsDisplayed()

    // Tap Add 10lb -> 145 lbs
    composeTestRule.onNodeWithTag("quick_add_10_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(145.0, viewModel.uiState.value.targetWeight, 0.001)

    // Tap Add 25lb -> 170 lbs
    composeTestRule.onNodeWithTag("quick_add_25_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(170.0, viewModel.uiState.value.targetWeight, 0.001)
    assertEquals(170.0, viewModel.uiState.value.calculationResult.achievedTotalWeight, 0.001)

    // Reset to empty bar (45 lbs) and tap Add 90lb -> 135 lbs
    composeTestRule.onNodeWithTag("quick_add_reset_bar_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(45.0, viewModel.uiState.value.targetWeight, 0.001)

    composeTestRule.onNodeWithTag("quick_add_90_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(135.0, viewModel.uiState.value.targetWeight, 0.001)
  }

  @Test
  fun quickAddButtonsAdaptToKgUnit() {
    composeTestRule.setContent {
      BarbellTheme {
        BarbellAppScreen(viewModel = viewModel)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("unit_kg_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(WeightUnit.KG, viewModel.uiState.value.unit)

    val beforeKg = viewModel.uiState.value.targetWeight
    composeTestRule.onNodeWithText("Add 10kg").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add 20kg").assertIsDisplayed()

    composeTestRule.onNodeWithTag("quick_add_20_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(beforeKg + 20.0, viewModel.uiState.value.targetWeight, 0.001)
  }

  @Test
  fun customTargetDialogIncludesQuickAddChips() {
    var inputText by mutableStateOf("100")
    composeTestRule.setContent {
      BarbellTheme {
        Surface {
          Column {
            OutlinedTextField(
              value = inputText,
              onValueChange = { inputText = it },
              modifier = Modifier.testTag("dialog_input_under_test")
            )
            com.example.ui.components.QuickAddDialogChips(
              unit = WeightUnit.LBS,
              onAddDelta = { delta ->
                val base = if (inputText.isBlank()) 0.0 else (inputText.toDoubleOrNull() ?: 0.0)
                val next = kotlin.math.round((base + delta) * 100.0) / 100.0
                inputText = WeightUnit.LBS.formatPlain(maxOf(0.0, next))
              }
            )
          }
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("dialog_quick_add_25_button").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals("125", inputText)
    composeTestRule.onNodeWithTag("dialog_input_under_test").assertTextContains("125")

    composeTestRule.onNodeWithTag("dialog_quick_add_90_button").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals("215", inputText)
  }
}
