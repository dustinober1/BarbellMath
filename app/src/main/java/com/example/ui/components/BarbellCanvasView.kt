package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlateCount
import com.example.model.WeightUnit
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PlateRed
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SteelChrome

@Composable
fun BarbellCanvasSection(
  platesPerSide: List<PlateCount>,
  unit: WeightUnit,
  barName: String,
  barWeightFormatted: String,
  isFullBar: Boolean,
  onToggleFullBar: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("barbell_canvas_section"),
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    tonalElevation = 0.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      // Header with view switcher
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (isFullBar) "Full Barbell Load" else "One Sleeve (Loading Sequence)",
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

        // Toggle button
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggleFullBar)
            .testTag("toggle_bar_view_button"),
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AspectRatio,
              contentDescription = "Toggle view",
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              text = if (isFullBar) "Sleeve View" else "Full Bar",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Canvas Visualizer
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(DarkSurfaceVariant)
          .border(1.dp, Slate700.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        if (isFullBar) {
          FullBarbellCanvas(platesPerSide = platesPerSide)
        } else {
          SingleSleeveCanvas(platesPerSide = platesPerSide)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Plate sequence badges
      if (platesPerSide.isNotEmpty()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "Inside ➔",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Expand each plate multiplier to individual badge items
            val allSinglePlates = platesPerSide.flatMap { item ->
              List(item.quantity) { item.plate }
            }

            allSinglePlates.take(8).forEach { plate ->
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = plate.color,
                modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
              ) {
                Text(
                  text = plate.formattedWeight,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = plate.textColor,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  fontSize = 11.sp
                )
              }
            }

            if (allSinglePlates.size > 8) {
              Text(
                text = "+${allSinglePlates.size - 8} more",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Text(
            text = "➔ Outside",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
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
            text = "Empty bar — no plates required for this weight",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun SingleSleeveCanvas(
  platesPerSide: List<PlateCount>,
  modifier: Modifier = Modifier
) {
  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(150.dp)
      .testTag("single_sleeve_canvas")
  ) {
    val canvasW = size.width
    val canvasH = size.height
    val centerY = canvasH / 2f

    // Shaft & Sleeve geometry
    val shaftWidth = canvasW * 0.15f
    val shaftHeight = 16.dp.toPx()
    val flangeWidth = 14.dp.toPx()
    val flangeHeight = 60.dp.toPx()
    val sleeveWidth = canvasW * 0.72f
    val sleeveHeight = 32.dp.toPx()

    val startX = 16.dp.toPx()
    val flangeX = startX + shaftWidth
    val sleeveStartX = flangeX + flangeWidth

    // 1. Draw Knurled Inner Shaft
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
    var knurlX = startX + 4.dp.toPx()
    while (knurlX < flangeX - 4.dp.toPx()) {
      drawLine(
        color = Color(0x44FFFFFF),
        start = Offset(knurlX, centerY - shaftHeight / 2 + 2),
        end = Offset(knurlX, centerY + shaftHeight / 2 - 2),
        strokeWidth = 1.5f
      )
      knurlX += 4.dp.toPx()
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

    // 3. Draw Steel Rotating Sleeve (Barbell post)
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

    // Sleeve ridges / grooves
    drawLine(
      color = Color(0x33000000),
      start = Offset(sleeveStartX, centerY - sleeveHeight / 2 + 6.dp.toPx()),
      end = Offset(sleeveStartX + sleeveWidth - 4.dp.toPx(), centerY - sleeveHeight / 2 + 6.dp.toPx()),
      strokeWidth = 1.5f
    )
    drawLine(
      color = Color(0x33000000),
      start = Offset(sleeveStartX, centerY + sleeveHeight / 2 - 6.dp.toPx()),
      end = Offset(sleeveStartX + sleeveWidth - 4.dp.toPx(), centerY + sleeveHeight / 2 - 6.dp.toPx()),
      strokeWidth = 1.5f
    )

    // 4. Draw Loaded Plates sequentially from flange outwards
    val flatPlates = platesPerSide.flatMap { item -> List(item.quantity) { item.plate } }
    var currentX = sleeveStartX + 2.dp.toPx()
    val maxPlateHeight = 120.dp.toPx()
    val minPlateThickness = 14.dp.toPx()
    val maxPlateThickness = 28.dp.toPx()

    for (plate in flatPlates) {
      val plateHeight = maxPlateHeight * plate.heightRatio
      val plateThickness = minPlateThickness + (maxPlateThickness - minPlateThickness) * plate.thicknessFraction
      val plateTopY = centerY - plateHeight / 2

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

      // Plate border rim
      drawRoundRect(
        color = Color(0x44FFFFFF),
        topLeft = Offset(currentX, plateTopY),
        size = Size(plateThickness, plateHeight),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(width = 1.2.dp.toPx())
      )

      // Center Chrome Hub / Hole
      val hubH = 34.dp.toPx()
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF64748B)),
          startY = centerY - hubH / 2,
          endY = centerY + hubH / 2
        ),
        topLeft = Offset(currentX + 1.dp.toPx(), centerY - hubH / 2),
        size = Size(plateThickness - 2.dp.toPx(), hubH)
      )

      // Weight label text on plate
      val paint = android.graphics.Paint().apply {
        color = plate.textColor.toArgb()
        textSize = if (plateHeight > 80.dp.toPx()) 30f else 22f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
      }

      val textX = currentX + plateThickness / 2f
      val textY = plateTopY + 22.dp.toPx()
      drawContext.canvas.nativeCanvas.drawText(plate.formattedWeight, textX, textY, paint)

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
    }

    // End Cap of barbell sleeve
    drawRoundRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFCBD5E1), Color(0xFF475569)),
        startY = centerY - sleeveHeight / 2,
        endY = centerY + sleeveHeight / 2
      ),
      topLeft = Offset(sleeveStartX + sleeveWidth - 4.dp.toPx(), centerY - sleeveHeight / 2),
      size = Size(6.dp.toPx(), sleeveHeight),
      cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
  }
}

@Composable
fun FullBarbellCanvas(
  platesPerSide: List<PlateCount>,
  modifier: Modifier = Modifier
) {
  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(150.dp)
      .testTag("full_barbell_canvas")
  ) {
    val canvasW = size.width
    val canvasH = size.height
    val centerY = canvasH / 2f

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
    val rightSleeveEnd = rightSleeveStart + sleeveWidth

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

    // Center knurling sections
    val knurlSectionW = centerShaftWidth * 0.25f
    // Left knurl
    drawRect(
      color = Color(0x33FFFFFF),
      topLeft = Offset(leftSleeveEnd + centerShaftWidth * 0.15f, centerY - shaftHeight / 2),
      size = Size(knurlSectionW, shaftHeight)
    )
    // Right knurl
    drawRect(
      color = Color(0x33FFFFFF),
      topLeft = Offset(rightSleeveStart - centerShaftWidth * 0.15f - knurlSectionW, centerY - shaftHeight / 2),
      size = Size(knurlSectionW, shaftHeight)
    )

    // 2. Draw Left and Right Sleeves
    // Left sleeve
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
    // Right sleeve
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

    // Left flange
    drawRoundRect(
      color = Color(0xFFCBD5E1),
      topLeft = Offset(leftSleeveEnd - flangeWidth, centerY - flangeHeight / 2),
      size = Size(flangeWidth, flangeHeight),
      cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    // Right flange
    drawRoundRect(
      color = Color(0xFFCBD5E1),
      topLeft = Offset(rightSleeveStart, centerY - flangeHeight / 2),
      size = Size(flangeWidth, flangeHeight),
      cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // 3. Draw Symmetrical Plates
    val flatPlates = platesPerSide.flatMap { item -> List(item.quantity) { item.plate } }
    val maxPlateHeight = 90.dp.toPx()
    val minThick = 6.dp.toPx()
    val maxThick = 12.dp.toPx()

    // Draw Left Sleeve Plates (inside collar moving leftwards)
    var leftPlateX = leftSleeveEnd - flangeWidth - 2.dp.toPx()
    for (plate in flatPlates) {
      val pH = maxPlateHeight * plate.heightRatio
      val pW = minThick + (maxThick - minThick) * plate.thicknessFraction
      leftPlateX -= pW

      drawRoundRect(
        color = plate.color,
        topLeft = Offset(leftPlateX, centerY - pH / 2),
        size = Size(pW, pH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
      )
      drawRoundRect(
        color = Color(0x44FFFFFF),
        topLeft = Offset(leftPlateX, centerY - pH / 2),
        size = Size(pW, pH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
      )
      leftPlateX -= 1.dp.toPx()
    }

    // Draw Right Sleeve Plates (inside collar moving rightwards)
    var rightPlateX = rightSleeveStart + flangeWidth + 2.dp.toPx()
    for (plate in flatPlates) {
      val pH = maxPlateHeight * plate.heightRatio
      val pW = minThick + (maxThick - minThick) * plate.thicknessFraction

      drawRoundRect(
        color = plate.color,
        topLeft = Offset(rightPlateX, centerY - pH / 2),
        size = Size(pW, pH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
      )
      drawRoundRect(
        color = Color(0x44FFFFFF),
        topLeft = Offset(rightPlateX, centerY - pH / 2),
        size = Size(pW, pH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
      )
      rightPlateX += pW + 1.dp.toPx()
    }
  }
}
