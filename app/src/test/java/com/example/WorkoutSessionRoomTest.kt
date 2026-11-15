package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.dao.WorkoutSessionDao
import com.example.data.entity.WorkoutSessionEntity
import com.example.data.repository.LoadingPlanRepository
import com.example.data.repository.PlateInventoryRepository
import com.example.data.repository.WorkoutSessionRepository
import com.example.model.BarType
import com.example.model.WeightUnit
import com.example.ui.BarbellViewModel
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
class WorkoutSessionRoomTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var database: AppDatabase
  private lateinit var sessionDao: WorkoutSessionDao
  private lateinit var sessionRepository: WorkoutSessionRepository
  private lateinit var planRepository: LoadingPlanRepository
  private lateinit var plateRepository: PlateInventoryRepository

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    sessionDao = database.workoutSessionDao()
    sessionRepository = WorkoutSessionRepository(sessionDao)
    planRepository = LoadingPlanRepository(database.loadingPlanDao())
    plateRepository = PlateInventoryRepository(database.plateInventoryDao())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    database.close()
  }

  @Test
  fun testInsertRetrieveDeleteAndClearSessions() = runTest {
    val firstId = sessionRepository.saveSession(
      WorkoutSessionEntity(
        timestamp = 1000L,
        liftName = "Squat",
        reps = "5x5",
        notes = "Warm and solid",
        targetWeight = 225.0,
        achievedWeight = 225.0,
        unit = "LBS",
        barId = BarType.OLYMPIC_MENS.id,
        barName = BarType.OLYMPIC_MENS.name,
        barWeight = 45.0,
        collarsEnabled = false,
        platesSummary = "45.0:2",
        totalPlatesOnBar = 4,
        isExact = true
      )
    )
    val secondId = sessionRepository.saveSession(
      WorkoutSessionEntity(
        timestamp = 2000L,
        liftName = "Squat",
        reps = "3x5",
        notes = "Felt fast",
        targetWeight = 245.0,
        achievedWeight = 245.0,
        unit = "LBS",
        barId = BarType.OLYMPIC_MENS.id,
        barName = BarType.OLYMPIC_MENS.name,
        barWeight = 45.0,
        collarsEnabled = false,
        platesSummary = "45.0:2,10.0:1",
        totalPlatesOnBar = 6,
        isExact = true
      )
    )

    var sessions = sessionRepository.recentSessions.first()
    assertEquals(2, sessions.size)
    // Ordered newest first
    assertEquals(secondId, sessions[0].id)
    assertEquals(245.0, sessions[0].achievedWeight, 0.001)
    assertEquals("3x5", sessions[0].reps)
    assertEquals(firstId, sessions[1].id)

    sessionRepository.deleteSessionById(secondId)
    sessions = sessionRepository.recentSessions.first()
    assertEquals(1, sessions.size)
    assertEquals(firstId, sessions[0].id)

    sessionRepository.clearSessions()
    sessions = sessionRepository.recentSessions.first()
    assertTrue(sessions.isEmpty())
  }

  @Test
  fun testComputeSessionProgressionsTracksDeltasAndPersonalBests() {
    val sessions = listOf(
      WorkoutSessionEntity(
        id = 1,
        timestamp = 1000L,
        liftName = "Bench Press",
        targetWeight = 185.0,
        achievedWeight = 185.0,
        unit = "LBS",
        barId = BarType.OLYMPIC_MENS.id,
        barName = BarType.OLYMPIC_MENS.name,
        barWeight = 45.0,
        platesSummary = "45.0:1,25.0:1",
        totalPlatesOnBar = 4,
        isExact = true
      ),
      WorkoutSessionEntity(
        id = 2,
        timestamp = 2000L,
        liftName = "bench press",
        targetWeight = 195.0,
        achievedWeight = 195.0,
        unit = "LBS",
        barId = BarType.OLYMPIC_MENS.id,
        barName = BarType.OLYMPIC_MENS.name,
        barWeight = 45.0,
        platesSummary = "45.0:1,25.0:1,5.0:1",
        totalPlatesOnBar = 6,
        isExact = true
      ),
      WorkoutSessionEntity(
        id = 3,
        timestamp = 3000L,
        liftName = "Bench Press",
        targetWeight = 180.0,
        achievedWeight = 180.0,
        unit = "LBS",
        barId = BarType.OLYMPIC_MENS.id,
        barName = BarType.OLYMPIC_MENS.name,
        barWeight = 45.0,
        platesSummary = "45.0:1,10.0:2,2.5:1",
        totalPlatesOnBar = 8,
        isExact = true
      ),
      WorkoutSessionEntity(
        id = 4,
        timestamp = 2500L,
        liftName = "Deadlift",
        targetWeight = 140.0,
        achievedWeight = 140.0,
        unit = "KG",
        barId = BarType.OLYMPIC_MENS.id,
        barName = BarType.OLYMPIC_MENS.name,
        barWeight = 20.0,
        platesSummary = "25.0:2,10.0:1",
        totalPlatesOnBar = 6,
        isExact = true
      )
    )

    val progressions = WorkoutSessionRepository.computeSessionProgressions(sessions)

    val firstBench = progressions[1L]!!
    assertNull(firstBench.deltaFromPrevious)
    assertFalse(firstBench.isPersonalBest)

    val secondBench = progressions[2L]!!
    assertEquals(10.0, secondBench.deltaFromPrevious!!, 0.001)
    assertTrue(secondBench.isPersonalBest)

    val thirdBench = progressions[3L]!!
    assertEquals(-15.0, thirdBench.deltaFromPrevious!!, 0.001)
    assertFalse(thirdBench.isPersonalBest)

    val firstDeadlift = progressions[4L]!!
    assertNull(firstDeadlift.deltaFromPrevious)
    assertFalse(firstDeadlift.isPersonalBest)
  }

  @Test
  fun testViewModelSaveLoadDeleteAndClearSession() = runTest {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BarbellViewModel(
      application = application,
      customRepository = planRepository,
      customPlateRepository = plateRepository,
      customSessionRepository = sessionRepository
    )

    viewModel.setUnit(WeightUnit.KG)
    viewModel.selectBar(BarType.OLYMPIC_WOMENS)
    viewModel.toggleCollars(true)
    viewModel.setTargetWeight(100.0)
    viewModel.saveCurrentSession(
      liftName = "Deadlift",
      reps = "5 reps",
      notes = "Top set"
    )
    testScheduler.runCurrent()

    val saved = sessionRepository.recentSessions.first()
    assertEquals(1, saved.size)
    assertEquals("Deadlift", saved[0].liftName)
    assertEquals("5 reps", saved[0].reps)
    assertEquals("Top set", saved[0].notes)
    assertEquals("KG", saved[0].unit)
    assertEquals(BarType.OLYMPIC_WOMENS.id, saved[0].barId)
    assertTrue(saved[0].collarsEnabled)

    // Mutate calculator state, then restore from saved session
    viewModel.setActiveTab(2)
    viewModel.setUnit(WeightUnit.LBS)
    viewModel.selectBar(BarType.OLYMPIC_MENS)
    viewModel.toggleCollars(false)
    viewModel.setTargetWeight(135.0)

    viewModel.loadSession(saved[0])
    val restored = viewModel.uiState.value
    assertEquals(0, restored.activeTab)
    assertEquals(WeightUnit.KG, restored.unit)
    assertEquals(BarType.OLYMPIC_WOMENS.id, restored.selectedBar.id)
    assertTrue(restored.collarsEnabled)
    assertEquals(100.0, restored.targetWeight, 0.001)

    viewModel.deleteSession(saved[0].id)
    testScheduler.runCurrent()
    assertTrue(sessionRepository.recentSessions.first().isEmpty())
  }
}
