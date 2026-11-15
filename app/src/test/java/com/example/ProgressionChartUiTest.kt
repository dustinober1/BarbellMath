package com.example

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.WorkoutSessionEntity
import com.example.data.repository.ChartTimeRange
import com.example.data.repository.LoadingPlanRepository
import com.example.data.repository.PlateInventoryRepository
import com.example.data.repository.WorkoutSessionRepository
import com.example.model.BarType
import com.example.model.WeightUnit
import com.example.ui.BarbellAppScreen
import com.example.ui.BarbellViewModel
import com.example.ui.theme.BarbellTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import kotlinx.coroutines.runBlocking
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
class ProgressionChartUiTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private lateinit var database: AppDatabase
  private lateinit var sessionRepository: WorkoutSessionRepository
  private lateinit var planRepository: LoadingPlanRepository
  private lateinit var plateRepository: PlateInventoryRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Application>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    sessionRepository = WorkoutSessionRepository(database.workoutSessionDao())
    planRepository = LoadingPlanRepository(database.loadingPlanDao())
    plateRepository = PlateInventoryRepository(database.plateInventoryDao())
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun progressTabShowsEmptyStateWhenNoSessionsLogged() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BarbellViewModel(
      application = application,
      customRepository = planRepository,
      customPlateRepository = plateRepository,
      customSessionRepository = sessionRepository
    )

    composeTestRule.setContent {
      BarbellTheme {
        BarbellAppScreen(viewModel = viewModel)
      }
    }

    composeTestRule.onNodeWithTag("nav_tab_progress").performClick()
    composeTestRule.waitForIdle()
    assertEquals(3, viewModel.uiState.value.activeTab)
    composeTestRule.onNodeWithTag("charts_empty_state").assertIsDisplayed()
  }

  @Test
  fun sparklinePreviewNavigatesToProgressChartsAndLoadsSelectedSession() {
    runBlocking {
      sessionRepository.saveSession(
        session(timestamp = 1_000L, lift = "Squat", weight = 225.0, unit = WeightUnit.LBS)
      )
      sessionRepository.saveSession(
        session(timestamp = 2_000L, lift = "Squat", weight = 245.0, unit = WeightUnit.LBS)
      )
    }

    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BarbellViewModel(
      application = application,
      customRepository = planRepository,
      customPlateRepository = plateRepository,
      customSessionRepository = sessionRepository
    )

    composeTestRule.setContent {
      BarbellTheme {
        BarbellAppScreen(viewModel = viewModel)
      }
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      viewModel.progressionChartState.value.points.size == 2 &&
        viewModel.recentSessions.value.size == 2
    }

    // Scroll to Recent Sessions sparkline preview on Calculator tab and tap it
    composeTestRule.onNodeWithTag("main_lazy_column")
      .performScrollToNode(hasTestTag("recent_sessions_trend_preview"))
    composeTestRule.onNodeWithTag("recent_sessions_trend_preview").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recent_sessions_sparkline", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("recent_sessions_trend_preview").performClick()
    composeTestRule.waitForIdle()

    assertEquals(3, viewModel.uiState.value.activeTab)
    composeTestRule.onNodeWithTag("chart_date_range_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("chart_range_1m").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals(ChartTimeRange.MONTH_1, viewModel.progressionChartState.value.selectedTimeRange)

    composeTestRule.onNodeWithTag("chart_range_3m").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals(ChartTimeRange.MONTH_3, viewModel.progressionChartState.value.selectedTimeRange)

    composeTestRule.onNodeWithTag("chart_range_6m").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals(ChartTimeRange.MONTH_6, viewModel.progressionChartState.value.selectedTimeRange)

    composeTestRule.onNodeWithTag("chart_range_1y").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals(ChartTimeRange.YEAR_1, viewModel.progressionChartState.value.selectedTimeRange)

    composeTestRule.onNodeWithTag("chart_range_all").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals(ChartTimeRange.ALL, viewModel.progressionChartState.value.selectedTimeRange)

    composeTestRule.onNodeWithTag("progression_kpi_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("weight_progression_canvas").assertIsDisplayed()
    composeTestRule.onNodeWithTag("main_lazy_column")
      .performScrollToNode(hasTestTag("inspector_load_on_bar_button"))
    composeTestRule.onNodeWithTag("selected_session_inspector").assertIsDisplayed()

    // Tap "Load" on inspector -> restores 245 lbs session on Calculator tab
    composeTestRule.onNodeWithTag("inspector_load_on_bar_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(0, viewModel.uiState.value.activeTab)
    assertEquals(245.0, viewModel.uiState.value.targetWeight, 0.001)
  }

  private fun session(
    timestamp: Long,
    lift: String,
    weight: Double,
    unit: WeightUnit
  ): WorkoutSessionEntity {
    return WorkoutSessionEntity(
      timestamp = timestamp,
      liftName = lift,
      reps = "5x5",
      notes = "",
      targetWeight = weight,
      achievedWeight = weight,
      unit = unit.name,
      barId = BarType.OLYMPIC_MENS.id,
      barName = BarType.OLYMPIC_MENS.name,
      barWeight = if (unit == WeightUnit.LBS) 45.0 else 20.0,
      collarsEnabled = false,
      platesSummary = "45.0:2",
      totalPlatesOnBar = 4,
      isExact = true
    )
  }
}
