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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BarType
import com.example.model.Plate
import com.example.model.WeightUnit

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
  onAddCustomPlate: (weight: Double, color: Color, availablePairs: Int) -> String? = { _, _, _ -> null },
  onUpdateCustomPlate: (originalPlate: Plate, newWeight: Double, color: Color, availablePairs: Int) -> String? = { _, _, _, _ -> null },
  onDeleteCustomPlate: (Plate) -> Unit = {},
  onUnitChange: (WeightUnit) -> Unit,
  onExclude35s: () -> Unit,
  onEnableAll: () -> Unit,
  onApplyMinimalist: () -> Unit,
  onResetDefaults: () -> Unit,
  onOpenBarSelector: () -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onNavigateBack)

  var showAddPlateDialog by remember { mutableStateOf(false) }
  var editingCustomPlate by remember { mutableStateOf<Plate?>(null) }

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
              text = "The calculator only loads enabled plates up to the pairs you own. Add custom fractional or specialty plates anytime.",
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

        val excludeTargetWeight = if (unit == WeightUnit.LBS) 35.0 else 15.0
        val isExcludedActive = plates.none { it.weight == excludeTargetWeight && it.isEnabled }
        val minimalistSet = if (unit == WeightUnit.LBS) {
          setOf(45.0, 25.0, 10.0, 5.0, 2.5)
        } else {
          setOf(25.0, 20.0, 10.0, 5.0, 2.5)
        }
        val isMinimalistActive = plates.isNotEmpty() && plates.all {
          val shouldKeep = it.weight in minimalistSet
          it.isEnabled == shouldKeep
        }
        val isAllEnabledActive = plates.isNotEmpty() && plates.all { it.isEnabled }

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          PresetChip(
            label = if (unit == WeightUnit.LBS) "Exclude 35 lb Plates" else "Exclude 15 kg Plates",
            selected = isExcludedActive && !isMinimalistActive,
            onClick = onExclude35s,
            testTag = "preset_exclude_35s_button"
          )

          PresetChip(
            label = if (unit == WeightUnit.LBS) "Minimalist (45/25/10/5/2.5)" else "Minimalist (25/20/10/5/2.5)",
            selected = isMinimalistActive,
            onClick = onApplyMinimalist,
            testTag = "preset_minimalist_button"
          )

          PresetChip(
            label = "Enable All Denominations",
            selected = isAllEnabledActive,
            onClick = onEnableAll,
            testTag = "preset_enable_all_button"
          )
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
        Column(modifier = Modifier.weight(1f)) {
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

    // 3. Plate Inventory Toggles & Custom Plates (Main section)
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
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Available Plates (${unit.shortLabel.uppercase()})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            val activeCount = plates.count { it.isEnabled && it.availablePairs > 0 }
            Text(
              text = "$activeCount of ${plates.size} plate types active in gym",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          FilledTonalButton(
            onClick = { showAddPlateDialog = true },
            modifier = Modifier.testTag("add_custom_plate_button")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Add Plate",
              fontWeight = FontWeight.Bold
            )
          }
        }

        if (plates.none { it.isEnabled && it.availablePairs > 0 }) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "All plates are currently disabled. Enable at least one plate or add a custom plate so the calculator can load weights.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.padding(12.dp)
            )
          }
        }

        // List of plates
        plates.forEach { plate ->
          PlateInventorySettingRow(
            plate = plate,
            unit = unit,
            onToggle = { onTogglePlate(plate.weight) },
            onUpdatePairs = { pairs -> onUpdatePairs(plate.weight, pairs) },
            onEditCustom = if (plate.isCustom) {
              { editingCustomPlate = plate }
            } else null,
            onDeleteCustom = if (plate.isCustom) {
              { onDeleteCustomPlate(plate) }
            } else null
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

  if (showAddPlateDialog) {
    CustomPlateDialog(
      unit = unit,
      initialPlate = null,
      onSave = { weight, color, pairs ->
        onAddCustomPlate(weight, color, pairs)
      },
      onDismiss = { showAddPlateDialog = false }
    )
  }

  editingCustomPlate?.let { targetPlate ->
    CustomPlateDialog(
      unit = unit,
      initialPlate = targetPlate,
      onSave = { weight, color, pairs ->
        onUpdateCustomPlate(targetPlate, weight, color, pairs)
      },
      onDelete = { onDeleteCustomPlate(targetPlate) },
      onDismiss = { editingCustomPlate = null }
    )
  }
}

@Composable
private fun PresetChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (selected) {
      MaterialTheme.colorScheme.primary
    } else {
      MaterialTheme.colorScheme.surfaceVariant
    },
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      if (selected) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.size(14.dp)
        )
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = if (selected) {
          MaterialTheme.colorScheme.onPrimary
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        }
      )
    }
  }
}
