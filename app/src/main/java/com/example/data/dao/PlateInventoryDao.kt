package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.PlateInventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlateInventoryDao {

  @Query("SELECT * FROM plate_inventory WHERE unit = :unit ORDER BY weight DESC")
  fun observePlatesByUnit(unit: String): Flow<List<PlateInventoryEntity>>

  @Query("SELECT * FROM plate_inventory WHERE unit = :unit ORDER BY weight DESC")
  suspend fun getPlatesByUnit(unit: String): List<PlateInventoryEntity>

  @Query("SELECT COUNT(*) FROM plate_inventory")
  suspend fun getTotalCount(): Int

  @Query("SELECT * FROM plate_inventory WHERE unit = :unit AND weightHundredths = :weightHundredths LIMIT 1")
  suspend fun findByUnitAndWeight(unit: String, weightHundredths: Int): PlateInventoryEntity?

  @Query("SELECT * FROM plate_inventory WHERE id = :id LIMIT 1")
  suspend fun findById(id: Long): PlateInventoryEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(plates: List<PlateInventoryEntity>)

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertPlate(plate: PlateInventoryEntity): Long

  @Update
  suspend fun updatePlate(plate: PlateInventoryEntity)

  @Query("UPDATE plate_inventory SET isEnabled = :enabled WHERE unit = :unit AND weightHundredths = :weightHundredths")
  suspend fun setPlateEnabled(unit: String, weightHundredths: Int, enabled: Boolean)

  @Query("UPDATE plate_inventory SET availablePairs = :pairs WHERE unit = :unit AND weightHundredths = :weightHundredths")
  suspend fun setAvailablePairs(unit: String, weightHundredths: Int, pairs: Int)

  @Query("UPDATE plate_inventory SET isEnabled = :enabled")
  suspend fun setAllEnabled(enabled: Boolean)

  @Query("DELETE FROM plate_inventory WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM plate_inventory")
  suspend fun clearAll()
}
