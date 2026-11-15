package com.example.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.BarbellCalculator
import com.example.data.AppDatabase
import com.example.data.entity.LoadingPlanEntity
import com.example.data.entity.WorkoutSessionEntity
import com.example.data.repository.ChartTimeRange
import com.example.data.repository.LoadingPlanRepository
import com.example.data.repository.PlateInventoryRepository
import com.example.data.repository.ProgressionChartAnalyzer
import com.example.data.repository.ProgressionChartState
import com.example.data.repository.SessionProgression
import com.example.data.repository.WorkoutSessionRepository
import com.example.model.BarType
import com.example.model.Plate
import com.example.model.PlateCalculationResult
import com.example.model.PlateCount
import com.example.model.WarmupSet
import com.example.model.WeightUnit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class BarbellUiState(
  val targetWeight: Double = 135.0,
  val unit: WeightUnit = WeightUnit.LBS,
  val selectedBar: BarType = BarType.OLYMPIC_MENS,
  val customBarWeight: Double = 45.0,
  val collarsEnabled: Boolean = false,
  val collarsWeightLbs: Double = 1.0,
  val collarsWeightKg: Double = 0.5,
  val lbsPlates: List<Plate> = Plate.DEFAULT_LBS_PLATES,
  val kgPlates: List<Plate> = Plate.DEFAULT_KG_PLATES,
  val calculationResult: PlateCalculationResult = BarbellCalculator.calculatePlates(
    targetWeight = 135.0,
    barWeight = 45.0,
    collarsWeight = 0.0,
    availablePlates = Plate.DEFAULT_LBS_PLATES,
    unit = WeightUnit.LBS
  ),
  val warmupSets: List<WarmupSet> = emptyList(),
  val reversePlatesPerSide: List<PlateCount> = emptyList(),
  val reverseTotalWeight: Double = 45.0,
  val isFullBarView: Boolean = false,
  val showPlateLabels: Boolean = true,
  val activeTab: Int = 0 // 0: Plate Calculator, 1: Warm-up Ladder, 2: Reverse Racker, 3: Progress, 4: Settings
)

private data class ChartFilterState(
  val unit: WeightUnit = WeightUnit.LBS,
  val hasExplicitUnitSelection: Boolean = false,
  val selectedLift: String = "All",
  val timeRange: ChartTimeRange = ChartTimeRange.ALL
)

class BarbellViewModel @JvmOverloads constructor(
  application: Application,
  customRepository: LoadingPlanRepository? = null,
  customPlateRepository: PlateInventoryRepository? = null,
  customSessionRepository: WorkoutSessionRepository? = null
) : AndroidViewModel(application) {

  private val database by lazy { AppDatabase.getInstance(application) }

  private val repository: LoadingPlanRepository = customRepository
    ?: LoadingPlanRepository(database.loadingPlanDao())

  private val plateRepository: PlateInventoryRepository = customPlateRepository
    ?: PlateInventoryRepository(database.plateInventoryDao())

  private val sessionRepository: WorkoutSessionRepository = customSessionRepository
    ?: WorkoutSessionRepository(database.workoutSessionDao())

  val recentPlans: StateFlow<List<LoadingPlanEntity>> = repository.recentPlans
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val recentSessions: StateFlow<List<WorkoutSessionEntity>> = sessionRepository.recentSessions
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val allSessions: StateFlow<List<WorkoutSessionEntity>> = sessionRepository.allSessions
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val sessionProgressions: StateFlow<Map<Long, SessionProgression>> = allSessions
    .map { WorkoutSessionRepository.computeSessionProgressions(it) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyMap()
    )

  private val _uiState = MutableStateFlow(BarbellUiState())
  val uiState: StateFlow<BarbellUiState> = _uiState.asStateFlow()

  private val _chartFilterState = MutableStateFlow(ChartFilterState())

  val progressionChartState: StateFlow<ProgressionChartState> = combine(
    allSessions,
    _chartFilterState,
    _uiState.map { it.unit }
  ) { sessions, filter, activeCalculatorUnit ->
    val resolvedUnit = if (filter.hasExplicitUnitSelection) {
      filter.unit
    } else {
      val hasActiveUnitSessions = sessions.any { it.weightUnit == activeCalculatorUnit }
      when {
        hasActiveUnitSessions -> activeCalculatorUnit
        sessions.isNotEmpty() -> sessions.first().weightUnit
        else -> activeCalculatorUnit
      }
    }
    ProgressionChartAnalyzer.analyze(
      sessions = sessions,
      unit = resolvedUnit,
      selectedLift = filter.selectedLift,
      timeRange = filter.timeRange
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ProgressionChartState()
  )

  private var autoSaveJob: Job? = null
  @Volatile
  private var hasLocalInventoryMutation = false

  init {
    recalculate(triggerAutoSave = false)
    viewModelScope.launch {
      plateRepository.ensureSeeded()
      if (hasLocalInventoryMutation) {
        val current = _uiState.value
        plateRepository.replaceAllPlates(current.lbsPlates, current.kgPlates)
      }
      combine(plateRepository.lbsPlates, plateRepository.kgPlates) { lbs, kg ->
        lbs to kg
      }.collect { (lbs, kg) ->
        if (lbs.isNotEmpty() || kg.isNotEmpty()) {
          _uiState.update { state ->
            state.copy(
              lbsPlates = if (lbs.isNotEmpty()) lbs else state.lbsPlates,
              kgPlates = if (kg.isNotEmpty()) kg else state.kgPlates
            )
          }
          recalculate(triggerAutoSave = false)
        }
      }
    }
  }

  fun setTargetWeight(weight: Double) {
    val cleanWeight = maxOf(0.0, weight)
    _uiState.update { it.copy(targetWeight = cleanWeight) }
    recalculate()
  }

  fun adjustTargetWeight(delta: Double) {
    val newWeight = maxOf(0.0, ((_uiState.value.targetWeight + delta) * 100.0).roundToInt() / 100.0)
    _uiState.update { it.copy(targetWeight = newWeight) }
    recalculate()
  }

  fun setUnit(unit: WeightUnit) {
    if (_uiState.value.unit == unit) return
    val currentWeight = _uiState.value.targetWeight
    val convertedWeight = if (unit == WeightUnit.KG) {
      maxOf(20.0, (currentWeight * 0.45359237 / 2.5).toInt() * 2.5)
    } else {
      maxOf(45.0, (currentWeight * 2.20462 / 5.0).toInt() * 5.0)
    }

    _uiState.update {
      it.copy(
        unit = unit,
        targetWeight = convertedWeight,
        reversePlatesPerSide = emptyList()
      )
    }
    recalculate()
  }

  fun selectBar(bar: BarType) {
    _uiState.update { it.copy(selectedBar = bar) }
    recalculate()
  }

  fun setCustomBarWeight(weight: Double) {
    _uiState.update { it.copy(customBarWeight = maxOf(0.0, weight)) }
    recalculate()
  }

  fun toggleCollars(enabled: Boolean) {
    _uiState.update { it.copy(collarsEnabled = enabled) }
    recalculate()
  }

  fun toggleFullBarView() {
    _uiState.update { it.copy(isFullBarView = !it.isFullBarView) }
  }

  fun togglePlateLabels() {
    _uiState.update { it.copy(showPlateLabels = !it.showPlateLabels) }
  }

  fun setShowPlateLabels(show: Boolean) {
    _uiState.update { it.copy(showPlateLabels = show) }
  }

  fun setActiveTab(tabIndex: Int) {
    _uiState.update { it.copy(activeTab = tabIndex) }
  }

  fun openProgressTab() {
    _uiState.update { it.copy(activeTab = 3) }
  }

  fun setChartUnit(unit: WeightUnit) {
    _chartFilterState.update {
      it.copy(unit = unit, hasExplicitUnitSelection = true)
    }
  }

  fun setChartLiftFilter(lift: String) {
    _chartFilterState.update {
      it.copy(selectedLift = lift)
    }
  }

  fun setChartTimeRange(range: ChartTimeRange) {
    _chartFilterState.update {
      it.copy(timeRange = range)
    }
  }

  fun togglePlateEnabled(plateWeight: Double) {
    hasLocalInventoryMutation = true
    val activeUnit = _uiState.value.unit
    var newEnabled = true

    _uiState.update { state ->
      if (activeUnit == WeightUnit.LBS) {
        val updated = state.lbsPlates.map {
          if (abs(it.weight - plateWeight) < 0.001) {
            newEnabled = !it.isEnabled
            it.copy(isEnabled = newEnabled)
          } else it
        }
        state.copy(lbsPlates = updated)
      } else {
        val updated = state.kgPlates.map {
          if (abs(it.weight - plateWeight) < 0.001) {
            newEnabled = !it.isEnabled
            it.copy(isEnabled = newEnabled)
          } else it
        }
        state.copy(kgPlates = updated)
      }
    }
    syncReverseRackWithInventory()
    recalculate()

    viewModelScope.launch {
      plateRepository.setPlateEnabled(activeUnit, plateWeight, newEnabled)
    }
  }

  fun updatePlateAvailablePairs(plateWeight: Double, pairs: Int) {
    hasLocalInventoryMutation = true
    val cleanPairs = maxOf(0, pairs)
    val activeUnit = _uiState.value.unit

    _uiState.update { state ->
      if (activeUnit == WeightUnit.LBS) {
        val updated = state.lbsPlates.map {
          if (abs(it.weight - plateWeight) < 0.001) it.copy(availablePairs = cleanPairs) else it
        }
        state.copy(lbsPlates = updated)
      } else {
        val updated = state.kgPlates.map {
          if (abs(it.weight - plateWeight) < 0.001) it.copy(availablePairs = cleanPairs) else it
        }
        state.copy(kgPlates = updated)
      }
    }
    syncReverseRackWithInventory()
    recalculate()

    viewModelScope.launch {
      plateRepository.setAvailablePairs(activeUnit, plateWeight, cleanPairs)
    }
  }

  fun addCustomPlate(
    weight: Double,
    color: Color,
    availablePairs: Int
  ): String? {
    val cleanWeight = (weight * 100.0).roundToInt() / 100.0
    if (cleanWeight <= 0.0 || cleanWeight > 500.0) {
      return "Enter a plate weight between 0.01 and 500"
    }

    val activeUnit = _uiState.value.unit
    val existingList = if (activeUnit == WeightUnit.LBS) _uiState.value.lbsPlates else _uiState.value.kgPlates
    if (existingList.any { abs(it.weight - cleanWeight) < 0.001 }) {
      return "A ${Plate.formatWeight(cleanWeight)} ${activeUnit.shortLabel} plate already exists"
    }

    hasLocalInventoryMutation = true
    val newPlate = Plate.createCustom(
      weight = cleanWeight,
      unit = activeUnit,
      color = color,
      availablePairs = availablePairs,
      isEnabled = true
    )

    _uiState.update { state ->
      if (activeUnit == WeightUnit.LBS) {
        state.copy(lbsPlates = (state.lbsPlates + newPlate).sortedByDescending { it.weight })
      } else {
        state.copy(kgPlates = (state.kgPlates + newPlate).sortedByDescending { it.weight })
      }
    }
    recalculate()

    viewModelScope.launch {
      plateRepository.addCustomPlate(
        weight = cleanWeight,
        unit = activeUnit,
        color = color,
        availablePairs = availablePairs
      )
    }
    return null
  }

  fun updateCustomPlate(
    originalPlate: Plate,
    newWeight: Double,
    color: Color,
    availablePairs: Int
  ): String? {
    val cleanWeight = (newWeight * 100.0).roundToInt() / 100.0
    if (cleanWeight <= 0.0 || cleanWeight > 500.0) {
      return "Enter a plate weight between 0.01 and 500"
    }

    val activeUnit = originalPlate.unit
    val existingList = if (activeUnit == WeightUnit.LBS) _uiState.value.lbsPlates else _uiState.value.kgPlates
    val hasConflict = existingList.any {
      abs(it.weight - cleanWeight) < 0.001 && abs(it.weight - originalPlate.weight) >= 0.001
    }
    if (hasConflict) {
      return "A ${Plate.formatWeight(cleanWeight)} ${activeUnit.shortLabel} plate already exists"
    }

    hasLocalInventoryMutation = true
    val updatedPlate = Plate.createCustom(
      weight = cleanWeight,
      unit = activeUnit,
      color = color,
      availablePairs = availablePairs,
      isEnabled = originalPlate.isEnabled,
      id = originalPlate.id
    )

    _uiState.update { state ->
      if (activeUnit == WeightUnit.LBS) {
        val updated = state.lbsPlates.map {
          if (abs(it.weight - originalPlate.weight) < 0.001) updatedPlate else it
        }.sortedByDescending { it.weight }
        state.copy(lbsPlates = updated)
      } else {
        val updated = state.kgPlates.map {
          if (abs(it.weight - originalPlate.weight) < 0.001) updatedPlate else it
        }.sortedByDescending { it.weight }
        state.copy(kgPlates = updated)
      }
    }
    syncReverseRackWithInventory()
    recalculate()

    viewModelScope.launch {
      plateRepository.updateCustomPlate(
        plateId = originalPlate.id,
        originalWeight = originalPlate.weight,
        newWeight = cleanWeight,
        unit = activeUnit,
        color = color,
        availablePairs = availablePairs,
        isEnabled = originalPlate.isEnabled
      )
    }
    return null
  }

  fun deleteCustomPlate(plate: Plate) {
    hasLocalInventoryMutation = true
    _uiState.update { state ->
      if (plate.unit == WeightUnit.LBS) {
        state.copy(lbsPlates = state.lbsPlates.filterNot { abs(it.weight - plate.weight) < 0.001 })
      } else {
        state.copy(kgPlates = state.kgPlates.filterNot { abs(it.weight - plate.weight) < 0.001 })
      }
    }
    syncReverseRackWithInventory()
    recalculate()

    viewModelScope.launch {
      plateRepository.deleteCustomPlate(plate)
    }
  }

  fun resetPlateInventory() {
    hasLocalInventoryMutation = true
    _uiState.update {
      it.copy(
        lbsPlates = Plate.DEFAULT_LBS_PLATES,
        kgPlates = Plate.DEFAULT_KG_PLATES
      )
    }
    syncReverseRackWithInventory()
    recalculate()

    viewModelScope.launch {
      plateRepository.resetToDefaults()
    }
  }

  fun exclude35LbPlates() {
    hasLocalInventoryMutation = true
    _uiState.update { state ->
      val updatedLbs = state.lbsPlates.map {
        if (it.weight == 35.0) it.copy(isEnabled = false) else it
      }
      val updatedKg = state.kgPlates.map {
        if (it.weight == 15.0) it.copy(isEnabled = false) else it
      }
      state.copy(lbsPlates = updatedLbs, kgPlates = updatedKg)
    }
    syncReverseRackWithInventory()
    recalculate()

    viewModelScope.launch {
      plateRepository.exclude35LbAnd15Kg()
    }
  }

  fun enableAllPlates() {
    hasLocalInventoryMutation = true
    _uiState.update { state ->
      val updatedLbs = state.lbsPlates.map { it.copy(isEnabled = true) }
      val updatedKg = state.kgPlates.map { it.copy(isEnabled = true) }
      state.copy(lbsPlates = updatedLbs, kgPlates = updatedKg)
    }
    recalculate()

    viewModelScope.launch {
      plateRepository.enableAllPlates()
    }
  }

  fun applyMinimalistPreset() {
    hasLocalInventoryMutation = true
    _uiState.update { state ->
      val updatedLbs = state.lbsPlates.map {
        val keep = it.weight in listOf(45.0, 25.0, 10.0, 5.0, 2.5)
        it.copy(isEnabled = keep)
      }
      val updatedKg = state.kgPlates.map {
        val keep = it.weight in listOf(25.0, 20.0, 10.0, 5.0, 2.5)
        it.copy(isEnabled = keep)
      }
      state.copy(lbsPlates = updatedLbs, kgPlates = updatedKg)
    }
    syncReverseRackWithInventory()
    recalculate()

    viewModelScope.launch {
      plateRepository.applyMinimalistPreset()
    }
  }

  // Load a previously saved plan from Room database
  fun loadPlan(plan: LoadingPlanEntity) {
    val unit = if (plan.unit == "KG") WeightUnit.KG else WeightUnit.LBS
    val bar = BarType.ALL_PRESETS.firstOrNull { it.id == plan.barId }
      ?: BarType(
        id = plan.barId,
        name = plan.barName,
        weightLbs = plan.barWeight,
        weightKg = plan.barWeight,
        description = plan.barName,
        isCustom = plan.barId == "custom"
      )

    _uiState.update {
      it.copy(
        targetWeight = plan.targetWeight,
        unit = unit,
        selectedBar = bar,
        customBarWeight = plan.customBarWeight,
        collarsEnabled = plan.collarsEnabled,
        activeTab = 0
      )
    }
    recalculate(triggerAutoSave = false)
  }

  fun deleteHistoryPlan(id: Long) {
    viewModelScope.launch {
      repository.deletePlanById(id)
    }
  }

  fun clearHistory() {
    viewModelScope.launch {
      repository.clearHistory()
    }
  }

  // Workout Session Logging actions
  fun saveCurrentSession(
    liftName: String = "General",
    reps: String = "",
    notes: String = ""
  ) {
    val state = _uiState.value
    val result = state.calculationResult
    val cleanLift = liftName.trim().ifBlank { "General" }
    val platesSummary = result.platesPerSide.joinToString(",") {
      "${it.plate.weight}:${it.quantity}"
    }
    val barWeight = getEffectiveBarWeight()

    val session = WorkoutSessionEntity(
      timestamp = System.currentTimeMillis(),
      liftName = cleanLift,
      reps = reps.trim(),
      notes = notes.trim(),
      targetWeight = result.targetWeight,
      achievedWeight = result.achievedTotalWeight,
      unit = state.unit.name,
      barId = state.selectedBar.id,
      barName = state.selectedBar.name,
      barWeight = barWeight,
      customBarWeight = state.customBarWeight,
      collarsEnabled = state.collarsEnabled,
      platesSummary = platesSummary,
      totalPlatesOnBar = result.totalPlatesOnBar,
      isExact = result.isExact
    )

    viewModelScope.launch {
      sessionRepository.saveSession(session)
    }
  }

  fun loadSession(session: WorkoutSessionEntity) {
    val unit = if (session.unit == WeightUnit.KG.name) WeightUnit.KG else WeightUnit.LBS
    val bar = BarType.ALL_PRESETS.firstOrNull { it.id == session.barId }
      ?: BarType(
        id = session.barId,
        name = session.barName,
        weightLbs = session.barWeight,
        weightKg = session.barWeight,
        description = session.barName,
        isCustom = session.barId == "custom"
      )

    _uiState.update {
      it.copy(
        targetWeight = session.targetWeight,
        unit = unit,
        selectedBar = bar,
        customBarWeight = session.customBarWeight,
        collarsEnabled = session.collarsEnabled,
        activeTab = 0
      )
    }
    recalculate(triggerAutoSave = false)
  }

  fun deleteSession(id: Long) {
    viewModelScope.launch {
      sessionRepository.deleteSessionById(id)
    }
  }

  fun clearAllSessions() {
    viewModelScope.launch {
      sessionRepository.clearSessions()
    }
  }

  // Reverse Racker actions
  fun addReversePlate(plate: Plate) {
    val activeList = if (_uiState.value.unit == WeightUnit.LBS) _uiState.value.lbsPlates else _uiState.value.kgPlates
    val inventoryPlate = activeList.firstOrNull { abs(it.weight - plate.weight) < 0.001 } ?: plate
    if (!inventoryPlate.isEnabled || inventoryPlate.availablePairs <= 0) return

    val currentPlates = _uiState.value.reversePlatesPerSide.toMutableList()
    val existingIndex = currentPlates.indexOfFirst { abs(it.plate.weight - plate.weight) < 0.001 }
    if (existingIndex >= 0) {
      val item = currentPlates[existingIndex]
      if (item.quantity >= inventoryPlate.availablePairs) return
      currentPlates[existingIndex] = item.copy(plate = inventoryPlate, quantity = item.quantity + 1)
    } else {
      currentPlates.add(PlateCount(inventoryPlate, 1))
    }
    currentPlates.sortByDescending { it.plate.weight }

    val barWeight = getEffectiveBarWeight()
    val collars = getEffectiveCollarsWeight()
    val total = BarbellCalculator.calculateReverseWeight(currentPlates, barWeight, collars)

    _uiState.update {
      it.copy(
        reversePlatesPerSide = currentPlates,
        reverseTotalWeight = total
      )
    }
  }

  fun removeReversePlate(plate: Plate) {
    val currentPlates = _uiState.value.reversePlatesPerSide.toMutableList()
    val existingIndex = currentPlates.indexOfFirst { abs(it.plate.weight - plate.weight) < 0.001 }
    if (existingIndex >= 0) {
      val item = currentPlates[existingIndex]
      if (item.quantity > 1) {
        currentPlates[existingIndex] = item.copy(quantity = item.quantity - 1)
      } else {
        currentPlates.removeAt(existingIndex)
      }
    }
    val barWeight = getEffectiveBarWeight()
    val collars = getEffectiveCollarsWeight()
    val total = BarbellCalculator.calculateReverseWeight(currentPlates, barWeight, collars)

    _uiState.update {
      it.copy(
        reversePlatesPerSide = currentPlates,
        reverseTotalWeight = total
      )
    }
  }

  fun clearReversePlates() {
    val barWeight = getEffectiveBarWeight()
    val collars = getEffectiveCollarsWeight()
    _uiState.update {
      it.copy(
        reversePlatesPerSide = emptyList(),
        reverseTotalWeight = barWeight + collars
      )
    }
  }

  fun applyReverseWeightToTarget() {
    val weight = _uiState.value.reverseTotalWeight
    _uiState.update {
      it.copy(
        targetWeight = weight,
        activeTab = 0
      )
    }
    recalculate()
  }

  private fun syncReverseRackWithInventory() {
    val state = _uiState.value
    val activeInventory = if (state.unit == WeightUnit.LBS) state.lbsPlates else state.kgPlates
    val filteredReverse = state.reversePlatesPerSide.mapNotNull { loaded ->
      val match = activeInventory.firstOrNull {
        abs(it.weight - loaded.plate.weight) < 0.001 && it.isEnabled && it.availablePairs > 0
      }
      match?.let {
        val cappedQty = minOf(loaded.quantity, it.availablePairs)
        if (cappedQty > 0) loaded.copy(plate = it, quantity = cappedQty) else null
      }
    }
    if (filteredReverse != state.reversePlatesPerSide) {
      _uiState.update { it.copy(reversePlatesPerSide = filteredReverse) }
    }
  }

  private fun getEffectiveBarWeight(): Double {
    val state = _uiState.value
    return if (state.selectedBar.isCustom) {
      state.customBarWeight
    } else {
      state.selectedBar.getWeight(state.unit)
    }
  }

  private fun getEffectiveCollarsWeight(): Double {
    val state = _uiState.value
    if (!state.collarsEnabled) return 0.0
    return if (state.unit == WeightUnit.LBS) state.collarsWeightLbs else state.collarsWeightKg
  }

  private fun scheduleAutoSave() {
    val state = _uiState.value
    val result = state.calculationResult
    if (result.targetWeight <= 0.0) return

    autoSaveJob?.cancel()
    autoSaveJob = viewModelScope.launch {
      delay(800) // Debounce rapid adjustments
      val currentState = _uiState.value
      val currentResult = currentState.calculationResult
      if (currentResult.targetWeight <= 0.0) return@launch

      val platesSummary = currentResult.platesPerSide.joinToString(",") {
        "${it.plate.weight}:${it.quantity}"
      }

      val entity = LoadingPlanEntity(
        timestamp = System.currentTimeMillis(),
        targetWeight = currentState.targetWeight,
        achievedWeight = currentResult.achievedTotalWeight,
        unit = currentState.unit.name,
        barId = currentState.selectedBar.id,
        barName = currentState.selectedBar.name,
        barWeight = if (currentState.selectedBar.isCustom) currentState.customBarWeight else currentState.selectedBar.getWeight(currentState.unit),
        customBarWeight = currentState.customBarWeight,
        collarsEnabled = currentState.collarsEnabled,
        platesSummary = platesSummary,
        totalPlatesOnBar = currentResult.totalPlatesOnBar,
        isExact = currentResult.isExact
      )
      repository.savePlan(entity)
    }
  }

  private fun recalculate(triggerAutoSave: Boolean = true) {
    val state = _uiState.value
    val barWeight = getEffectiveBarWeight()
    val collarsWeight = getEffectiveCollarsWeight()
    val plates = if (state.unit == WeightUnit.LBS) state.lbsPlates else state.kgPlates

    val result = BarbellCalculator.calculatePlates(
      targetWeight = state.targetWeight,
      barWeight = barWeight,
      collarsWeight = collarsWeight,
      availablePlates = plates,
      unit = state.unit
    )

    val warmups = BarbellCalculator.generateWarmupSets(
      workingWeight = state.targetWeight,
      barWeight = barWeight,
      collarsWeight = collarsWeight,
      availablePlates = plates,
      unit = state.unit
    )

    val reverseTotal = BarbellCalculator.calculateReverseWeight(
      state.reversePlatesPerSide,
      barWeight,
      collarsWeight
    )

    _uiState.update {
      it.copy(
        calculationResult = result,
        warmupSets = warmups,
        reverseTotalWeight = reverseTotal
      )
    }

    if (triggerAutoSave) {
      scheduleAutoSave()
    }
  }
}
