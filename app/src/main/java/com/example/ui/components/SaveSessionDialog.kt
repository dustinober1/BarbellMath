package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.FitnessCenter
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.model.Plate
import com.example.model.PlateCalculationResult
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SaveSessionDialog(
  calculationResult: PlateCalculationResult,
  barName: String,
  onSave: (liftName: String, reps: String, notes: String) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedLift by remember { mutableStateOf("General") }
  var setsCount by remember { mutableIntStateOf(3) }
  var repsCount by remember { mutableIntStateOf(5) }
  var useCustomRepsText by remember { mutableStateOf(false) }
  var customRepsText by remember { mutableStateOf("3×5") }
  var notesText by remember { mutableStateOf("") }

  val resolvedRepsSummary = if (useCustomRepsText) {
    customRepsText.trim()
  } else {
    "${setsCount}×${repsCount}"
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("save_session_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.BookmarkAdd,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Text(
          text = "Log Sets & Reps",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      LogSessionFormContent(
        calculationResult = calculationResult,
        barName = barName,
        selectedLift = selectedLift,
        onSelectLift = { selectedLift = it },
        setsCount = setsCount,
        onSetsChange = {
          setsCount = it.coerceIn(1, 20)
          useCustomRepsText = false
        },
        repsCount = repsCount,
        onRepsChange = {
          repsCount = it.coerceIn(1, 30)
          useCustomRepsText = false
        },
        useCustomRepsText = useCustomRepsText,
        onToggleCustomReps = { enabled ->
          if (enabled && !useCustomRepsText) {
            customRepsText = "${setsCount}×${repsCount}"
          }
          useCustomRepsText = enabled
        },
        customRepsText = customRepsText,
        onCustomRepsTextChange = { customRepsText = it },
        notesText = notesText,
        onNotesTextChange = { notesText = it },
        modifier = Modifier.verticalScroll(rememberScrollState())
      )
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(selectedLift, resolvedRepsSummary, notesText)
          onDismiss()
        },
        modifier = Modifier.testTag("confirm_save_session_button")
      ) {
        Text("Save to History")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_save_session_button")
      ) {
        Text("Cancel")
      }
    }
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogSessionFormContent(
  calculationResult: PlateCalculationResult,
  barName: String,
  selectedLift: String,
  onSelectLift: (String) -> Unit,
  setsCount: Int,
  onSetsChange: (Int) -> Unit,
  repsCount: Int,
  onRepsChange: (Int) -> Unit,
  useCustomRepsText: Boolean,
  onToggleCustomReps: (Boolean) -> Unit,
  customRepsText: String,
  onCustomRepsTextChange: (String) -> Unit,
  notesText: String,
  onNotesTextChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val liftPresets = remember {
    listOf("General", "Squat", "Bench Press", "Deadlift", "Overhead Press", "Barbell Row")
  }
  val setsRepsPresets = remember {
    listOf(
      3 to 5,
      5 to 5,
      3 to 8,
      3 to 10,
      4 to 6,
      1 to 3,
      1 to 1
    )
  }

  val plateSummaryText = remember(calculationResult) {
    if (calculationResult.platesPerSide.isEmpty()) {
      "Empty Bar"
    } else {
      calculationResult.platesPerSide.joinToString(" + ") {
        "${it.quantity}×${it.plate.formattedWeight}"
      } + " per side"
    }
  }

  val totalReps = setsCount * repsCount
  val totalVolume = totalReps * calculationResult.achievedTotalWeight
  val formattedVolume = remember(totalVolume) {
    val nf = NumberFormat.getNumberInstance(Locale.US)
    if (totalVolume % 1.0 == 0.0) {
      nf.format(totalVolume.toLong())
    } else {
      Plate.formatWeight(totalVolume)
    }
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Summary of calculated barbell load
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(18.dp)
          )
          Column {
            Text(
              text = barName,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              text = plateSummaryText,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
          }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = calculationResult.unit.format(calculationResult.achievedTotalWeight),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }

    // Lift / Movement Selection
    Text(
      text = "Exercise / Lift",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      liftPresets.forEach { lift ->
        val isSelected = selectedLift == lift
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelectLift(lift) }
            .testTag("session_lift_chip_${lift.lowercase().replace(" ", "_")}")
        ) {
          Text(
            text = lift,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
          )
        }
      }
    }

    // Sets & Reps Quick Presets + Steppers
    Text(
      text = "Sets & Reps",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      setsRepsPresets.forEach { (s, r) ->
        val isPresetSelected = !useCustomRepsText && setsCount == s && repsCount == r
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (isPresetSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable {
              onSetsChange(s)
              onRepsChange(r)
            }
            .testTag("preset_sets_reps_${s}x${r}")
        ) {
          Text(
            text = if (s == 1 && r == 1) "1RM" else "${s}×${r}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPresetSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (useCustomRepsText) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .clickable { onToggleCustomReps(!useCustomRepsText) }
          .testTag("preset_sets_reps_custom")
      ) {
        Text(
          text = "Custom",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = if (useCustomRepsText) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        )
      }
    }

    if (!useCustomRepsText) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        CountStepperBox(
          label = "Sets",
          value = setsCount,
          minValue = 1,
          maxValue = 20,
          onValueChange = onSetsChange,
          decTag = "log_sets_dec",
          incTag = "log_sets_inc",
          valueTag = "log_sets_value",
          modifier = Modifier.weight(1f)
        )
        CountStepperBox(
          label = "Reps",
          value = repsCount,
          minValue = 1,
          maxValue = 30,
          onValueChange = onRepsChange,
          decTag = "log_reps_dec",
          incTag = "log_reps_inc",
          valueTag = "log_reps_value",
          modifier = Modifier.weight(1f)
        )
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "$setsCount × $repsCount = $totalReps total reps • $formattedVolume ${calculationResult.unit.shortLabel} volume",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("log_volume_summary")
        )
      }
    } else {
      OutlinedTextField(
        value = customRepsText,
        onValueChange = onCustomRepsTextChange,
        label = { Text("Custom Sets / Reps") },
        placeholder = { Text("e.g. 5×5 or 3×5+ AMRAP") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("session_reps_input")
      )
    }

    OutlinedTextField(
      value = notesText,
      onValueChange = onNotesTextChange,
      label = { Text("Notes (optional)") },
      placeholder = { Text("e.g. Felt fast, RPE 8") },
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("session_notes_input")
    )
  }
}

@Composable
private fun CountStepperBox(
  label: String,
  value: Int,
  minValue: Int,
  maxValue: Int,
  onValueChange: (Int) -> Unit,
  decTag: String,
  incTag: String,
  valueTag: String,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 6.dp, vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { if (value > minValue) onValueChange(value - 1) },
          enabled = value > minValue,
          modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .testTag(decTag)
        ) {
          Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "Decrease $label",
            modifier = Modifier.size(18.dp)
          )
        }

        Text(
          text = "$value",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.testTag(valueTag)
        )

        IconButton(
          onClick = { if (value < maxValue) onValueChange(value + 1) },
          enabled = value < maxValue,
          modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .testTag(incTag)
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Increase $label",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}
