package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BarType
import com.example.model.Plate
import com.example.model.WeightUnit
import com.example.ui.theme.AccentAmber

@Composable
fun TargetWeightControlCard(
  targetWeight: Double,
  unit: WeightUnit,
  selectedBar: BarType,
  customBarWeight: Double,
  onTargetWeightChange: (Double) -> Unit,
  onAdjustWeight: (Double) -> Unit,
  onUnitChange: (WeightUnit) -> Unit,
  onOpenBarSelector: () -> Unit,
  availablePlates: List<Plate> = emptyList(),
  modifier: Modifier = Modifier
) {
  var isCustomInputOpen by remember { mutableStateOf(false) }

  val effectiveBarWeight = if (selectedBar.isCustom) customBarWeight else selectedBar.getWeight(unit)
  val barWeightText = unit.format(effectiveBarWeight)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("target_weight_control_card"),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      horizontalAlignment = Alignment.Start
    ) {
      // Top row: Unit Switcher (LBS / KG) & Bar Selector shortcut
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "TARGET WEIGHT (${unit.shortLabel.uppercase()})",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.2.sp,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(start = 4.dp)
        )

        // Unit Switcher (LBS / KG) in sleek pill container
        Row(
          modifier = Modifier
            .background(Color(0xFFE7E0EC), RoundedCornerShape(16.dp))
            .padding(3.dp)
        ) {
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { onUnitChange(WeightUnit.LBS) }
              .testTag("unit_lbs_button"),
            color = if (unit == WeightUnit.LBS) MaterialTheme.colorScheme.surface else Color.Transparent,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = if (unit == WeightUnit.LBS) 2.dp else 0.dp
          ) {
            Text(
              text = "LBS",
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (unit == WeightUnit.LBS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { onUnitChange(WeightUnit.KG) }
              .testTag("unit_kg_button"),
            color = if (unit == WeightUnit.KG) MaterialTheme.colorScheme.surface else Color.Transparent,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = if (unit == WeightUnit.KG) 2.dp else 0.dp
          ) {
            Text(
              text = "KG",
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (unit == WeightUnit.KG) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Main High-Contrast Sleek Target Weight Input Box
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
          .clickable { isCustomInputOpen = true }
          .testTag("target_weight_display_box"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = unit.formatPlain(targetWeight),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 42.sp
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = unit.shortLabel.uppercase(),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit weight directly",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Barbell Type Selector Pill Header
      Text(
        text = "BARBELL TYPE",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Barbell Type Segmented Container
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFFE7E0EC), RoundedCornerShape(18.dp))
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        val isOlympicMen = selectedBar.id == "olympic_mens"
        val isOlympicWomen = selectedBar.id == "olympic_womens"
        val isOtherBar = !isOlympicMen && !isOlympicWomen

        // Standard Bar
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onOpenBarSelector() }
            .testTag("bar_standard_pill"),
          color = if (isOlympicMen) MaterialTheme.colorScheme.surface else Color.Transparent,
          shape = RoundedCornerShape(14.dp),
          shadowElevation = if (isOlympicMen) 2.dp else 0.dp
        ) {
          Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Standard",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (isOlympicMen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = if (unit == WeightUnit.LBS) "45 lb" else "20 kg",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = if (isOlympicMen) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Women's Bar
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onOpenBarSelector() }
            .testTag("bar_womens_pill"),
          color = if (isOlympicWomen) MaterialTheme.colorScheme.surface else Color.Transparent,
          shape = RoundedCornerShape(14.dp),
          shadowElevation = if (isOlympicWomen) 2.dp else 0.dp
        ) {
          Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Women's",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (isOlympicWomen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = if (unit == WeightUnit.LBS) "35 lb" else "15 kg",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = if (isOlympicWomen) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Other / Custom Bar
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onOpenBarSelector() }
            .testTag("bar_other_pill"),
          color = if (isOtherBar) MaterialTheme.colorScheme.surface else Color.Transparent,
          shape = RoundedCornerShape(14.dp),
          shadowElevation = if (isOtherBar) 2.dp else 0.dp
        ) {
          Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = if (isOtherBar) selectedBar.name.take(9) else "Other Bars",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (isOtherBar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = if (isOtherBar) barWeightText else "More ▾",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = if (isOtherBar) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Quick-Step Increment Buttons
      Text(
        text = "QUICK ADJUST",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      val baseSteps = if (unit == WeightUnit.LBS) {
        listOf(2.5, 5.0, 10.0, 25.0, 45.0)
      } else {
        listOf(1.0, 2.5, 5.0, 10.0, 20.0, 25.0)
      }
      val smallestPairIncrement = availablePlates
        .filter { it.isEnabled && it.availablePairs > 0 && it.weight > 0.0 }
        .minOfOrNull { kotlin.math.round(it.weight * 2.0 * 100.0) / 100.0 }
      val stepValues = remember(baseSteps, smallestPairIncrement) {
        if (smallestPairIncrement != null && baseSteps.none { kotlin.math.abs(it - smallestPairIncrement) < 0.001 }) {
          (listOf(smallestPairIncrement) + baseSteps).distinct().sorted()
        } else {
          baseSteps
        }
      }

      // Decrement row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        stepValues.reversed().forEach { step ->
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { onAdjustWeight(-step) }
              .testTag("minus_${step}_button"),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "-${unit.formatPlain(step)}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.error,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Increment row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        stepValues.forEach { step ->
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { onAdjustWeight(step) }
              .testTag("plus_${step}_button"),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "+${unit.formatPlain(step)}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
          }
        }
      }
    }
  }

  // Custom Weight Direct Input Dialog
  if (isCustomInputOpen) {
    var inputText by remember { mutableStateOf(unit.formatPlain(targetWeight)) }

    AlertDialog(
      onDismissRequest = { isCustomInputOpen = false },
      title = {
        Text("Enter Target Weight", fontWeight = FontWeight.Bold)
      },
      text = {
        Column {
          Text(
            text = "Total weight including the bar (${unit.label}):",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Decimal,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = {
                val weight = inputText.toDoubleOrNull()
                if (weight != null && weight >= 0) {
                  onTargetWeightChange(weight)
                }
                isCustomInputOpen = false
              }
            ),
            singleLine = true,
            suffix = { Text(unit.shortLabel, fontWeight = FontWeight.Bold) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("custom_weight_input_field")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val weight = inputText.toDoubleOrNull()
            if (weight != null && weight >= 0) {
              onTargetWeightChange(weight)
            }
            isCustomInputOpen = false
          },
          modifier = Modifier.testTag("confirm_weight_button")
        ) {
          Text("Set Weight")
        }
      },
      dismissButton = {
        TextButton(onClick = { isCustomInputOpen = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
