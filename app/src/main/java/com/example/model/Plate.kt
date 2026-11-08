package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.PlateBlack
import com.example.ui.theme.PlateBlue
import com.example.ui.theme.PlateCyan
import com.example.ui.theme.PlateGreen
import com.example.ui.theme.PlateOrange
import com.example.ui.theme.PlatePurple
import com.example.ui.theme.PlateRed
import com.example.ui.theme.PlateWhite
import com.example.ui.theme.PlateYellow

data class Plate(
  val weight: Double,
  val unit: WeightUnit,
  val color: Color,
  val textColor: Color = Color.White,
  val heightRatio: Float, // 1.0 = full Olympic bumper diameter (450mm)
  val thicknessFraction: Float, // Relative width
  val isEnabled: Boolean = true,
  val availablePairs: Int = 10, // Available pairs in gym
) {
  val formattedWeight: String
    get() = if (weight % 1.0 == 0.0) "${weight.toInt()}" else "$weight"

  val displayLabel: String
    get() = "$formattedWeight ${unit.shortLabel}"

  companion object {
    val DEFAULT_LBS_PLATES = listOf(
      Plate(weight = 45.0, unit = WeightUnit.LBS, color = PlateRed, textColor = Color.White, heightRatio = 1.0f, thicknessFraction = 1.0f),
      Plate(weight = 35.0, unit = WeightUnit.LBS, color = PlateBlue, textColor = Color.White, heightRatio = 0.88f, thicknessFraction = 0.85f),
      Plate(weight = 25.0, unit = WeightUnit.LBS, color = PlateYellow, textColor = Color.Black, heightRatio = 0.78f, thicknessFraction = 0.75f),
      Plate(weight = 10.0, unit = WeightUnit.LBS, color = PlateGreen, textColor = Color.White, heightRatio = 0.65f, thicknessFraction = 0.60f),
      Plate(weight = 5.0, unit = WeightUnit.LBS, color = PlateWhite, textColor = Color.Black, heightRatio = 0.52f, thicknessFraction = 0.50f),
      Plate(weight = 2.5, unit = WeightUnit.LBS, color = PlateBlack, textColor = Color.White, heightRatio = 0.42f, thicknessFraction = 0.42f),
      Plate(weight = 1.25, unit = WeightUnit.LBS, color = PlatePurple, textColor = Color.White, heightRatio = 0.35f, thicknessFraction = 0.35f, isEnabled = true),
      Plate(weight = 0.75, unit = WeightUnit.LBS, color = PlateOrange, textColor = Color.White, heightRatio = 0.30f, thicknessFraction = 0.30f, isEnabled = false),
      Plate(weight = 0.5, unit = WeightUnit.LBS, color = PlateCyan, textColor = Color.White, heightRatio = 0.28f, thicknessFraction = 0.28f, isEnabled = false)
    )

    val DEFAULT_KG_PLATES = listOf(
      Plate(weight = 25.0, unit = WeightUnit.KG, color = PlateRed, textColor = Color.White, heightRatio = 1.0f, thicknessFraction = 1.0f),
      Plate(weight = 20.0, unit = WeightUnit.KG, color = PlateBlue, textColor = Color.White, heightRatio = 0.90f, thicknessFraction = 0.88f),
      Plate(weight = 15.0, unit = WeightUnit.KG, color = PlateYellow, textColor = Color.Black, heightRatio = 0.80f, thicknessFraction = 0.75f),
      Plate(weight = 10.0, unit = WeightUnit.KG, color = PlateGreen, textColor = Color.White, heightRatio = 0.68f, thicknessFraction = 0.62f),
      Plate(weight = 5.0, unit = WeightUnit.KG, color = PlateWhite, textColor = Color.Black, heightRatio = 0.55f, thicknessFraction = 0.50f),
      Plate(weight = 2.5, unit = WeightUnit.KG, color = PlateBlack, textColor = Color.White, heightRatio = 0.45f, thicknessFraction = 0.42f),
      Plate(weight = 1.25, unit = WeightUnit.KG, color = PlatePurple, textColor = Color.White, heightRatio = 0.38f, thicknessFraction = 0.36f),
      Plate(weight = 0.5, unit = WeightUnit.KG, color = PlateCyan, textColor = Color.White, heightRatio = 0.30f, thicknessFraction = 0.30f, isEnabled = true),
      Plate(weight = 0.25, unit = WeightUnit.KG, color = PlateOrange, textColor = Color.White, heightRatio = 0.25f, thicknessFraction = 0.26f, isEnabled = false)
    )
  }
}

data class PlateCount(
  val plate: Plate,
  val quantity: Int // Number on ONE side
) {
  val totalWeightPerSide: Double get() = plate.weight * quantity
  val totalWeightBothSides: Double get() = totalWeightPerSide * 2
}

data class PlateCalculationResult(
  val targetWeight: Double,
  val barWeight: Double,
  val collarsWeight: Double,
  val unit: WeightUnit,
  val platesPerSide: List<PlateCount>,
  val totalPlatesOnBar: Int,
  val plateWeightPerSide: Double,
  val achievedTotalWeight: Double,
  val remainder: Double,
  val isExact: Boolean,
  val isUnderBarWeight: Boolean
)

data class WarmupSet(
  val percentage: Int,
  val title: String,
  val reps: String,
  val targetWeight: Double,
  val platesPerSide: List<PlateCount>,
  val achievedTotalWeight: Double,
  val isExact: Boolean
)
