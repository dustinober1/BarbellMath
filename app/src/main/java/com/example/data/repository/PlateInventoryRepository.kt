package com.example.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.data.dao.PlateInventoryDao
import com.example.data.entity.PlateInventoryEntity
import com.example.model.Plate
import com.example.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToInt

class PlateInventoryRepository(private val dao: PlateInventoryDao) {

  private val seedMutex = Mutex()

  val lbsPlates: Flow<List<Plate>> = dao.observePlatesByUnit(WeightUnit.LBS.name)
    .map { list -> list.map { it.toPlate() } }

  val kgPlates: Flow<List<Plate>> = dao.observePlatesByUnit(WeightUnit.KG.name)
    .map { list -> list.map { it.toPlate() } }

  suspend fun ensureSeeded() {
    seedMutex.withLock {
      if (dao.getTotalCount() == 0) {
        val defaults = (Plate.DEFAULT_LBS_PLATES + Plate.DEFAULT_KG_PLATES)
          .map { PlateInventoryEntity.fromPlate(it) }
        dao.insertAll(defaults)
      }
    }
  }

  suspend fun replaceAllPlates(lbs: List<Plate>, kg: List<Plate>) {
    seedMutex.withLock {
      dao.clearAll()
      val entities = (lbs + kg).map { PlateInventoryEntity.fromPlate(it.copy(id = 0L)) }
      dao.insertAll(entities)
    }
  }

  suspend fun addCustomPlate(
    weight: Double,
    unit: WeightUnit,
    color: Color,
    availablePairs: Int
  ): Result<Plate> {
    ensureSeeded()
    val cleanWeight = (weight * 100.0).roundToInt() / 100.0
    val hundredths = (cleanWeight * 100.0).roundToInt()
    if (cleanWeight <= 0.0) {
      return Result.failure(IllegalArgumentException("Plate weight must be greater than 0"))
    }
    val existing = dao.findByUnitAndWeight(unit.name, hundredths)
    if (existing != null) {
      return Result.failure(IllegalStateException("A ${Plate.formatWeight(cleanWeight)} ${unit.shortLabel} plate already exists"))
    }

    val plate = Plate.createCustom(
      weight = cleanWeight,
      unit = unit,
      color = color,
      availablePairs = availablePairs,
      isEnabled = true
    )
    val id = dao.insertPlate(PlateInventoryEntity.fromPlate(plate))
    return Result.success(plate.copy(id = id))
  }

  suspend fun updateCustomPlate(
    plateId: Long,
    originalWeight: Double,
    newWeight: Double,
    unit: WeightUnit,
    color: Color,
    availablePairs: Int,
    isEnabled: Boolean
  ): Result<Plate> {
    ensureSeeded()
    val cleanWeight = (newWeight * 100.0).roundToInt() / 100.0
    val newHundredths = (cleanWeight * 100.0).roundToInt()
    if (cleanWeight <= 0.0) {
      return Result.failure(IllegalArgumentException("Plate weight must be greater than 0"))
    }

    val currentEntity = if (plateId > 0L) {
      dao.findById(plateId)
    } else {
      dao.findByUnitAndWeight(unit.name, (originalWeight * 100.0).roundToInt())
    }

    val conflicting = dao.findByUnitAndWeight(unit.name, newHundredths)
    if (conflicting != null && (currentEntity == null || conflicting.id != currentEntity.id)) {
      return Result.failure(IllegalStateException("A ${Plate.formatWeight(cleanWeight)} ${unit.shortLabel} plate already exists"))
    }

    val updatedPlate = Plate.createCustom(
      weight = cleanWeight,
      unit = unit,
      color = color,
      availablePairs = availablePairs,
      isEnabled = isEnabled,
      id = currentEntity?.id ?: plateId
    )
    val entity = PlateInventoryEntity.fromPlate(updatedPlate)
    if (currentEntity != null) {
      dao.updatePlate(entity)
      return Result.success(updatedPlate)
    } else {
      val insertedId = dao.insertPlate(entity.copy(id = 0L))
      return Result.success(updatedPlate.copy(id = insertedId))
    }
  }

  suspend fun deleteCustomPlate(plate: Plate) {
    ensureSeeded()
    val target = if (plate.id > 0L) {
      dao.findById(plate.id)
    } else {
      dao.findByUnitAndWeight(plate.unit.name, (plate.weight * 100.0).roundToInt())
    }
    if (target != null) {
      dao.deleteById(target.id)
    }
  }

  suspend fun setPlateEnabled(unit: WeightUnit, weight: Double, enabled: Boolean) {
    ensureSeeded()
    val hundredths = (weight * 100.0).roundToInt()
    dao.setPlateEnabled(unit.name, hundredths, enabled)
  }

  suspend fun setAvailablePairs(unit: WeightUnit, weight: Double, pairs: Int) {
    ensureSeeded()
    val hundredths = (weight * 100.0).roundToInt()
    dao.setAvailablePairs(unit.name, hundredths, maxOf(0, pairs))
  }

  suspend fun exclude35LbAnd15Kg() {
    ensureSeeded()
    dao.setPlateEnabled(WeightUnit.LBS.name, 3500, false)
    dao.setPlateEnabled(WeightUnit.KG.name, 1500, false)
  }

  suspend fun enableAllPlates() {
    ensureSeeded()
    dao.setAllEnabled(true)
  }

  suspend fun applyMinimalistPreset() {
    ensureSeeded()
    val lbsKeep = setOf(4500, 2500, 1000, 500, 250)
    val kgKeep = setOf(2500, 2000, 1000, 500, 250)

    dao.getPlatesByUnit(WeightUnit.LBS.name).forEach { entity ->
      dao.updatePlate(entity.copy(isEnabled = entity.weightHundredths in lbsKeep))
    }
    dao.getPlatesByUnit(WeightUnit.KG.name).forEach { entity ->
      dao.updatePlate(entity.copy(isEnabled = entity.weightHundredths in kgKeep))
    }
  }

  suspend fun resetToDefaults() {
    seedMutex.withLock {
      dao.clearAll()
      val defaults = (Plate.DEFAULT_LBS_PLATES + Plate.DEFAULT_KG_PLATES)
        .map { PlateInventoryEntity.fromPlate(it) }
      dao.insertAll(defaults)
    }
  }
}
