package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.BarbellCalculator
import com.example.data.AppDatabase
import com.example.data.entity.LoadingPlanEntity
import com.example.data.repository.LoadingPlanRepository
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
  val activeTab: Int = 0 // 0: Plate Calculator, 1: Warm-up Ladder, 2: Reverse Racker
)

class BarbellViewModel @JvmOverloads constructor(
  application: Application,
  customRepository: LoadingPlanRepository? = null
) : AndroidViewModel(application) {

  private val repository: LoadingPlanRepository = customRepository
    ?: LoadingPlanRepository(AppDatabase.getInstance(application).loadingPlanDao())

  val recentPlans: StateFlow<List<LoadingPlanEntity>> = repository.recentPlans
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  private val _uiState = MutableStateFlow(BarbellUiState())
  val uiState: StateFlow<BarbellUiState> = _uiState.asStateFlow()

  private var autoSaveJob: Job? = null

  init {
    recalculate(triggerAutoSave = false)
  }

  fun setTargetWeight(weight: Double) {
    val cleanWeight = maxOf(0.0, weight)
    _uiState.update { it.copy(targetWeight = cleanWeight) }
    recalculate()
  }

  fun adjustTargetWeight(delta: Double) {
    val newWeight = maxOf(0.0, _uiState.value.targetWeight + delta)
    _uiState.update { it.copy(targetWeight = newWeight) }
    recalculate()
  }

  fun setUnit(unit: WeightUnit) {
    if (_uiState.value.unit == unit) return
    val currentWeight = _uiState.value.targetWeight
    // Smart converted target weight
    val convertedWeight = if (unit == WeightUnit.KG) {
      // lbs -> kg rounded to 2.5kg
      maxOf(20.0, (currentWeight * 0.45359237 / 2.5).toInt() * 2.5)
    } else {
      // kg -> lbs rounded to 5lbs
      maxOf(45.0, (currentWeight * 2.20462 / 5.0).toInt() * 5.0)
    }

    _uiState.update {
      it.copy(
        unit = unit,
        targetWeight = convertedWeight
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

  fun togglePlateEnabled(plateWeight: Double) {
    _uiState.update { state ->
      if (state.unit == WeightUnit.LBS) {
        val updated = state.lbsPlates.map {
          if (it.weight == plateWeight) it.copy(isEnabled = !it.isEnabled) else it
        }
        state.copy(lbsPlates = updated)
      } else {
        val updated = state.kgPlates.map {
          if (it.weight == plateWeight) it.copy(isEnabled = !it.isEnabled) else it
        }
        state.copy(kgPlates = updated)
      }
    }
    recalculate()
  }

  fun updatePlateAvailablePairs(plateWeight: Double, pairs: Int) {
    val cleanPairs = maxOf(0, pairs)
    _uiState.update { state ->
      if (state.unit == WeightUnit.LBS) {
        val updated = state.lbsPlates.map {
          if (it.weight == plateWeight) it.copy(availablePairs = cleanPairs) else it
        }
        state.copy(lbsPlates = updated)
      } else {
        val updated = state.kgPlates.map {
          if (it.weight == plateWeight) it.copy(availablePairs = cleanPairs) else it
        }
        state.copy(kgPlates = updated)
      }
    }
    recalculate()
  }

  fun resetPlateInventory() {
    _uiState.update {
      it.copy(
        lbsPlates = Plate.DEFAULT_LBS_PLATES,
        kgPlates = Plate.DEFAULT_KG_PLATES
      )
    }
    recalculate()
  }

  fun exclude35LbPlates() {
    _uiState.update { state ->
      val updatedLbs = state.lbsPlates.map {
        if (it.weight == 35.0) it.copy(isEnabled = false) else it
      }
      val updatedKg = state.kgPlates.map {
        if (it.weight == 15.0) it.copy(isEnabled = false) else it
      }
      state.copy(lbsPlates = updatedLbs, kgPlates = updatedKg)
    }
    recalculate()
  }

  fun enableAllPlates() {
    _uiState.update { state ->
      val updatedLbs = state.lbsPlates.map { it.copy(isEnabled = true) }
      val updatedKg = state.kgPlates.map { it.copy(isEnabled = true) }
      state.copy(lbsPlates = updatedLbs, kgPlates = updatedKg)
    }
    recalculate()
  }

  fun applyMinimalistPreset() {
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
    recalculate()
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

  // Reverse Racker actions
  fun addReversePlate(plate: Plate) {
    val currentPlates = _uiState.value.reversePlatesPerSide.toMutableList()
    val existingIndex = currentPlates.indexOfFirst { it.plate.weight == plate.weight }
    if (existingIndex >= 0) {
      val item = currentPlates[existingIndex]
      currentPlates[existingIndex] = item.copy(quantity = item.quantity + 1)
    } else {
      currentPlates.add(PlateCount(plate, 1))
    }
    // Sort heaviest inside
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
    val existingIndex = currentPlates.indexOfFirst { it.plate.weight == plate.weight }
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
