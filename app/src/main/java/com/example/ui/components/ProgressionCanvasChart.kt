package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ChartPoint
import com.example.model.Plate
import com.example.model.WeightUnit
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberDark
import com.example.ui.theme.PlateGreen
import kotlin.math.abs

@Composable
fun WeightProgressionCanvasChart(
  points: List<ChartPoint>,
  minWeight: Double,
  maxWeight: Double,
  unit: WeightUnit,
  selectedIndex: Int?,
  onSelectIndex: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  if (points.isEmpty()) return

  val primaryColor = MaterialTheme.colorScheme.primary
  val surfaceColor = MaterialTheme.colorScheme.surface
  val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
  val onSurfaceColor = MaterialTheme.colorScheme.onSurface
  val outlineColor = MaterialTheme.colorScheme.outlineVariant
  val gridColor = outlineColor.copy(alpha = 0.52f)
  val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
  val prColor = AccentAmber
  val prStrokeColor = AccentAmberDark

  val textMeasurer = rememberTextMeasurer()
  val axisTextStyle = TextStyle(
    fontSize = 10.sp,
    fontWeight = FontWeight.Medium,
    color = labelColor
  )
  val prBadgeTextStyle = TextStyle(
    fontSize = 9.sp,
    fontWeight = FontWeight.Bold,
    color = prStrokeColor
  )
  val tooltipWeightStyle = TextStyle(
    fontSize = 11.sp,
    fontWeight = FontWeight.ExtraBold,
    color = onSurfaceColor
  )
  val tooltipMetaStyle = TextStyle(
    fontSize = 9.sp,
    fontWeight = FontWeight.Medium,
    color = labelColor
  )

  val sweepProgress by animateFloatAsState(
    targetValue = 1f,
    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
    label = "chart_sweep"
  )

  val rawMinWeight = points.minOfOrNull { it.weight } ?: minWeight
  val rawMaxWeight = points.maxOfOrNull { it.weight } ?: maxWeight
  val linearScale = remember(rawMinWeight, rawMaxWeight, minWeight, maxWeight) {
    LinearChartScale.nice(
      rawMin = minOf(rawMinWeight, minWeight),
      rawMax = maxOf(rawMaxWeight, maxWeight),
      desiredTickCount = 5
    )
  }
  val prMilestones = remember(points) { resolvePrMilestoneIndices(points) }

  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(256.dp)
      .semantics {
        contentDescription = "Time-series weight progression line chart with ${points.size} logged sessions and ${prMilestones.size} personal records in ${unit.shortLabel}"
      }
      .testTag("weight_progression_canvas")
      .pointerInput(points) {
        val leftPad = 50.dp.toPx()
        val rightPad = 18.dp.toPx()
        val plotW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
        val timeScale = TimeChartScale(points, leftPad, plotW)

        detectTapGestures { offset ->
          onSelectIndex(timeScale.nearestIndex(offset.x))
        }
      }
      .pointerInput(points) {
        val leftPad = 50.dp.toPx()
        val rightPad = 18.dp.toPx()
        val plotW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
        val timeScale = TimeChartScale(points, leftPad, plotW)

        detectHorizontalDragGestures { change, _ ->
          change.consume()
          onSelectIndex(timeScale.nearestIndex(change.position.x))
        }
      }
  ) {
    val leftPad = 50.dp.toPx()
    val rightPad = 18.dp.toPx()
    val topPad = 42.dp.toPx() // Room for D3-style floating tooltip & peak badge
    val bottomPad = 30.dp.toPx()

    val plotWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
    val plotHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)
    val plotBottom = topPad + plotHeight

    val timeScale = TimeChartScale(points, leftPad, plotWidth)

    // 1. D3 axisLeft: Horizontal dashed gridlines & clean nice Y-axis labels
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 7f), 0f)
    linearScale.ticks.forEachIndexed { idx, tickWeight ->
      val y = linearScale.yFor(tickWeight, plotBottom, plotHeight)
      val isBaseline = idx == 0

      drawLine(
        color = if (isBaseline) outlineColor.copy(alpha = 0.85f) else gridColor,
        start = Offset(leftPad, y),
        end = Offset(leftPad + plotWidth, y),
        strokeWidth = if (isBaseline) 1.2.dp.toPx() else 0.9.dp.toPx(),
        pathEffect = if (isBaseline) null else dashEffect
      )

      // Small axis tick mark
      drawLine(
        color = outlineColor.copy(alpha = 0.8f),
        start = Offset(leftPad - 4.dp.toPx(), y),
        end = Offset(leftPad, y),
        strokeWidth = 1.dp.toPx()
      )

      val label = Plate.formatWeight(tickWeight)
      val measured = textMeasurer.measure(text = label, style = axisTextStyle)
      drawText(
        textLayoutResult = measured,
        topLeft = Offset(
          x = (leftPad - measured.size.width - 7.dp.toPx()).coerceAtLeast(0f),
          y = (y - measured.size.height / 2f).coerceIn(0f, size.height - measured.size.height)
        )
      )
    }

    // Subtle left Y-axis spine
    drawLine(
      color = outlineColor.copy(alpha = 0.7f),
      start = Offset(leftPad, topPad),
      end = Offset(leftPad, plotBottom),
      strokeWidth = 1.dp.toPx()
    )

    val coordinates = points.mapIndexed { idx, pt ->
      Offset(
        x = timeScale.xFor(idx),
        y = linearScale.yFor(pt.weight, plotBottom, plotHeight)
      )
    }

    // 2. PR Ceiling reference line across plot
    val maxPointWeight = points.maxOf { it.weight }
    if (points.size >= 2) {
      val prY = linearScale.yFor(maxPointWeight, plotBottom, plotHeight)
      drawLine(
        color = prColor.copy(alpha = 0.55f),
        start = Offset(leftPad, prY),
        end = Offset(leftPad + plotWidth, prY),
        strokeWidth = 1.2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
      )
      val prCeilingLabel = textMeasurer.measure(
        "PR ${Plate.formatWeight(maxPointWeight)} ${unit.shortLabel}",
        prBadgeTextStyle
      )
      drawText(
        textLayoutResult = prCeilingLabel,
        topLeft = Offset(
          x = (leftPad + plotWidth - prCeilingLabel.size.width).coerceAtLeast(leftPad),
          y = (prY - prCeilingLabel.size.height - 3.dp.toPx()).coerceAtLeast(0f)
        )
      )
    }

    // 3. Smooth D3-style Area Fill + Monotone-like Cubic Line Path with reveal clip
    if (coordinates.size >= 2) {
      val linePath = Path().apply {
        moveTo(coordinates.first().x, coordinates.first().y)
        for (i in 1 until coordinates.size) {
          val prev = coordinates[i - 1]
          val curr = coordinates[i]
          val controlX = (prev.x + curr.x) / 2f
          cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
        }
      }

      val areaPath = Path().apply {
        addPath(linePath)
        lineTo(coordinates.last().x, plotBottom)
        lineTo(coordinates.first().x, plotBottom)
        close()
      }

      val revealRight = leftPad + plotWidth * sweepProgress + 10.dp.toPx()
      clipRect(left = leftPad - 4.dp.toPx(), top = 0f, right = revealRight, bottom = plotBottom + 4.dp.toPx()) {
        drawPath(
          path = areaPath,
          brush = Brush.verticalGradient(
            colors = listOf(
              primaryColor.copy(alpha = 0.26f),
              primaryColor.copy(alpha = 0.05f),
              Color.Transparent
            ),
            startY = topPad,
            endY = plotBottom
          )
        )

        drawPath(
          path = linePath,
          color = primaryColor,
          style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
          )
        )
      }
    } else if (coordinates.size == 1) {
      val onlyPt = coordinates.first()
      drawLine(
        color = primaryColor.copy(alpha = 0.45f),
        start = Offset(leftPad, onlyPt.y),
        end = Offset(leftPad + plotWidth, onlyPt.y),
        strokeWidth = 2.dp.toPx(),
        pathEffect = dashEffect
      )
    }

    // 4. D3 Crosshair (vertical + horizontal dashed lines) for active point
    val activeIdx = (selectedIndex ?: points.lastIndex).coerceIn(0, points.lastIndex)
    val selectedCoord = coordinates[activeIdx]
    val activeIsPr = activeIdx in prMilestones
    val crosshairColor = (if (activeIsPr) prStrokeColor else primaryColor).copy(alpha = 0.4f)

    drawLine(
      color = crosshairColor,
      start = Offset(selectedCoord.x, topPad),
      end = Offset(selectedCoord.x, plotBottom),
      strokeWidth = 1.4.dp.toPx(),
      pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
    )
    drawLine(
      color = crosshairColor.copy(alpha = 0.25f),
      start = Offset(leftPad, selectedCoord.y),
      end = Offset(leftPad + plotWidth, selectedCoord.y),
      strokeWidth = 1.1.dp.toPx(),
      pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
    )

    // 5. Distinct Session Circles vs Golden Star/Diamond PR Markers
    coordinates.forEachIndexed { index, center ->
      val isSelected = index == activeIdx
      val isPr = index in prMilestones

      if (isSelected) {
        drawCircle(
          color = (if (isPr) prColor else primaryColor).copy(alpha = 0.22f),
          radius = 13.dp.toPx(),
          center = center
        )
      }

      if (isPr) {
        // Vertical PR drop stem accent when multiple points exist
        if (points.size >= 2) {
          drawLine(
            color = prColor.copy(alpha = 0.26f),
            start = center,
            end = Offset(center.x, plotBottom),
            strokeWidth = 1.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 4f), 0f)
          )
        }
        drawPrStarMarker(
          center = center,
          outerRadius = if (isSelected) 9.5.dp.toPx() else 8.dp.toPx(),
          fillColor = prColor,
          strokeColor = prStrokeColor,
          coreColor = surfaceColor
        )
      } else {
        val nodeRadius = if (isSelected) 6.dp.toPx() else if (points.size > 18) 3.dp.toPx() else 4.2.dp.toPx()
        drawCircle(
          color = primaryColor,
          radius = nodeRadius,
          center = center
        )
        drawCircle(
          color = surfaceColor,
          radius = nodeRadius * 0.48f,
          center = center
        )
      }
    }

    // 6. D3-style Floating Tooltip Callout for Active Point
    val activePoint = points[activeIdx]
    val weightTitle = buildString {
      if (activeIsPr) append("★ PR ")
      append(activePoint.session.formattedAchievedWeight())
      if (activePoint.session.reps.isNotBlank()) {
        append(" • ${activePoint.session.reps}")
      }
    }
    val metaLine = "${activePoint.session.liftName} • ${activePoint.dateLabel}"

    val titleMeasured = textMeasurer.measure(
      weightTitle,
      tooltipWeightStyle.copy(color = if (activeIsPr) prStrokeColor else onSurfaceColor)
    )
    val metaMeasured = textMeasurer.measure(metaLine, tooltipMetaStyle)

    val boxPadH = 8.dp.toPx()
    val boxPadV = 4.dp.toPx()
    val boxW = maxOf(titleMeasured.size.width, metaMeasured.size.width) + boxPadH * 2
    val boxH = titleMeasured.size.height + metaMeasured.size.height + boxPadV * 2 + 1.dp.toPx()

    val boxLeft = (selectedCoord.x - boxW / 2f)
      .coerceIn(leftPad, (leftPad + plotWidth - boxW).coerceAtLeast(leftPad))
    val desiredTop = selectedCoord.y - boxH - 14.dp.toPx()
    val boxTop = if (desiredTop >= 2.dp.toPx()) {
      desiredTop
    } else {
      (selectedCoord.y + 14.dp.toPx()).coerceAtMost(plotBottom - boxH - 4.dp.toPx())
    }

    drawRoundRect(
      color = surfaceColor.copy(alpha = 0.96f),
      topLeft = Offset(boxLeft, boxTop),
      size = Size(boxW, boxH),
      cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )
    drawRoundRect(
      color = if (activeIsPr) prColor.copy(alpha = 0.85f) else surfaceVariantColor.copy(alpha = 0.95f),
      topLeft = Offset(boxLeft, boxTop),
      size = Size(boxW, boxH),
      cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
      style = Stroke(width = if (activeIsPr) 1.4.dp.toPx() else 1.dp.toPx())
    )
    drawText(
      textLayoutResult = titleMeasured,
      topLeft = Offset(boxLeft + boxPadH, boxTop + boxPadV)
    )
    drawText(
      textLayoutResult = metaMeasured,
      topLeft = Offset(boxLeft + boxPadH, boxTop + boxPadV + titleMeasured.size.height + 1.dp.toPx())
    )

    // 7. D3 axisBottom: X-axis tick marks & time-scaled date labels (start, middle, end without overlap)
    val labelIndices = when {
      points.size == 1 -> listOf(0)
      points.size == 2 -> listOf(0, 1)
      else -> listOf(0, points.size / 2, points.lastIndex).distinct()
    }
    val labelY = plotBottom + 8.dp.toPx()
    var lastLabelRight = -1f

    labelIndices.forEach { idx ->
      val xCenter = coordinates[idx].x
      drawLine(
        color = outlineColor.copy(alpha = 0.85f),
        start = Offset(xCenter, plotBottom),
        end = Offset(xCenter, plotBottom + 4.dp.toPx()),
        strokeWidth = 1.dp.toPx()
      )

      val text = points[idx].dateLabel
      val measured = textMeasurer.measure(text, axisTextStyle)
      val desiredX = when (idx) {
        0 -> xCenter.coerceAtLeast(leftPad)
        points.lastIndex -> (xCenter - measured.size.width).coerceAtMost(leftPad + plotWidth - measured.size.width)
        else -> (xCenter - measured.size.width / 2f).coerceIn(leftPad, leftPad + plotWidth - measured.size.width)
      }
      if (desiredX > lastLabelRight + 8.dp.toPx()) {
        drawText(
          textLayoutResult = measured,
          topLeft = Offset(desiredX, labelY)
        )
        lastLabelRight = desiredX + measured.size.width
      }
    }
  }
}

private fun DrawScope.drawPrStarMarker(
  center: Offset,
  outerRadius: Float,
  fillColor: Color,
  strokeColor: Color,
  coreColor: Color
) {
  // 4-point starburst / rotated diamond marker for unmistakable PR distinction
  val innerRadius = outerRadius * 0.44f
  val starPath = Path().apply {
    moveTo(center.x, center.y - outerRadius) // Top
    lineTo(center.x + innerRadius, center.y - innerRadius)
    lineTo(center.x + outerRadius, center.y) // Right
    lineTo(center.x + innerRadius, center.y + innerRadius)
    lineTo(center.x, center.y + outerRadius) // Bottom
    lineTo(center.x - innerRadius, center.y + innerRadius)
    lineTo(center.x - outerRadius, center.y) // Left
    lineTo(center.x - innerRadius, center.y - innerRadius)
    close()
  }

  drawPath(path = starPath, color = fillColor)
  drawPath(
    path = starPath,
    color = strokeColor,
    style = Stroke(width = 1.6.dp.toPx(), join = StrokeJoin.Round)
  )
  drawCircle(
    color = coreColor,
    radius = outerRadius * 0.24f,
    center = center
  )
}

@Composable
fun MiniProgressionSparkline(
  weights: List<Double>,
  isPositiveOrFlat: Boolean,
  modifier: Modifier = Modifier
) {
  if (weights.size < 2) return

  val strokeColor = if (isPositiveOrFlat) PlateGreen else MaterialTheme.colorScheme.primary
  val surfaceColor = MaterialTheme.colorScheme.surface

  Canvas(
    modifier = modifier
      .semantics {
        contentDescription = "Recent sessions weight trend sparkline"
      }
      .testTag("recent_sessions_sparkline")
  ) {
    val padX = 4.dp.toPx()
    val padY = 5.dp.toPx()
    val w = (size.width - padX * 2).coerceAtLeast(1f)
    val h = (size.height - padY * 2).coerceAtLeast(1f)

    val minW = weights.min()
    val maxW = weights.max()
    val span = (maxW - minW).takeIf { it > 0.01 } ?: 1.0

    val coords = weights.mapIndexed { idx, value ->
      val x = padX + (idx.toFloat() / (weights.size - 1).toFloat()) * w
      val normY = if (maxW - minW <= 0.01) 0.5f else ((value - minW) / span).toFloat()
      val y = padY + h - normY * h
      Offset(x, y)
    }

    val path = Path().apply {
      moveTo(coords.first().x, coords.first().y)
      for (i in 1 until coords.size) {
        val prev = coords[i - 1]
        val curr = coords[i]
        val cx = (prev.x + curr.x) / 2f
        cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
      }
    }

    val area = Path().apply {
      addPath(path)
      lineTo(coords.last().x, padY + h)
      lineTo(coords.first().x, padY + h)
      close()
    }

    drawPath(
      path = area,
      brush = Brush.verticalGradient(
        colors = listOf(strokeColor.copy(alpha = 0.28f), Color.Transparent),
        startY = padY,
        endY = padY + h
      )
    )

    drawPath(
      path = path,
      color = strokeColor,
      style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    val peakIdx = weights.indexOfFirst { abs(it - maxW) < 0.001 }
    if (peakIdx in coords.indices && peakIdx != coords.lastIndex) {
      drawPrStarMarker(
        center = coords[peakIdx],
        outerRadius = 4.8.dp.toPx(),
        fillColor = AccentAmber,
        strokeColor = AccentAmberDark,
        coreColor = surfaceColor
      )
    }

    val last = coords.last()
    if (peakIdx == coords.lastIndex && maxW > minW) {
      drawPrStarMarker(
        center = last,
        outerRadius = 5.dp.toPx(),
        fillColor = AccentAmber,
        strokeColor = AccentAmberDark,
        coreColor = surfaceColor
      )
    } else {
      drawCircle(color = strokeColor, radius = 3.5.dp.toPx(), center = last)
      drawCircle(color = surfaceColor, radius = 1.6.dp.toPx(), center = last)
    }
  }
}
