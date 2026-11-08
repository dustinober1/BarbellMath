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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.model.PlateCalculationResult
import com.example.ui.theme.PlateGreen

@Composable
fun PlateBreakdownCard(
  result: PlateCalculationResult,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("plate_breakdown_card"),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Plates Per Side",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (result.platesPerSide.isNotEmpty()) {
              "Total: ${result.unit.format(result.plateWeightPerSide)} per side"
            } else {
              "Empty Bar"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Pill badge with count
        if (result.platesPerSide.isNotEmpty()) {
          val platesCountSide = result.platesPerSide.sumOf { it.quantity }
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "$platesCountSide ${if (platesCountSide == 1) "PLATE" else "PLATES"}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        }
      }

      // Remainder / Under Bar Warning banner if applicable
      if (result.isUnderBarWeight) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          color = MaterialTheme.colorScheme.errorContainer,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "Target (${result.unit.format(result.targetWeight)}) is less than the bar weight (${result.unit.format(result.barWeight)}). Minimum load is the bar itself.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onErrorContainer
            )
          }
        }
      } else if (!result.isExact) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          color = MaterialTheme.colorScheme.tertiaryContainer,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "Achieved ${result.unit.format(result.achievedTotalWeight)} (${result.unit.formatPlain(result.remainder)} ${result.unit.shortLabel} remaining). Check gym plate inventory to add smaller microplates.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Plates List (Individual Rows)
      if (result.platesPerSide.isNotEmpty()) {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          result.platesPerSide.forEachIndexed { index, plateCount ->
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = Color(0xFFF7F2FA),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFEADDFF), RoundedCornerShape(16.dp))
                .testTag("plate_item_row_${plateCount.plate.formattedWeight}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                // Left: Plate Color Token & Weight Label
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  // Colored Plate Avatar Token
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(plateCount.plate.color)
                      .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = plateCount.plate.formattedWeight,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Black,
                      color = plateCount.plate.textColor,
                      fontSize = 12.sp
                    )
                  }

                  Column {
                    Text(
                      text = "${plateCount.plate.displayLabel} Plate",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Position #${index + 1} (${if (index == 0) "Inside next to collar" else "Slide on next"})",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                // Right: Multiplier badge & Subtotal
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                  ) {
                    Text(
                      text = "${plateCount.quantity} ×",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Black,
                      color = MaterialTheme.colorScheme.onPrimary,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                  }

                  Text(
                    text = "= ${result.unit.formatPlain(plateCount.totalWeightPerSide)} ${result.unit.shortLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Just lift the empty bar! No plates needed.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(12.dp))

      // Summary Total Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Total Weight Breakdown",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Bar (${result.unit.formatPlain(result.barWeight)}) + Plates (2 × ${result.unit.formatPlain(result.plateWeightPerSide)})" +
              if (result.collarsWeight > 0) " + Collars (${result.unit.formatPlain(result.collarsWeight)})" else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Text(
          text = result.unit.format(result.achievedTotalWeight),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}
