package com.example.data.entity

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.Plate
import com.example.model.WeightUnit
import kotlin.math.roundToInt

@Entity(
  tableName = "plate_inventory",
  indices = [Index(value = ["unit", "weightHundredths"], unique = true)]
)
data class PlateInventoryEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val weight: Double,
  val weightHundredths: Int,
  val unit: String, // "LBS" or "KG"
  val colorArgb: Int,
  val textColorArgb: Int,
  val heightRatio: Float,
  val thicknessFraction: Float,
  val isEnabled: Boolean,
  val availablePairs: Int,
  val isCustom: Boolean
) {
  fun toPlate(): Plate {
    val weightUnit = if (unit == WeightUnit.KG.name) WeightUnit.KG else WeightUnit.LBS
    return Plate(
      weight = weight,
      unit = weightUnit,
      color = Color(colorArgb),
      textColor = Color(textColorArgb),
      heightRatio = heightRatio,
      thicknessFraction = thicknessFraction,
      isEnabled = isEnabled,
      availablePairs = availablePairs,
      id = id,
      isCustom = isCustom
    )
  }

  companion object {
    fun fromPlate(plate: Plate): PlateInventoryEntity {
      val cleanWeight = (plate.weight * 100.0).roundToInt() / 100.0
      val hundredths = (cleanWeight * 100.0).roundToInt()
      return PlateInventoryEntity(
        id = plate.id,
        weight = cleanWeight,
        weightHundredths = hundredths,
        unit = plate.unit.name,
        colorArgb = plate.color.toArgb(),
        textColorArgb = plate.textColor.toArgb(),
        heightRatio = plate.heightRatio,
        thicknessFraction = plate.thicknessFraction,
        isEnabled = plate.isEnabled,
        availablePairs = plate.availablePairs,
        isCustom = plate.isCustom
      )
    }
  }
}
