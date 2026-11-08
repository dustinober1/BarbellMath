package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Plate
import com.example.model.WeightUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryManagerSheet(
  plates: List<Plate>,
  unit: WeightUnit,
  onTogglePlateEnabled: (Double) -> Unit,
  onUpdateAvailablePairs: (Double, Int) -> Unit,
  onResetInventory: () -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("inventory_modal_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Gym Plate Inventory (${unit.shortLabel.uppercase()})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Enable/disable plate denominations and pair quantities",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = onResetInventory,
          modifier = Modifier.testTag("reset_inventory_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reset Defaults",
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f, fill = false)
      ) {
        items(plates) { plate ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (plate.isEnabled) 0.6f else 0.25f),
            modifier = Modifier
              .fillMaxWidth()
              .border(
                1.dp,
                if (plate.isEnabled) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                RoundedCornerShape(16.dp)
              )
              .testTag("inventory_plate_${plate.formattedWeight}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // Left: Plate Color Token & Label
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(plate.color.copy(alpha = if (plate.isEnabled) 1.0f else 0.4f)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = plate.formattedWeight,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = plate.textColor,
                    fontSize = 12.sp
                  )
                }

                Column {
                  Text(
                    text = "${plate.formattedWeight} ${unit.shortLabel} Plate",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (plate.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = if (plate.isEnabled) "${plate.availablePairs} pairs available" else "Disabled in calculation",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              // Right: Pair stepper & Toggle switch
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                if (plate.isEnabled) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                      .padding(horizontal = 4.dp, vertical = 2.dp)
                  ) {
                    IconButton(
                      onClick = { onUpdateAvailablePairs(plate.weight, plate.availablePairs - 1) },
                      modifier = Modifier.size(24.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease pairs",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                      )
                    }
                    Text(
                      text = "${plate.availablePairs}",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                      onClick = { onUpdateAvailablePairs(plate.weight, plate.availablePairs + 1) },
                      modifier = Modifier.size(24.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase pairs",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }

                Switch(
                  checked = plate.isEnabled,
                  onCheckedChange = { onTogglePlateEnabled(plate.weight) },
                  modifier = Modifier.testTag("toggle_plate_switch_${plate.formattedWeight}")
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = onDismiss,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("close_inventory_button")
      ) {
        Text("Done")
      }
    }
  }
}
