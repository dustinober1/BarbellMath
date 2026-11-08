package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LabelOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Plate
import com.example.model.PlateCount
import com.example.model.WeightUnit
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PlateRed
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun BarbellCanvasSection(
  platesPerSide: List<PlateCount>,
  unit: WeightUnit,
  barName: String,
  barWeightFormatted: String,
  isFullBar: Boolean,
  onToggleFullBar: () -> Unit,
  showPlateLabels: Boolean = true,
  onTogglePlateLabels: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var selectedPlateIndex by remember { mutableStateOf<Int?>(null) }

  // Flatten plates into individual items from inside flange to outside
  val flatPlates = remember(platesPerSide) {
    platesPerSide.flatMap { item -> List(item.quantity) { item.plate } }
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("barbell_canvas_section"),
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    ),
    tonalElevation = 0.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      // Header with view switcher & weight labels toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
          Text(
            text = if (isFullBar) "Full Barbell Load" else "Sleeve Position Visualizer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "$barName • $barWeightFormatted",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Plate Weight Labels Toggle button
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onTogglePlateLabels)
              .testTag("toggle_plate_labels_button"),
            color = if (showPlateLabels) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = if (showPlateLabels) {
              androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            } else null
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = if (showPlateLabels) Icons.Default.Label else Icons.Default.LabelOff,
                contentDescription = "Toggle Plate Weight Labels",
                modifier = Modifier.size(15.dp),
                tint = if (showPlateLabels) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = if (showPlateLabels) "Labels ON" else "Labels OFF",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (showPlateLabels) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
          }

          // Full Bar / Sleeve View Toggle button
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onToggleFullBar)
              .testTag("toggle_bar_view_button"),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.AspectRatio,
                contentDescription = "Toggle view",
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
              )
              Text(
                text = if (isFullBar) "Sleeve" else "Full Bar",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 11.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Canvas Visualizer with Position Markers
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(195.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(DarkSurfaceVariant)
          .border(1.dp, Slate700.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        if (isFullBar) {
          FullBarbellCanvas(
            platesPerSide = platesPerSide,
            selectedPlateIndex = selectedPlateIndex,
            showWeightLabels = showPlateLabels,
            unit = unit
          )
        } else {
          SingleSleeveCanvas(
            platesPerSide = platesPerSide,
            selectedPlateIndex = selectedPlateIndex,
            showWeightLabels = showPlateLabels,
            unit = unit,
            onPlateClicked = { index ->
              selectedPlateIndex = if (selectedPlateIndex == index) null else index
            }
          )
        }

        // Quick interactive indicator overlay in the visual barbell canvas
        Surface(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onTogglePlateLabels)
            .testTag("canvas_overlay_label_toggle"),
          color = Color(0xD90F172A),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (showPlateLabels) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color(0x33FFFFFF)
          )
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (showPlateLabels) Color(0xFF22C55E) else Color(0xFF94A3B8))
            )
            Text(
              text = if (showPlateLabels) "Labels ON" else "Labels OFF",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = if (showPlateLabels) Color(0xFFE2E8F0) else Color(0xFF94A3B8),
              fontSize = 10.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Sleeve Loading Guide & Position Sequence Cards
      if (flatPlates.isNotEmpty()) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Sleeve Loading Sequence",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = "Inside Flange",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp
              )
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = "Outside Collar",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
              )
            }
          }

          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            itemsIndexed(flatPlates) { index, plate ->
              val positionNumber = index + 1
              val isSelected = selectedPlateIndex == index
              val positionName = when (index) {
                0 -> "Position #1 (Innermost)"
                flatPlates.size - 1 -> "Position #$positionNumber (Outermost)"
                else -> "Position #$positionNumber"
              }
              val cumulativeWeight = flatPlates.take(index + 1).sumOf { it.weight }

              SleevePositionCard(
                positionNumber = positionNumber,
                positionName = positionName,
                plate = plate,
                cumulativeWeight = cumulativeWeight,
                unit = unit,
                isSelected = isSelected,
                onClick = {
                  selectedPlateIndex = if (isSelected) null else index
                }
              )
            }

            // Collar Clamp Card at the end of the sleeve
            item {
              CollarClampPositionCard(
                totalPlatesCount = flatPlates.size
              )
            }
          }
        }
      } else {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Empty Barbell — Sleeves bare with zero plates loaded",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun SleevePositionCard(
  positionNumber: Int,
  positionName: String,
  plate: Plate,
  cumulativeWeight: Double,
  unit: WeightUnit,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = androidx.compose.foundation.BorderStroke(
      if (isSelected) 2.dp else 1.dp,
      if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    ),
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("sleeve_position_card_$positionNumber")
  ) {
    Column(
      modifier = Modifier
        .width(135.dp)
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.primary
        ) {
          Text(
            text = "#$positionNumber",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp
          )
        }

        Text(
          text = if (positionNumber == 1) "Slide 1st" else "Slide #${positionNumber}",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 10.sp
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(plate.color)
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = plate.formattedWeight,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = plate.textColor,
            fontSize = 11.sp
          )
        }

        Column {
          Text(
            text = "${plate.formattedWeight} ${unit.shortLabel}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Total: ${unit.formatPlain(cumulativeWeight)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
          )
        }
      }

      Text(
        text = if (positionNumber == 1) "Flush to Flange" else "Locks against #$positionNumber",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        fontSize = 9.sp
      )
    }
  }
}

@Composable
private fun CollarClampPositionCard(
  totalPlatesCount: Int,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    ),
    modifier = modifier.width(130.dp)
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFFDC2626)
        ) {
          Text(
            text = "COLLAR",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp
          )
        }

        Text(
          text = "Outer Lock",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 10.sp
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF475569)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(18.dp)
          )
        }

        Column {
          Text(
            text = "Spring Clamp",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Locks load",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
          )
        }
      }

      Text(
        text = "Clamp against plate #$totalPlatesCount",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        fontSize = 9.sp
      )
    }
  }
}

@Composable
fun SingleSleeveCanvas(
  platesPerSide: List<PlateCount>,
  selectedPlateIndex: Int? = null,
  showWeightLabels: Boolean = true,
  unit: WeightUnit = WeightUnit.LBS,
  onPlateClicked: ((Int) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(175.dp)
      .testTag("single_sleeve_canvas")
  ) {
    val canvasW = size.width
    val canvasH = size.height
    val centerY = canvasH * 0.52f

    // Shaft & Sleeve geometry
    val shaftWidth = canvasW * 0.12f
    val shaftHeight = 16.dp.toPx()
    val flangeWidth = 14.dp.toPx()
    val flangeHeight = 64.dp.toPx()
    val sleeveWidth = canvasW * 0.74f
    val sleeveHeight = 32.dp.toPx()

    val startX = 14.dp.toPx()
    val flangeX = startX + shaftWidth
    val sleeveStartX = flangeX + flangeWidth

    // 1. Draw Knurled Inner Shaft (Barbell Center)
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF94A3B8), Color(0xFF64748B), Color(0xFF334155)),
        startY = centerY - shaftHeight / 2,
        endY = centerY + shaftHeight / 2
      ),
      topLeft = Offset(startX, centerY - shaftHeight / 2),
      size = Size(shaftWidth, shaftHeight)
    )

    // Shaft knurling texture stripes
    var knurlX = startX + 3.dp.toPx()
    while (knurlX < flangeX - 3.dp.toPx()) {
      drawLine(
        color = Color(0x33FFFFFF),
        start = Offset(knurlX, centerY - shaftHeight / 2 + 1),
        end = Offset(knurlX, centerY + shaftHeight / 2 - 1),
        strokeWidth = 1.2f
      )
      knurlX += 3.5.dp.toPx()
    }

    // 2. Draw Inside Collar Flange / Stopper
    drawRoundRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF475569)),
        startY = centerY - flangeHeight / 2,
        endY = centerY + flangeHeight / 2
      ),
      topLeft = Offset(flangeX, centerY - flangeHeight / 2),
      size = Size(flangeWidth, flangeHeight),
      cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )

    // Flange label above
    val flangePaint = android.graphics.Paint().apply {
      color = android.graphics.Color.argb(200, 203, 213, 225)
      textSize = 20f
      isFakeBoldText = true
      textAlign = android.graphics.Paint.Align.CENTER
      isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText(
      "FLANGE",
      flangeX + flangeWidth / 2f,
      centerY - flangeHeight / 2 - 6.dp.toPx(),
      flangePaint
    )

    // 3. Draw Steel Rotating Sleeve
    drawRoundRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFF1F5F9), Color(0xFFCBD5E1), Color(0xFF64748B), Color(0xFF475569)),
        startY = centerY - sleeveHeight / 2,
        endY = centerY + sleeveHeight / 2
      ),
      topLeft = Offset(sleeveStartX, centerY - sleeveHeight / 2),
      size = Size(sleeveWidth, sleeveHeight),
      cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // Sleeve ridges
    drawLine(
      color = Color(0x22000000),
      start = Offset(sleeveStartX, centerY - sleeveHeight / 2 + 6.dp.toPx()),
      end = Offset(sleeveStartX + sleeveWidth - 4.dp.toPx(), centerY - sleeveHeight / 2 + 6.dp.toPx()),
      strokeWidth = 1.5f
    )
    drawLine(
      color = Color(0x22000000),
      start = Offset(sleeveStartX, centerY + sleeveHeight / 2 - 6.dp.toPx()),
      end = Offset(sleeveStartX + sleeveWidth - 4.dp.toPx(), centerY + sleeveHeight / 2 - 6.dp.toPx()),
      strokeWidth = 1.5f
    )

    // 4. Draw Loaded Plates sequentially from flange outwards with Position Numbers
    val flatPlates = platesPerSide.flatMap { item -> List(item.quantity) { item.plate } }
    var currentX = sleeveStartX + 2.dp.toPx()
    val maxPlateHeight = 115.dp.toPx()
    val minPlateThickness = 14.dp.toPx()
    val maxPlateThickness = 28.dp.toPx()

    flatPlates.forEachIndexed { index, plate ->
      val plateHeight = maxPlateHeight * plate.heightRatio
      val plateThickness = minPlateThickness + (maxPlateThickness - minPlateThickness) * plate.thicknessFraction
      val plateTopY = centerY - plateHeight / 2
      val isSelected = selectedPlateIndex == index
      val positionNum = index + 1

      // Position callout badge above plate
      val badgeCenter = currentX + plateThickness / 2f
      val badgeY = plateTopY - 14.dp.toPx()

      // Vertical connector tick line from badge to plate
      drawLine(
        color = if (isSelected) Color(0xFFFFD54F) else Color(0x88FFFFFF),
        start = Offset(badgeCenter, badgeY + 6.dp.toPx()),
        end = Offset(badgeCenter, plateTopY - 1.dp.toPx()),
        strokeWidth = if (isSelected) 2.dp.toPx() else 1.dp.toPx()
      )

      // Badge pill
      val badgeBgColor = if (isSelected) Color(0xFFFFB300) else Color(0xFF1E293B)
      val badgeW = if (showWeightLabels) 30.dp.toPx() else 20.dp.toPx()
      drawRoundRect(
        color = badgeBgColor,
        topLeft = Offset(badgeCenter - badgeW / 2f, badgeY - 7.dp.toPx()),
        size = Size(badgeW, 14.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
      )

      val badgePaint = android.graphics.Paint().apply {
        color = if (isSelected) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        textSize = if (showWeightLabels) 16f else 20f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
      }
      val badgeLabel = if (showWeightLabels) "#$positionNum:${plate.formattedWeight}" else "#$positionNum"
      drawContext.canvas.nativeCanvas.drawText(
        badgeLabel,
        badgeCenter,
        badgeY + 4.dp.toPx(),
        badgePaint
      )

      // Outer plate shadow / 3D edge
      drawRoundRect(
        color = Color(0x66000000),
        topLeft = Offset(currentX + 2f, plateTopY + 2f),
        size = Size(plateThickness, plateHeight),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
      )

      // Main Plate Body with Gradient
      drawRoundRect(
        brush = Brush.verticalGradient(
          colors = listOf(
            plate.color.copy(alpha = 0.95f),
            plate.color,
            plate.color.copy(red = plate.color.red * 0.75f, green = plate.color.green * 0.75f, blue = plate.color.blue * 0.75f)
          ),
          startY = plateTopY,
          endY = plateTopY + plateHeight
        ),
        topLeft = Offset(currentX, plateTopY),
        size = Size(plateThickness, plateHeight),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
      )

      // Selection glow border or standard rim
      drawRoundRect(
        color = if (isSelected) Color(0xFFFFD54F) else Color(0x44FFFFFF),
        topLeft = Offset(currentX, plateTopY),
        size = Size(plateThickness, plateHeight),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(width = if (isSelected) 2.5.dp.toPx() else 1.2.dp.toPx())
      )

      // Center Chrome Hub / Hole
      val hubH = 32.dp.toPx()
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF64748B)),
          startY = centerY - hubH / 2,
          endY = centerY + hubH / 2
        ),
        topLeft = Offset(currentX + 1.dp.toPx(), centerY - hubH / 2),
        size = Size(plateThickness - 2.dp.toPx(), hubH)
      )

      // Weight label text inside plate (toggleable)
      if (showWeightLabels) {
        val isLargePlate = plateHeight > 75.dp.toPx()
        val textX = currentX + plateThickness / 2f
        val upperCenterY = (plateTopY + (centerY - hubH / 2)) / 2f

        // High contrast backing pill for crystal clear readability at a glance
        val pillW = (plateThickness - 2.dp.toPx()).coerceAtLeast(12.dp.toPx())
        val pillH = if (isLargePlate) 18.dp.toPx() else 14.dp.toPx()
        val pillBgColor = if (plate.textColor == Color.White) Color(0x55000000) else Color(0x40FFFFFF)

        drawRoundRect(
          color = pillBgColor,
          topLeft = Offset(textX - pillW / 2f, upperCenterY - pillH / 2f),
          size = Size(pillW, pillH),
          cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )

        val paint = android.graphics.Paint().apply {
          color = plate.textColor.toArgb()
          textSize = if (isLargePlate) 24f else if (plateThickness >= 18.dp.toPx()) 19f else 15f
          isFakeBoldText = true
          textAlign = android.graphics.Paint.Align.CENTER
          isAntiAlias = true
        }

        drawContext.canvas.nativeCanvas.drawText(
          plate.formattedWeight,
          textX,
          upperCenterY + if (isLargePlate) 5.dp.toPx() else 4.dp.toPx(),
          paint
        )

        // For tall bumper plates, also draw weight/unit at lower section like authentic gym plates
        if (isLargePlate) {
          val lowerCenterY = ((centerY + hubH / 2) + (plateTopY + plateHeight)) / 2f
          drawRoundRect(
            color = pillBgColor,
            topLeft = Offset(textX - pillW / 2f, lowerCenterY - pillH / 2f),
            size = Size(pillW, pillH),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
          )
          val unitPaint = android.graphics.Paint().apply {
            color = plate.textColor.toArgb()
            textSize = 15f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
          }
          drawContext.canvas.nativeCanvas.drawText(
            unit.shortLabel.uppercase(),
            textX,
            lowerCenterY + 4.dp.toPx(),
            unitPaint
          )
        }
      }

      currentX += plateThickness + 2.dp.toPx()
    }

    // 5. Draw Spring Collar Clip if plates exist
    if (flatPlates.isNotEmpty()) {
      val collarWidth = 10.dp.toPx()
      val collarHeight = 44.dp.toPx()
      drawRoundRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFFE2E8F0), Color(0xFF64748B), Color(0xFF334155)),
          startY = centerY - collarHeight / 2,
          endY = centerY + collarHeight / 2
        ),
        topLeft = Offset(currentX, centerY - collarHeight / 2),
        size = Size(collarWidth, collarHeight),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
      )
      // Collar grip handle
      drawRoundRect(
        color = Color(0xFFDC2626),
        topLeft = Offset(currentX + 2.dp.toPx(), centerY - collarHeight / 2 - 8.dp.toPx()),
        size = Size(6.dp.toPx(), 10.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
      )

      // Label above collar
      val collarPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(220, 239, 68, 68)
        textSize = 18f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
      }
      drawContext.canvas.nativeCanvas.drawText(
        "LOCK",
        currentX + collarWidth / 2f,
        centerY - collarHeight / 2 - 12.dp.toPx(),
        collarPaint
      )
    }

    // End Cap of barbell sleeve
    val endCapX = sleeveStartX + sleeveWidth - 4.dp.toPx()
    drawRoundRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFCBD5E1), Color(0xFF475569)),
        startY = centerY - sleeveHeight / 2,
        endY = centerY + sleeveHeight / 2
      ),
      topLeft = Offset(endCapX, centerY - sleeveHeight / 2),
      size = Size(6.dp.toPx(), sleeveHeight),
      cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // Sleeve Tip label
    val tipPaint = android.graphics.Paint().apply {
      color = android.graphics.Color.argb(160, 148, 163, 184)
      textSize = 18f
      isFakeBoldText = true
      textAlign = android.graphics.Paint.Align.RIGHT
      isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText(
      "SLEEVE TIP",
      sleeveStartX + sleeveWidth,
      centerY + sleeveHeight / 2 + 16.dp.toPx(),
      tipPaint
    )

    // Loading direction bottom guide ruler
    val guideY = centerY + 62.dp.toPx()
    drawLine(
      color = Color(0x33FFFFFF),
      start = Offset(flangeX, guideY),
      end = Offset(sleeveStartX + sleeveWidth, guideY),
      strokeWidth = 1.dp.toPx()
    )

    val guidePaint = android.graphics.Paint().apply {
      color = android.graphics.Color.argb(180, 148, 163, 184)
      textSize = 19f
      isFakeBoldText = true
      textAlign = android.graphics.Paint.Align.LEFT
      isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText(
      "◄ INMOST (#1)  ───────────────────────►  OUTMOST",
      flangeX,
      guideY + 14.dp.toPx(),
      guidePaint
    )
  }
}

@Composable
fun FullBarbellCanvas(
  platesPerSide: List<PlateCount>,
  selectedPlateIndex: Int? = null,
  showWeightLabels: Boolean = true,
  unit: WeightUnit = WeightUnit.LBS,
  modifier: Modifier = Modifier
) {
  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(175.dp)
      .testTag("full_barbell_canvas")
  ) {
    val canvasW = size.width
    val canvasH = size.height
    val centerY = canvasH * 0.52f

    val totalBarWidth = canvasW * 0.94f
    val barStartX = (canvasW - totalBarWidth) / 2f
    val sleeveWidth = totalBarWidth * 0.28f
    val centerShaftWidth = totalBarWidth - (sleeveWidth * 2f)
    val shaftHeight = 10.dp.toPx()
    val flangeWidth = 8.dp.toPx()
    val flangeHeight = 46.dp.toPx()
    val sleeveHeight = 18.dp.toPx()

    val leftSleeveEnd = barStartX + sleeveWidth
    val rightSleeveStart = leftSleeveEnd + centerShaftWidth

    // 1. Draw Center Shaft
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF94A3B8), Color(0xFF64748B), Color(0xFF334155)),
        startY = centerY - shaftHeight / 2,
        endY = centerY + shaftHeight / 2
      ),
      topLeft = Offset(leftSleeveEnd, centerY - shaftHeight / 2),
      size = Size(centerShaftWidth, shaftHeight)
    )

    // Center bar mark & knurling sections
    val knurlSectionW = centerShaftWidth * 0.22f
    drawRect(
      color = Color(0x33FFFFFF),
      topLeft = Offset(leftSleeveEnd + centerShaftWidth * 0.15f, centerY - shaftHeight / 2),
      size = Size(knurlSectionW, shaftHeight)
    )
    drawRect(
      color = Color(0x33FFFFFF),
      topLeft = Offset(rightSleeveStart - centerShaftWidth * 0.15f - knurlSectionW, centerY - shaftHeight / 2),
      size = Size(knurlSectionW, shaftHeight)
    )

    // Center Shaft Label
    val centerPaint = android.graphics.Paint().apply {
      color = android.graphics.Color.argb(160, 203, 213, 225)
      textSize = 18f
      isFakeBoldText = true
      textAlign = android.graphics.Paint.Align.CENTER
      isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText(
      "BARBELL SHAFT",
      leftSleeveEnd + centerShaftWidth / 2f,
      centerY + shaftHeight / 2 + 18.dp.toPx(),
      centerPaint
    )

    // 2. Draw Left and Right Sleeves
    drawRoundRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFE2E8F0), Color(0xFF64748B)),
        startY = centerY - sleeveHeight / 2,
        endY = centerY + sleeveHeight / 2
      ),
      topLeft = Offset(barStartX, centerY - sleeveHeight / 2),
      size = Size(sleeveWidth, sleeveHeight),
      cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawRoundRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFE2E8F0), Color(0xFF64748B)),
        startY = centerY - sleeveHeight / 2,
        endY = centerY + sleeveHeight / 2
      ),
      topLeft = Offset(rightSleeveStart, centerY - sleeveHeight / 2),
      size = Size(sleeveWidth, sleeveHeight),
      cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // Flanges
    drawRoundRect(
      color = Color(0xFFCBD5E1),
      topLeft = Offset(leftSleeveEnd - flangeWidth, centerY - flangeHeight / 2),
      size = Size(flangeWidth, flangeHeight),
      cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawRoundRect(
      color = Color(0xFFCBD5E1),
      topLeft = Offset(rightSleeveStart, centerY - flangeHeight / 2),
      size = Size(flangeWidth, flangeHeight),
      cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // 3. Draw Symmetrical Plates with Position / Weight Labels
    val flatPlates = platesPerSide.flatMap { item -> List(item.quantity) { item.plate } }
    val maxPlateHeight = 85.dp.toPx()
    val minThick = 6.dp.toPx()
    val maxThick = 12.dp.toPx()

    // Left Sleeve Plates (Inside Collar moving leftwards)
    var leftPlateX = leftSleeveEnd - flangeWidth - 2.dp.toPx()
    flatPlates.forEachIndexed { index, plate ->
      val pH = maxPlateHeight * plate.heightRatio
      val pW = minThick + (maxThick - minThick) * plate.thicknessFraction
      leftPlateX -= pW
      val isSelected = selectedPlateIndex == index

      drawRoundRect(
        color = plate.color,
        topLeft = Offset(leftPlateX, centerY - pH / 2),
        size = Size(pW, pH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
      )
      drawRoundRect(
        color = if (isSelected) Color(0xFFFFD54F) else Color(0x44FFFFFF),
        topLeft = Offset(leftPlateX, centerY - pH / 2),
        size = Size(pW, pH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        style = Stroke(width = if (isSelected) 2.dp.toPx() else 1.dp.toPx())
      )

      // Callout badge above plate (weight value or position)
      val numCenter = leftPlateX + pW / 2f
      val topY = centerY - pH / 2 - 4.dp.toPx()

      if (showWeightLabels) {
        val badgeW = (pW + 6.dp.toPx()).coerceAtLeast(18.dp.toPx())
        drawRoundRect(
          color = if (isSelected) Color(0xFFFFB300) else Color(0xDD1E293B),
          topLeft = Offset(numCenter - badgeW / 2f, topY - 12.dp.toPx()),
          size = Size(badgeW, 13.dp.toPx()),
          cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
        val numPaint = android.graphics.Paint().apply {
          color = if (isSelected) android.graphics.Color.BLACK else android.graphics.Color.WHITE
          textSize = 14f
          isFakeBoldText = true
          textAlign = android.graphics.Paint.Align.CENTER
          isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(
          plate.formattedWeight,
          numCenter,
          topY - 2.dp.toPx(),
          numPaint
        )

        // Inside plate weight text for larger plates in full bar view
        if (pH >= 46.dp.toPx()) {
          val insidePaint = android.graphics.Paint().apply {
            color = plate.textColor.toArgb()
            textSize = 12f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
          }
          val insideCenterY = centerY - pH / 4f
          drawContext.canvas.nativeCanvas.save()
          drawContext.canvas.nativeCanvas.rotate(-90f, numCenter, insideCenterY)
          drawContext.canvas.nativeCanvas.drawText(
            plate.formattedWeight,
            numCenter,
            insideCenterY + 4f,
            insidePaint
          )
          drawContext.canvas.nativeCanvas.restore()
        }
      } else {
        val numPaint = android.graphics.Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = 16f
          isFakeBoldText = true
          textAlign = android.graphics.Paint.Align.CENTER
          isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(
          "${index + 1}",
          numCenter,
          topY,
          numPaint
        )
      }

      leftPlateX -= 1.dp.toPx()
    }

    // Right Sleeve Plates (Inside Collar moving rightwards)
    var rightPlateX = rightSleeveStart + flangeWidth + 2.dp.toPx()
    flatPlates.forEachIndexed { index, plate ->
      val pH = maxPlateHeight * plate.heightRatio
      val pW = minThick + (maxThick - minThick) * plate.thicknessFraction
      val isSelected = selectedPlateIndex == index

      drawRoundRect(
        color = plate.color,
        topLeft = Offset(rightPlateX, centerY - pH / 2),
        size = Size(pW, pH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
      )
      drawRoundRect(
        color = if (isSelected) Color(0xFFFFD54F) else Color(0x44FFFFFF),
        topLeft = Offset(rightPlateX, centerY - pH / 2),
        size = Size(pW, pH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        style = Stroke(width = if (isSelected) 2.dp.toPx() else 1.dp.toPx())
      )

      // Callout badge above plate (weight value or position)
      val numCenter = rightPlateX + pW / 2f
      val topY = centerY - pH / 2 - 4.dp.toPx()

      if (showWeightLabels) {
        val badgeW = (pW + 6.dp.toPx()).coerceAtLeast(18.dp.toPx())
        drawRoundRect(
          color = if (isSelected) Color(0xFFFFB300) else Color(0xDD1E293B),
          topLeft = Offset(numCenter - badgeW / 2f, topY - 12.dp.toPx()),
          size = Size(badgeW, 13.dp.toPx()),
          cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
        val numPaint = android.graphics.Paint().apply {
          color = if (isSelected) android.graphics.Color.BLACK else android.graphics.Color.WHITE
          textSize = 14f
          isFakeBoldText = true
          textAlign = android.graphics.Paint.Align.CENTER
          isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(
          plate.formattedWeight,
          numCenter,
          topY - 2.dp.toPx(),
          numPaint
        )

        // Inside plate weight text for larger plates in full bar view
        if (pH >= 46.dp.toPx()) {
          val insidePaint = android.graphics.Paint().apply {
            color = plate.textColor.toArgb()
            textSize = 12f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
          }
          val insideCenterY = centerY - pH / 4f
          drawContext.canvas.nativeCanvas.save()
          drawContext.canvas.nativeCanvas.rotate(-90f, numCenter, insideCenterY)
          drawContext.canvas.nativeCanvas.drawText(
            plate.formattedWeight,
            numCenter,
            insideCenterY + 4f,
            insidePaint
          )
          drawContext.canvas.nativeCanvas.restore()
        }
      } else {
        val numPaint = android.graphics.Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = 16f
          isFakeBoldText = true
          textAlign = android.graphics.Paint.Align.CENTER
          isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(
          "${index + 1}",
          numCenter,
          topY,
          numPaint
        )
      }

      rightPlateX += pW + 1.dp.toPx()
    }

    // Sleeve labels at bottom
    val sidePaint = android.graphics.Paint().apply {
      color = android.graphics.Color.argb(160, 148, 163, 184)
      textSize = 18f
      isFakeBoldText = true
      textAlign = android.graphics.Paint.Align.CENTER
      isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText(
      "LEFT SLEEVE",
      barStartX + sleeveWidth / 2f,
      centerY + sleeveHeight / 2 + 18.dp.toPx(),
      sidePaint
    )
    drawContext.canvas.nativeCanvas.drawText(
      "RIGHT SLEEVE",
      rightSleeveStart + sleeveWidth / 2f,
      centerY + sleeveHeight / 2 + 18.dp.toPx(),
      sidePaint
    )
  }
}
