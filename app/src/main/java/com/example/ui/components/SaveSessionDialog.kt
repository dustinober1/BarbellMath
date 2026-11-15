package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.model.PlateCalculationResult

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SaveSessionDialog(
  calculationResult: PlateCalculationResult,
  barName: String,
  onSave: (liftName: String, reps: String, notes: String) -> Unit,
  onDismiss: () -> Unit
) {
  val liftPresets = remember {
    listOf("General", "Squat", "Bench Press", "Deadlift", "Overhead Press", "Barbell Row")
  }
  var selectedLift by remember { mutableStateOf("General") }
  var repsText by remember { mutableStateOf("") }
  var notesText by remember { mutableStateOf("") }

  val plateSummaryText = remember(calculationResult) {
    if (calculationResult.platesPerSide.isEmpty()) {
      "Empty Bar"
    } else {
      calculationResult.platesPerSide.joinToString(" + ") {
        "${it.quantity}×${it.plate.formattedWeight}"
      } + " per side"
    }
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
          text = "Save to Recent Sessions",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                .clickable { selectedLift = lift }
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

        OutlinedTextField(
          value = repsText,
          onValueChange = { repsText = it },
          label = { Text("Sets / Reps (optional)") },
          placeholder = { Text("e.g. 5×5 or 3 reps") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("session_reps_input")
        )

        OutlinedTextField(
          value = notesText,
          onValueChange = { notesText = it },
          label = { Text("Notes (optional)") },
          placeholder = { Text("e.g. Felt fast, RPE 8") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("session_notes_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(selectedLift, repsText, notesText)
          onDismiss()
        },
        modifier = Modifier.testTag("confirm_save_session_button")
      ) {
        Text("Save Session")
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
