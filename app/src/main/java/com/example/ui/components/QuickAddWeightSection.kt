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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WeightUnit
import com.example.ui.theme.PlateBlack
import com.example.ui.theme.PlateBlue
import com.example.ui.theme.PlateGreen
import com.example.ui.theme.PlateRed
import com.example.ui.theme.PlateWhite
import com.example.ui.theme.PlateYellow

data class QuickAddPreset(
  val delta: Double,
  val label: String,
  val comboHint: String,
  val plateBadgeText: String,
  val accentColor: Color,
  val testTag: String
)

object QuickAddPresets {
  fun forUnit(unit: WeightUnit): List<QuickAddPreset> {
    return if (unit == WeightUnit.LBS) {
      listOf(
        QuickAddPreset(
          delta = 5.0,
          label = "Add 5lb",
          comboHint = "2×2.5 lb",
          plateBadgeText = "2.5",
          accentColor = PlateBlack,
          testTag = "quick_add_5_button"
        ),
        QuickAddPreset(
          delta = 10.0,
          label = "Add 10lb",
          comboHint = "2×5 lb",
          plateBadgeText = "5",
          accentColor = PlateWhite,
          testTag = "quick_add_10_button"
        ),
        QuickAddPreset(
          delta = 20.0,
          label = "Add 20lb",
          comboHint = "2×10 lb",
          plateBadgeText = "10",
          accentColor = PlateGreen,
          testTag = "quick_add_20_button"
        ),
        QuickAddPreset(
          delta = 25.0,
          label = "Add 25lb",
          comboHint = "10+2.5 /side",
          plateBadgeText = "25",
          accentColor = PlateYellow,
          testTag = "quick_add_25_button"
        ),
        QuickAddPreset(
          delta = 50.0,
          label = "Add 50lb",
          comboHint = "2×25 lb",
          plateBadgeText = "25",
          accentColor = PlateYellow,
          testTag = "quick_add_50_button"
        ),
        QuickAddPreset(
          delta = 90.0,
          label = "Add 90lb",
          comboHint = "2×45 lb",
          plateBadgeText = "45",
          accentColor = PlateRed,
          testTag = "quick_add_90_button"
        )
      )
    } else {
      listOf(
        QuickAddPreset(
          delta = 5.0,
          label = "Add 5kg",
          comboHint = "2×2.5 kg",
          plateBadgeText = "2.5",
          accentColor = PlateBlack,
          testTag = "quick_add_5_button"
        ),
        QuickAddPreset(
          delta = 10.0,
          label = "Add 10kg",
          comboHint = "2×5 kg",
          plateBadgeText = "5",
          accentColor = PlateWhite,
          testTag = "quick_add_10_button"
        ),
        QuickAddPreset(
          delta = 20.0,
          label = "Add 20kg",
          comboHint = "2×10 kg",
          plateBadgeText = "10",
          accentColor = PlateGreen,
          testTag = "quick_add_20_button"
        ),
        QuickAddPreset(
          delta = 30.0,
          label = "Add 30kg",
          comboHint = "2×15 kg",
          plateBadgeText = "15",
          accentColor = PlateYellow,
          testTag = "quick_add_30_button"
        ),
        QuickAddPreset(
          delta = 40.0,
          label = "Add 40kg",
          comboHint = "2×20 kg",
          plateBadgeText = "20",
          accentColor = PlateBlue,
          testTag = "quick_add_40_button"
        ),
        QuickAddPreset(
          delta = 50.0,
          label = "Add 50kg",
          comboHint = "2×25 kg",
          plateBadgeText = "25",
          accentColor = PlateRed,
          testTag = "quick_add_50_button"
        )
      )
    }
  }
}

@Composable
fun QuickAddWeightSection(
  unit: WeightUnit,
  barWeight: Double,
  onQuickAdd: (Double) -> Unit,
  onResetToBar: () -> Unit,
  modifier: Modifier = Modifier
) {
  val presets = remember(unit) { QuickAddPresets.forUnit(unit) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("quick_add_section")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "QUICK ADD PLATES",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp)
      )

      Surface(
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .clickable { onResetToBar() }
          .testTag("quick_add_reset_bar_button")
          .semantics {
            contentDescription = "Reset target weight to empty bar (${unit.format(barWeight)})"
          },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.RestartAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "Bar (${unit.formatPlain(barWeight)})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      presets.chunked(3).forEach { rowPresets ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          rowPresets.forEach { preset ->
            QuickAddPresetButton(
              preset = preset,
              onClick = { onQuickAdd(preset.delta) },
              modifier = Modifier.weight(1f)
            )
          }
          repeat(3 - rowPresets.size) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}

@Composable
private fun QuickAddPresetButton(
  preset: QuickAddPreset,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .defaultMinSize(minHeight = 48.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag(preset.testTag)
      .semantics {
        contentDescription = "${preset.label}, ${preset.comboHint}"
      },
    color = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box(
        modifier = Modifier
          .size(18.dp)
          .clip(CircleShape)
          .background(preset.accentColor)
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = null,
          tint = if (preset.accentColor == PlateWhite || preset.accentColor == PlateYellow) {
            Color.Black
          } else {
            Color.White
          },
          modifier = Modifier.size(12.dp)
        )
      }

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = preset.label,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = preset.comboHint,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun QuickAddDialogChips(
  unit: WeightUnit,
  onAddDelta: (Double) -> Unit,
  modifier: Modifier = Modifier
) {
  val dialogPresets = remember(unit) {
    val all = QuickAddPresets.forUnit(unit)
    // Surface the 4 most frequent quick-add increments inside the compact dialog
    if (unit == WeightUnit.LBS) {
      all.filter { it.delta in listOf(10.0, 25.0, 50.0, 90.0) }
    } else {
      all.filter { it.delta in listOf(5.0, 10.0, 20.0, 40.0) }
    }
  }

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = "Quick add plate combos:",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(6.dp))
    Column(
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      dialogPresets.chunked(2).forEach { rowPresets ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          rowPresets.forEach { preset ->
            Surface(
              modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 40.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onAddDelta(preset.delta) }
                .testTag("dialog_${preset.testTag}"),
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = RoundedCornerShape(10.dp)
            ) {
              Box(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = preset.label,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
          repeat(2 - rowPresets.size) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}
