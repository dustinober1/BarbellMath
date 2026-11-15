package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Plate
import com.example.model.WeightUnit

@Composable
fun PlateInventorySettingRow(
  plate: Plate,
  unit: WeightUnit,
  onToggle: () -> Unit,
  onUpdatePairs: (Int) -> Unit,
  onEditCustom: (() -> Unit)? = null,
  onDeleteCustom: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val is35 = (plate.weight == 35.0 && unit == WeightUnit.LBS) ||
    (plate.weight == 15.0 && unit == WeightUnit.KG)
  val isFractional = plate.weight < 2.5

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = if (plate.isEnabled) {
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    } else {
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
    },
    border = BorderStroke(
      1.dp,
      if (plate.isEnabled) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
      } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
      }
    ),
    modifier = modifier
      .fillMaxWidth()
      .then(if (onEditCustom != null) Modifier.clickable(onClick = onEditCustom) else Modifier)
      .testTag("settings_plate_row_${plate.formattedWeight}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Top row: Plate badge + weight/tags + Enable Switch
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(plate.color.copy(alpha = if (plate.isEnabled) 1.0f else 0.35f))
              .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                RoundedCornerShape(10.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = plate.formattedWeight,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Black,
              color = plate.textColor,
              fontSize = if (plate.formattedWeight.length > 3) 10.sp else 12.sp
            )
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "${plate.formattedWeight} ${unit.shortLabel}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (plate.isEnabled) {
                  MaterialTheme.colorScheme.onSurface
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                }
              )

              if (plate.isCustom) {
                PlateTypeBadge(
                  text = "Custom",
                  containerColor = MaterialTheme.colorScheme.secondaryContainer,
                  contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
              } else if (is35) {
                PlateTypeBadge(
                  text = if (unit == WeightUnit.LBS) "35s" else "15s",
                  containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                  contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
              } else if (isFractional) {
                PlateTypeBadge(
                  text = "Fractional",
                  containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                  contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
              }
            }

            Text(
              text = if (plate.isEnabled) {
                "${plate.availablePairs} ${if (plate.availablePairs == 1) "pair" else "pairs"} (${plate.availablePairs * 2} plates)"
              } else {
                "Excluded from calculations"
              },
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium,
              fontSize = 11.sp,
              color = if (plate.isEnabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
              } else {
                MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
              }
            )
          }
        }

        Switch(
          checked = plate.isEnabled,
          onCheckedChange = { onToggle() },
          colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          modifier = Modifier
            .testTag("switch_plate_${plate.formattedWeight}")
            .semantics {
              contentDescription = "Toggle ${plate.formattedWeight} ${unit.shortLabel} plate"
            }
        )
      }

      // Secondary control strip for pairs stepper and custom plate edit/delete actions
      if (plate.isEnabled || plate.isCustom) {
        HorizontalDivider(
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (plate.isCustom && (onEditCustom != null || onDeleteCustom != null)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              if (onEditCustom != null) {
                IconButton(
                  onClick = onEditCustom,
                  modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("edit_custom_plate_${plate.formattedWeight}")
                ) {
                  Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit custom plate",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
              if (onDeleteCustom != null) {
                IconButton(
                  onClick = onDeleteCustom,
                  modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("delete_custom_plate_${plate.formattedWeight}")
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete custom plate",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          } else {
            Text(
              text = "Pairs owned",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          if (plate.isEnabled) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier
                .background(
                  MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                  RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 4.dp)
            ) {
              IconButton(
                onClick = { if (plate.availablePairs > 1) onUpdatePairs(plate.availablePairs - 1) },
                enabled = plate.availablePairs > 1,
                modifier = Modifier
                  .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                  .testTag("decrement_pairs_${plate.formattedWeight}")
              ) {
                Icon(
                  imageVector = Icons.Default.Remove,
                  contentDescription = "Decrease ${plate.formattedWeight} ${unit.shortLabel} pairs",
                  tint = if (plate.availablePairs > 1) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                  },
                  modifier = Modifier.size(18.dp)
                )
              }

              Text(
                text = "${plate.availablePairs}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 6.dp)
              )

              IconButton(
                onClick = { onUpdatePairs(plate.availablePairs + 1) },
                modifier = Modifier
                  .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                  .testTag("increment_pairs_${plate.formattedWeight}")
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Increase ${plate.formattedWeight} ${unit.shortLabel} pairs",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          } else {
            Spacer(modifier = Modifier.size(1.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun PlateTypeBadge(
  text: String,
  containerColor: Color,
  contentColor: Color
) {
  Surface(
    shape = RoundedCornerShape(6.dp),
    color = containerColor
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      fontSize = 9.sp,
      color = contentColor,
      modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
    )
  }
}
