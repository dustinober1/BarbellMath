package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.dao.LoadingPlanDao
import com.example.data.entity.LoadingPlanEntity
import com.example.data.repository.LoadingPlanRepository
import com.example.model.BarType
import com.example.model.WeightUnit
import com.example.ui.BarbellViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoadingPlanRoomTest {

  private lateinit var database: AppDatabase
  private lateinit var dao: LoadingPlanDao
  private lateinit var repository: LoadingPlanRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dao = database.loadingPlanDao()
    repository = LoadingPlanRepository(dao)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testInsertAndRetrievePlan() = runTest {
    val entity = LoadingPlanEntity(
      timestamp = System.currentTimeMillis(),
      targetWeight = 225.0,
      achievedWeight = 225.0,
      unit = "LBS",
      barId = "olympic_mens",
      barName = "Olympic Bar (Men's)",
      barWeight = 45.0,
      customBarWeight = 45.0,
      collarsEnabled = false,
      platesSummary = "45.0:2",
      totalPlatesOnBar = 4,
      isExact = true
    )

    repository.savePlan(entity)

    val plans = repository.recentPlans.first()
    assertEquals(1, plans.size)
    assertEquals(225.0, plans[0].targetWeight, 0.001)
    assertEquals("Olympic Bar (Men's)", plans[0].barName)
    assertTrue(plans[0].isExact)

    val parsedPlates = plans[0].parsePlates()
    assertEquals(1, parsedPlates.size)
    assertEquals(45.0, parsedPlates[0].first, 0.001)
    assertEquals(2, parsedPlates[0].second)
  }

  @Test
  fun testMax10PlansEnforced() = runTest {
    // Insert 15 distinct plans with increasing timestamps
    for (i in 1..15) {
      val entity = LoadingPlanEntity(
        timestamp = 1000L * i,
        targetWeight = 100.0 + (i * 10),
        achievedWeight = 100.0 + (i * 10),
        unit = "LBS",
        barId = "olympic_mens",
        barName = "Olympic Bar (Men's)",
        barWeight = 45.0,
        customBarWeight = 45.0,
        collarsEnabled = false,
        platesSummary = "45.0:1",
        totalPlatesOnBar = 2,
        isExact = true
      )
      repository.savePlan(entity)
    }

    val plans = repository.recentPlans.first()
    // Should be strictly capped at 10
    assertEquals(10, plans.size)

    // The most recent should be targetWeight = 250.0 (i = 15)
    assertEquals(250.0, plans[0].targetWeight, 0.001)
    // The 10th should be targetWeight = 160.0 (i = 6)
    assertEquals(160.0, plans[9].targetWeight, 0.001)
  }

  @Test
  fun testDeletePlanByIdAndClearAll() = runTest {
    val plan1 = LoadingPlanEntity(
      id = 1,
      timestamp = 1000L,
      targetWeight = 135.0,
      achievedWeight = 135.0,
      unit = "LBS",
      barId = "olympic_mens",
      barName = "Olympic Bar",
      barWeight = 45.0,
      platesSummary = "45.0:1",
      totalPlatesOnBar = 2,
      isExact = true
    )
    val plan2 = LoadingPlanEntity(
      id = 2,
      timestamp = 2000L,
      targetWeight = 185.0,
      achievedWeight = 185.0,
      unit = "LBS",
      barId = "olympic_mens",
      barName = "Olympic Bar",
      barWeight = 45.0,
      platesSummary = "45.0:1,25.0:1",
      totalPlatesOnBar = 4,
      isExact = true
    )

    dao.insertPlan(plan1)
    dao.insertPlan(plan2)

    var plans = repository.recentPlans.first()
    assertEquals(2, plans.size)

    repository.deletePlanById(plans[0].id)
    plans = repository.recentPlans.first()
    assertEquals(1, plans.size)

    repository.clearHistory()
    plans = repository.recentPlans.first()
    assertTrue(plans.isEmpty())
  }

  @Test
  fun testViewModelLoadPlanRestoresState() = runTest {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BarbellViewModel(application, repository)

    val plan = LoadingPlanEntity(
      id = 1,
      timestamp = System.currentTimeMillis(),
      targetWeight = 315.0,
      achievedWeight = 315.0,
      unit = "KG",
      barId = BarType.OLYMPIC_WOMENS.id,
      barName = BarType.OLYMPIC_WOMENS.name,
      barWeight = BarType.OLYMPIC_WOMENS.weightKg,
      customBarWeight = 0.0,
      collarsEnabled = true,
      platesSummary = "25.0:2",
      totalPlatesOnBar = 4,
      isExact = true
    )

    viewModel.loadPlan(plan)

    val state = viewModel.uiState.value
    assertEquals(315.0, state.targetWeight, 0.001)
    assertEquals(WeightUnit.KG, state.unit)
    assertEquals(BarType.OLYMPIC_WOMENS.id, state.selectedBar.id)
    assertTrue(state.collarsEnabled)
    assertEquals(0, state.activeTab)
    assertEquals(315.0, state.calculationResult.targetWeight, 0.001)
  }
}
