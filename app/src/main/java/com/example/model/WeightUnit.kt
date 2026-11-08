package com.example.model

enum class WeightUnit(val label: String, val shortLabel: String) {
  LBS("Pounds", "lbs"),
  KG("Kilograms", "kg");

  fun format(weight: Double): String {
    return if (weight % 1.0 == 0.0) {
      "${weight.toInt()} $shortLabel"
    } else {
      String.format("%.2f", weight).trimEnd('0').trimEnd('.') + " $shortLabel"
    }
  }

  fun formatPlain(weight: Double): String {
    return if (weight % 1.0 == 0.0) {
      "${weight.toInt()}"
    } else {
      String.format("%.2f", weight).trimEnd('0').trimEnd('.')
    }
  }
}
