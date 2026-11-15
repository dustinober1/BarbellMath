package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.model.BarType
import com.example.model.Plate
import com.example.model.PlateCount
import com.example.model.WeightUnit
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.Slate700

@Composable
fun ReversePlateBuilderSection(
  reversePlatesPerSide: List<PlateCount>,
  reverseTotalWeight: Double,
  availablePlates: List<Plate>,
  bar: BarType,
  customBarWeight: Double,
  collarsEnabled: Boolean,
  unit: WeightUnit,
  onAddPlate: (Plate) -> Unit,
  onRemovePlate: (Plate) -> Unit,
  onClearPlates: () -> Unit,
  onApplyToTarget: () -> Unit,
  modifier: Modifier = Modifier
) {
  val barWeight = if (bar.isCustom) customBarWeight else bar.getWeight(unit)
  val collarsWeight = if (collarsEnabled) (if (unit == WeightUnit.LBS) 1.0 else 0.5) else 0.0

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("reverse_plate_builder_card"),
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
            text = "Reverse Plate Racker",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Tap plates to load onto the bar & calculate total weight",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (reversePlatesPerSide.isNotEmpty()) {
          IconButton(
            onClick = onClearPlates,
            modifier = Modifier.testTag("clear_reverse_plates_button")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Clear bar",
              tint = MaterialTheme.colorScheme.error
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Canvas Preview of reverse racked barbell
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(DarkSurfaceVariant)
          .border(1.dp, Slate700, RoundedCornerShape(16.dp))
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        SingleSleeveCanvas(
          platesPerSide = reversePlatesPerSide,
          unit = unit
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Calculated Total Barbell Load Output Card
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Calculated Total Weight",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              text = "Bar (${unit.formatPlain(barWeight)}) + Plates on both sides" +
                if (collarsWeight > 0) " + Collars (${unit.formatPlain(collarsWeight)})" else "",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
          }

          Text(
            text = unit.format(reverseTotalWeight),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Plate Tap Grid / Rack Buttons
      Text(
        text = "Tap plate to add to both sides (+1 pair):",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Grid of available plate chips to tap
      val enabledPlates = availablePlates.filter { it.isEnabled && it.availablePairs > 0 }
      if (enabledPlates.isEmpty()) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "No plates are enabled in Settings. Enable or add custom plates to rack them.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp)
          )
        }
      } else {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          enabledPlates.chunked(3).forEach { rowPlates ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              rowPlates.forEach { plate ->
                val loadedCount = reversePlatesPerSide.find { kotlin.math.abs(it.plate.weight - plate.weight) < 0.001 }?.quantity ?: 0
                val atCapacity = loadedCount >= plate.availablePairs

                Surface(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                      width = if (loadedCount > 0) 2.dp else 1.dp,
                      color = if (loadedCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                      shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = !atCapacity) { onAddPlate(plate) }
                    .testTag("rack_plate_${plate.formattedWeight}"),
                  color = if (atCapacity) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant,
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Box(
                        modifier = Modifier
                          .size(24.dp)
                          .clip(RoundedCornerShape(6.dp))
                          .background(plate.color),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = plate.formattedWeight,
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Black,
                          color = plate.textColor,
                          fontSize = if (plate.formattedWeight.length > 3) 8.sp else 9.sp
                        )
                      }
                      Text(
                        text = "${plate.formattedWeight} ${unit.shortLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }

                    if (loadedCount > 0) {
                      Surface(
                        color = if (atCapacity) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                      ) {
                        Text(
                          text = if (atCapacity) "$loadedCount/${plate.availablePairs}" else "$loadedCount",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Black,
                          color = if (atCapacity) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary,
                          modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                          fontSize = 10.sp
                        )
                      }
                    }
                  }
                }
              }
              // Fill empty cells if row < 3
              repeat(3 - rowPlates.size) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }
      }

      // Loaded Plates Management Rows (with Minus buttons)
      if (reversePlatesPerSide.isNotEmpty()) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Currently Loaded On Bar (Per Side):",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        Column(
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          reversePlatesPerSide.forEach { item ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(18.dp)
                      .clip(RoundedCornerShape(4.dp))
                      .background(item.plate.color)
                  )
                  Text(
                    text = "${item.quantity} × ${item.plate.displayLabel} (both sides: ${item.quantity * 2} plates)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  IconButton(
                    onClick = { onRemovePlate(item.plate) },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Remove,
                      contentDescription = "Remove one",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  IconButton(
                    onClick = { onAddPlate(item.plate) },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = "Add one",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Button: Set this weight as main target
      Button(
        onClick = onApplyToTarget,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("apply_reverse_weight_button")
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Use ${unit.format(reverseTotalWeight)} in Calculator")
      }
    }
  }
}
