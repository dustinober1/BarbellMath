package com.example.model

data class BarType(
  val id: String,
  val name: String,
  val weightLbs: Double,
  val weightKg: Double,
  val description: String,
  val isCustom: Boolean = false,
) {
  fun getWeight(unit: WeightUnit): Double {
    return when (unit) {
      WeightUnit.LBS -> weightLbs
      WeightUnit.KG -> weightKg
    }
  }

  companion object {
    val OLYMPIC_MENS = BarType(
      id = "olympic_mens",
      name = "Olympic Bar (Men's)",
      weightLbs = 45.0,
      weightKg = 20.0,
      description = "Standard 20kg / 45lb barbell with 50mm rotating sleeves"
    )

    val OLYMPIC_WOMENS = BarType(
      id = "olympic_womens",
      name = "Olympic Bar (Women's)",
      weightLbs = 35.0,
      weightKg = 15.0,
      description = "Standard 15kg / 35lb barbell with 25mm shaft diameter"
    )

    val TECHNIQUE = BarType(
      id = "technique",
      name = "Technique / Youth Bar",
      weightLbs = 15.0,
      weightKg = 7.5,
      description = "Lightweight aluminum/steel training bar for form & warm-ups"
    )

    val EZ_CURL = BarType(
      id = "ez_curl",
      name = "EZ-Curl Bar",
      weightLbs = 25.0,
      weightKg = 10.0,
      description = "Angled ergonomic bar for curls, triceps & upright rows"
    )

    val TRAP_HEX = BarType(
      id = "trap_hex",
      name = "Trap / Hex Bar",
      weightLbs = 55.0,
      weightKg = 25.0,
      description = "Hexagonal frame bar for neutral-grip deadlifts & shrugs"
    )

    val SAFETY_SQUAT = BarType(
      id = "safety_squat",
      name = "Safety Squat Bar (SSB)",
      weightLbs = 65.0,
      weightKg = 30.0,
      description = "Cambered specialty bar with padded neck and shoulder yoke"
    )

    val STANDARD_1_INCH = BarType(
      id = "standard_1inch",
      name = "Standard 1-inch Bar",
      weightLbs = 15.0,
      weightKg = 7.0,
      description = "Home gym threaded or smooth 1-inch standard barbell"
    )

    val CUSTOM = BarType(
      id = "custom",
      name = "Custom Bar Weight",
      weightLbs = 45.0,
      weightKg = 20.0,
      description = "Set any custom bar weight for specialized equipment",
      isCustom = true
    )

    val ALL_PRESETS = listOf(
      OLYMPIC_MENS,
      OLYMPIC_WOMENS,
      TECHNIQUE,
      EZ_CURL,
      TRAP_HEX,
      SAFETY_SQUAT,
      STANDARD_1_INCH,
      CUSTOM
    )
  }
}
