package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Plate
import com.example.model.WeightUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomPlateDialog(
  unit: WeightUnit,
  initialPlate: Plate? = null,
  onSave: (weight: Double, color: Color, availablePairs: Int) -> String?,
  onDelete: (() -> Unit)? = null,
  onDismiss: () -> Unit
) {
  val isEditing = initialPlate != null
  var weightInput by remember(initialPlate) {
    mutableStateOf(initialPlate?.formattedWeight ?: "")
  }
  var selectedColor by remember(initialPlate) {
    mutableStateOf(initialPlate?.color ?: Plate.COLOR_PALETTE[6].second)
  }
  var pairsCount by remember(initialPlate) {
    mutableIntStateOf(initialPlate?.availablePairs ?: 1)
  }
  var errorText by remember { mutableStateOf<String?>(null) }

  val fractionalPresets = remember(unit) {
    if (unit == WeightUnit.LBS) {
      listOf(0.25, 0.5, 0.75, 1.0, 1.25, 2.0, 2.5)
    } else {
      listOf(0.25, 0.5, 1.0, 1.25, 1.5, 2.0, 2.5)
    }
  }

  val parsedPreviewWeight = weightInput.toDoubleOrNull()?.takeIf { it > 0.0 }
  val previewLabel = parsedPreviewWeight?.let { Plate.formatWeight(it) } ?: "?"
  val previewTextColor = Plate.contrastingTextColor(selectedColor)

  fun submit() {
    val parsed = weightInput.trim().toDoubleOrNull()
    if (parsed == null || parsed <= 0.0) {
      errorText = "Enter a valid positive weight"
      return
    }
    val saveError = onSave(parsed, selectedColor, pairsCount)
    if (saveError != null) {
      errorText = saveError
    } else {
      onDismiss()
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("custom_plate_dialog"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isEditing) "Edit Custom Plate" else "Add Custom Plate",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primaryContainer
        ) {
          Text(
            text = unit.shortLabel.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Live preview + Weight input row
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(selectedColor)
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = previewLabel,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Black,
              color = previewTextColor,
              fontSize = if (previewLabel.length > 3) 10.sp else 13.sp
            )
          }

          OutlinedTextField(
            value = weightInput,
            onValueChange = {
              weightInput = it
              errorText = null
            },
            label = { Text("Weight per plate") },
            placeholder = { Text("e.g. 1.25") },
            suffix = { Text(unit.shortLabel, fontWeight = FontWeight.Bold) },
            isError = errorText != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Decimal,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier
              .weight(1f)
              .testTag("custom_plate_weight_input")
          )
        }

        if (errorText != null) {
          Text(
            text = errorText!!,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag("custom_plate_error_text")
          )
        }

        // Quick fractional increments
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Quick Fractional Presets",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            fractionalPresets.forEach { preset ->
              val label = Plate.formatWeight(preset)
              val isSelected = parsedPreviewWeight != null && kotlin.math.abs(parsedPreviewWeight - preset) < 0.001
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .clickable {
                    weightInput = label
                    errorText = null
                  }
                  .testTag("preset_fractional_$label")
              ) {
                Text(
                  text = "$label ${unit.shortLabel}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
              }
            }
          }
        }

        // Pairs available stepper
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Pairs Available",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${pairsCount * 2} individual plates",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            IconButton(
              onClick = { if (pairsCount > 1) pairsCount-- },
              modifier = Modifier
                .size(48.dp)
                .testTag("custom_plate_pairs_dec")
            ) {
              Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease pairs",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Text(
              text = "$pairsCount",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 4.dp)
            )

            IconButton(
              onClick = { if (pairsCount < 99) pairsCount++ },
              modifier = Modifier
                .size(48.dp)
                .testTag("custom_plate_pairs_inc")
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase pairs",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        // Plate color swatches
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Plate Color",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Plate.COLOR_PALETTE.forEach { (colorName, swatch) ->
              val isChosen = selectedColor == swatch
              val checkTint = Plate.contrastingTextColor(swatch)
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(swatch)
                  .border(
                    width = if (isChosen) 3.dp else 1.dp,
                    color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                  )
                  .clickable { selectedColor = swatch }
                  .testTag("custom_plate_color_$colorName"),
                contentAlignment = Alignment.Center
              ) {
                if (isChosen) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected $colorName plate color",
                    tint = checkTint,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }

        if (isEditing && onDelete != null) {
          TextButton(
            onClick = {
              onDelete()
              onDismiss()
            },
            modifier = Modifier.testTag("dialog_delete_custom_plate_button")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete custom plate",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Delete Custom Plate",
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { submit() },
        modifier = Modifier.testTag("save_custom_plate_button")
      ) {
        Text(if (isEditing) "Save Changes" else "Add Plate")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_custom_plate_button")
      ) {
        Text("Cancel")
      }
    }
  )
}
