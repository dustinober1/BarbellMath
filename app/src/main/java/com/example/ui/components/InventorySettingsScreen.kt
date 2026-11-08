package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BarType
import com.example.model.Plate
import com.example.model.WeightUnit
import com.example.ui.theme.PlateGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventorySettingsScreen(
  plates: List<Plate>,
  unit: WeightUnit,
  selectedBar: BarType,
  customBarWeight: Double,
  collarsEnabled: Boolean,
  onTogglePlate: (Double) -> Unit,
  onUpdatePairs: (Double, Int) -> Unit,
  onUnitChange: (WeightUnit) -> Unit,
  onExclude35s: () -> Unit,
  onEnableAll: () -> Unit,
  onApplyMinimalist: () -> Unit,
  onResetDefaults: () -> Unit,
  onOpenBarSelector: () -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Support system back press to return to main calculator tab
  BackHandler(onBack = onNavigateBack)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("inventory_settings_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header Card
    Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
      ),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp)
              )
            }
            Column {
              Text(
                text = "Home Gym Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Configure your available plates & gear",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onResetDefaults)
              .testTag("settings_reset_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset Defaults",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Reset",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        // Info banner explaining inventory behavior
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "The calculator only loads plates that are toggled on below. Excluded plates (like 35 lb plates) will never be suggested.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        // Quick Presets
        Text(
          text = "Quick Presets",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (plates.none { it.weight == 35.0 && it.isEnabled }) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onExclude35s)
              .testTag("preset_exclude_35s_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              if (plates.none { it.weight == 35.0 && it.isEnabled }) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(14.dp)
                )
              }
              Text(
                text = "Exclude 35 lb Plates",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (plates.none { it.weight == 35.0 && it.isEnabled }) {
                  MaterialTheme.colorScheme.onPrimary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                }
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onApplyMinimalist)
              .testTag("preset_minimalist_button")
          ) {
            Text(
              text = "Minimalist (45/25/10/5)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onEnableAll)
              .testTag("preset_enable_all_button")
          ) {
            Text(
              text = "Enable All Denominations",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
          }
        }
      }
    }

    // 2. Unit Switcher Card
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
      ),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Active Weight Unit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Switch between Imperial (lbs) and Metric (kg)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Segmented Unit Switcher
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.padding(4.dp)
        ) {
          Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (unit == WeightUnit.LBS) MaterialTheme.colorScheme.primary else Color.Transparent,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onUnitChange(WeightUnit.LBS) }
                .testTag("settings_unit_lbs")
            ) {
              Text(
                text = "LBS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (unit == WeightUnit.LBS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (unit == WeightUnit.KG) MaterialTheme.colorScheme.primary else Color.Transparent,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onUnitChange(WeightUnit.KG) }
                .testTag("settings_unit_kg")
            ) {
              Text(
                text = "KG",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (unit == WeightUnit.KG) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      }
    }

    // 3. Plate Inventory Toggles (Main section)
    Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
      ),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Available Plates (${unit.shortLabel.uppercase()})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            val activeCount = plates.count { it.isEnabled }
            Text(
              text = "$activeCount of ${plates.size} plate types active in gym",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // List of plates
        plates.forEach { plate ->
          PlateInventorySettingRow(
            plate = plate,
            unit = unit,
            onToggle = { onTogglePlate(plate.weight) },
            onUpdatePairs = { pairs -> onUpdatePairs(plate.weight, pairs) }
          )
        }
      }
    }

    // 4. Equipment Defaults Card (Barbell & Collars)
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
      ),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Default Barbell",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenBarSelector)
            .testTag("settings_bar_selector_card")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.FitnessCenter,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(18.dp)
                )
              }

              Column {
                Text(
                  text = selectedBar.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${unit.format(if (selectedBar.isCustom) customBarWeight else selectedBar.getWeight(unit))}" +
                    if (collarsEnabled) " • + Collars" else "",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = "Change",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PlateInventorySettingRow(
  plate: Plate,
  unit: WeightUnit,
  onToggle: () -> Unit,
  onUpdatePairs: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val is35 = (plate.weight == 35.0 && unit == WeightUnit.LBS) || (plate.weight == 15.0 && unit == WeightUnit.KG)

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = if (plate.isEnabled) {
      Color(0xFFF7F2FA)
    } else {
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    },
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (plate.isEnabled) Color(0xFFEADDFF) else Color.Transparent
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("settings_plate_row_${plate.formattedWeight}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Plate Avatar and Details
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(plate.color.copy(alpha = if (plate.isEnabled) 1.0f else 0.35f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "${plate.formattedWeight} ${unit.shortLabel}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (plate.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            if (is35) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
              ) {
                Text(
                  text = "35s",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  fontSize = 9.sp,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
          }

          Text(
            text = if (plate.isEnabled) {
              "${plate.availablePairs} pairs in gym"
            } else {
              "Excluded from calculations"
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = if (plate.isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
          )
        }
      }

      // Right: Pair Steppers & Toggle Switch
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (plate.isEnabled) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            IconButton(
              onClick = { if (plate.availablePairs > 1) onUpdatePairs(plate.availablePairs - 1) },
              modifier = Modifier
                .size(30.dp)
                .testTag("decrement_pairs_${plate.formattedWeight}")
            ) {
              Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease pairs",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }

            Text(
              text = "${plate.availablePairs}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 4.dp)
            )

            IconButton(
              onClick = { onUpdatePairs(plate.availablePairs + 1) },
              modifier = Modifier
                .size(30.dp)
                .testTag("increment_pairs_${plate.formattedWeight}")
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase pairs",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        // Toggle Switch
        Switch(
          checked = plate.isEnabled,
          onCheckedChange = { onToggle() },
          colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          modifier = Modifier.testTag("switch_plate_${plate.formattedWeight}")
        )
      }
    }
  }
}
