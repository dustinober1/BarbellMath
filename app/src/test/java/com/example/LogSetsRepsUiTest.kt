package com.example

import android.app.Application
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.BarbellCalculator
import com.example.data.AppDatabase
import com.example.data.entity.WorkoutSessionEntity
import com.example.data.repository.LoadingPlanRepository
import com.example.data.repository.PlateInventoryRepository
import com.example.data.repository.WorkoutSessionRepository
import com.example.model.BarType
import com.example.model.Plate
import com.example.model.WeightUnit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.ui.BarbellAppScreen
import com.example.ui.BarbellViewModel
import com.example.ui.components.LogSessionFormContent
import com.example.ui.theme.BarbellTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class LogSetsRepsUiTest {

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
  fun setsAndRepsFormUpdatesPresetsSteppersAndVolumePreview() {
    val calcResult = BarbellCalculator.calculatePlates(
      targetWeight = 225.0,
      barWeight = 45.0,
      availablePlates = Plate.DEFAULT_LBS_PLATES,
      unit = WeightUnit.LBS
    )

    var selectedLift by mutableStateOf("General")
    var setsCount by mutableIntStateOf(3)
    var repsCount by mutableIntStateOf(5)
    var useCustom by mutableStateOf(false)
    var customRepsText by mutableStateOf("3×5")
    var notesText by mutableStateOf("")

    composeTestRule.setContent {
      BarbellTheme {
        Surface {
          LogSessionFormContent(
            calculationResult = calcResult,
            barName = BarType.OLYMPIC_MENS.name,
            selectedLift = selectedLift,
            onSelectLift = { selectedLift = it },
            setsCount = setsCount,
            onSetsChange = {
              setsCount = it
              useCustom = false
            },
            repsCount = repsCount,
            onRepsChange = {
              repsCount = it
              useCustom = false
            },
            useCustomRepsText = useCustom,
            onToggleCustomReps = { useCustom = it },
            customRepsText = customRepsText,
            onCustomRepsTextChange = { customRepsText = it },
            notesText = notesText,
            onNotesTextChange = { notesText = it }
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // Select Squat and 5x5 preset
    composeTestRule.onNodeWithTag("session_lift_chip_squat").performClick()
    composeTestRule.onNodeWithTag("preset_sets_reps_5x5").performClick()
    composeTestRule.waitForIdle()

    assertEquals("Squat", selectedLift)
    assertEquals(5, setsCount)
    assertEquals(5, repsCount)
    composeTestRule.onNodeWithTag("log_volume_summary")
      .assertTextContains("5 × 5 = 25 total reps", substring = true)

    // Step sets up to 6 and reps down to 4 -> 24 total reps
    composeTestRule.onNodeWithTag("log_sets_inc").performClick()
    composeTestRule.onNodeWithTag("log_reps_dec").performClick()
    composeTestRule.waitForIdle()

    assertEquals(6, setsCount)
    assertEquals(4, repsCount)
    composeTestRule.onNodeWithTag("log_volume_summary")
      .assertTextContains("6 × 4 = 24 total reps", substring = true)

    // Toggle Custom text field
    composeTestRule.onNodeWithTag("preset_sets_reps_custom").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("session_reps_input").assertIsDisplayed()
  }

  @Test
  fun loggingCalculatedSessionPersistsToRoomAndRendersInRecentSessions() {
    composeTestRule.setContent {
      BarbellTheme {
        BarbellAppScreen(viewModel = viewModel)
      }
    }
    composeTestRule.waitForIdle()

    // Calculate 225 lbs and save with sets & reps
    viewModel.setTargetWeight(225.0)
    viewModel.saveCurrentSession(
      liftName = "Squat",
      reps = "5×5",
      notes = "Beltless"
    )

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      viewModel.recentSessions.value.size == 1
    }
    composeTestRule.waitForIdle()

    val saved = viewModel.recentSessions.value.first()
    assertEquals("Squat", saved.liftName)
    assertEquals("5×5", saved.reps)
    assertEquals(225.0, saved.achievedWeight, 0.001)

    // Scroll to Recent Sessions and verify sets×reps badge and volume display
    composeTestRule.onNodeWithTag("main_lazy_column")
      .performScrollToNode(hasTestTag("recent_sessions_card"))
    composeTestRule.onNodeWithTag("session_reps_badge_${saved.id}")
      .performScrollTo()
      .assertIsDisplayed()
      .assertTextContains("5×5")
    composeTestRule.onNodeWithText("5625 lbs vol", substring = true)
      .performScrollTo()
      .assertIsDisplayed()
    composeTestRule.onNodeWithTag("header_log_session_button")
      .performScrollTo()
      .assertIsDisplayed()
  }

  @Test
  fun workoutSessionEntityParsesSetsRepsAndVolume() {
    val structured = WorkoutSessionEntity(
      targetWeight = 225.0,
      achievedWeight = 225.0,
      unit = WeightUnit.LBS.name,
      barId = BarType.OLYMPIC_MENS.id,
      barName = BarType.OLYMPIC_MENS.name,
      barWeight = 45.0,
      platesSummary = "45.0:2",
      totalPlatesOnBar = 4,
      isExact = true,
      reps = "5×5"
    )
    assertEquals(5 to 5, structured.parseSetsAndReps())
    assertEquals(5625.0, structured.totalVolume()!!, 0.001)

    val freeform = structured.copy(reps = "AMRAP")
    assertNull(freeform.parseSetsAndReps())
    assertNull(freeform.totalVolume())
  }
}
