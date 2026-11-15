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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import com.example.ui.theme.PlateGreen
import kotlin.math.abs
import kotlin.math.roundToInt

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
  val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
  val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
  val prColor = AccentAmber

  val textMeasurer = rememberTextMeasurer()
  val axisTextStyle = TextStyle(
    fontSize = 10.sp,
    fontWeight = FontWeight.Medium,
    color = labelColor
  )
  val prBadgeTextStyle = TextStyle(
    fontSize = 9.sp,
    fontWeight = FontWeight.Bold,
    color = Color(0xFF996B00)
  )

  val sweepProgress by animateFloatAsState(
    targetValue = 1f,
    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
    label = "chart_sweep"
  )

  val weightRange = (maxWeight - minWeight).coerceAtLeast(1.0)
  val maxPointWeight = points.maxOf { it.weight }

  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(224.dp)
      .semantics {
        contentDescription = "Weight progression chart with ${points.size} logged sessions in ${unit.shortLabel}"
      }
      .testTag("weight_progression_canvas")
      .pointerInput(points) {
        val leftPad = 48.dp.toPx()
        val rightPad = 16.dp.toPx()
        val plotW = (size.width - leftPad - rightPad).coerceAtLeast(1f)

        fun resolveNearestIndex(touchX: Float): Int {
          if (points.size <= 1) return 0
          val clamped = ((touchX - leftPad) / plotW).coerceIn(0f, 1f)
          return (clamped * (points.size - 1)).roundToInt().coerceIn(0, points.lastIndex)
        }

        detectTapGestures { offset ->
          onSelectIndex(resolveNearestIndex(offset.x))
        }
      }
      .pointerInput(points) {
        val leftPad = 48.dp.toPx()
        val rightPad = 16.dp.toPx()
        val plotW = (size.width - leftPad - rightPad).coerceAtLeast(1f)

        fun resolveNearestIndex(touchX: Float): Int {
          if (points.size <= 1) return 0
          val clamped = ((touchX - leftPad) / plotW).coerceIn(0f, 1f)
          return (clamped * (points.size - 1)).roundToInt().coerceIn(0, points.lastIndex)
        }

        detectHorizontalDragGestures { change, _ ->
          change.consume()
          onSelectIndex(resolveNearestIndex(change.position.x))
        }
      }
  ) {
    val leftPad = 48.dp.toPx()
    val rightPad = 16.dp.toPx()
    val topPad = 16.dp.toPx()
    val bottomPad = 28.dp.toPx()

    val plotWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
    val plotHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)
    val plotBottom = topPad + plotHeight

    // 1. Horizontal dashed gridlines & Y-axis labels (4 intervals -> 5 lines)
    val gridSteps = 4
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
    for (step in 0..gridSteps) {
      val fraction = step / gridSteps.toFloat()
      val y = plotBottom - fraction * plotHeight
      val tickWeight = minWeight + fraction * weightRange

      drawLine(
        color = gridColor,
        start = Offset(leftPad, y),
        end = Offset(leftPad + plotWidth, y),
        strokeWidth = 1.dp.toPx(),
        pathEffect = if (step == 0) null else dashEffect
      )

      val label = Plate.formatWeight((tickWeight * 10.0).roundToInt() / 10.0)
      val measured = textMeasurer.measure(text = label, style = axisTextStyle)
      drawText(
        textLayoutResult = measured,
        topLeft = Offset(
          x = (leftPad - measured.size.width - 6.dp.toPx()).coerceAtLeast(0f),
          y = (y - measured.size.height / 2f).coerceIn(0f, size.height - measured.size.height)
        )
      )
    }

    // Helper to map point index & weight to Canvas coordinates
    fun pointOffset(index: Int, weight: Double): Offset {
      val x = if (points.size == 1) {
        leftPad + plotWidth / 2f
      } else {
        leftPad + (index.toFloat() / (points.size - 1).toFloat()) * plotWidth
      }
      val normalizedY = ((weight - minWeight) / weightRange).toFloat().coerceIn(0f, 1f)
      val y = plotBottom - normalizedY * plotHeight
      return Offset(x, y)
    }

    val coordinates = points.mapIndexed { idx, pt -> pointOffset(idx, pt.weight) }

    // 2. PR ceiling reference line (if >= 2 points and there's variation)
    if (points.size >= 2) {
      val prY = pointOffset(0, maxPointWeight).y
      drawLine(
        color = prColor.copy(alpha = 0.55f),
        start = Offset(leftPad, prY),
        end = Offset(leftPad + plotWidth, prY),
        strokeWidth = 1.2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
      )
      val prLabel = textMeasurer.measure("PR ${Plate.formatWeight(maxPointWeight)}", prBadgeTextStyle)
      drawText(
        textLayoutResult = prLabel,
        topLeft = Offset(
          x = (leftPad + plotWidth - prLabel.size.width).coerceAtLeast(leftPad),
          y = (prY - prLabel.size.height - 2.dp.toPx()).coerceAtLeast(0f)
        )
      )
    }

    // 3. Area fill & Trend Line with animated horizontal reveal
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

      val revealRight = leftPad + plotWidth * sweepProgress + 8.dp.toPx()
      clipRect(left = leftPad - 4.dp.toPx(), top = 0f, right = revealRight, bottom = plotBottom + 4.dp.toPx()) {
        drawPath(
          path = areaPath,
          brush = Brush.verticalGradient(
            colors = listOf(
              primaryColor.copy(alpha = 0.28f),
              primaryColor.copy(alpha = 0.04f),
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
      // Single-point dashed horizontal baseline
      val onlyPt = coordinates.first()
      drawLine(
        color = primaryColor.copy(alpha = 0.45f),
        start = Offset(leftPad, onlyPt.y),
        end = Offset(leftPad + plotWidth, onlyPt.y),
        strokeWidth = 2.dp.toPx(),
        pathEffect = dashEffect
      )
    }

    // 4. Vertical crosshair for selected point
    val activeIdx = (selectedIndex ?: points.lastIndex).coerceIn(0, points.lastIndex)
    val selectedCoord = coordinates[activeIdx]
    drawLine(
      color = primaryColor.copy(alpha = 0.35f),
      start = Offset(selectedCoord.x, topPad),
      end = Offset(selectedCoord.x, plotBottom),
      strokeWidth = 1.5.dp.toPx(),
      pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
    )

    // 5. Data Nodes & PR Milestones
    coordinates.forEachIndexed { index, center ->
      val pt = points[index]
      val isSelected = index == activeIdx
      val isPr = pt.isPersonalBest || (points.size >= 2 && abs(pt.weight - maxPointWeight) < 0.001 && pt.weight == maxPointWeight && index == points.indexOfFirst { it.weight == maxPointWeight } && points.first().weight < maxPointWeight)

      if (isSelected) {
        drawCircle(
          color = (if (isPr) prColor else primaryColor).copy(alpha = 0.22f),
          radius = 12.dp.toPx(),
          center = center
        )
      }

      if (isPr) {
        drawCircle(
          color = prColor,
          radius = if (isSelected) 6.5.dp.toPx() else 5.5.dp.toPx(),
          center = center
        )
        drawCircle(
          color = surfaceColor,
          radius = if (isSelected) 3.dp.toPx() else 2.4.dp.toPx(),
          center = center
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

    // 6. X-axis date labels (start, middle, end without overlap)
    val labelIndices = when {
      points.size == 1 -> listOf(0)
      points.size == 2 -> listOf(0, 1)
      else -> listOf(0, points.size / 2, points.lastIndex).distinct()
    }
    val labelY = plotBottom + 8.dp.toPx()
    var lastLabelRight = -1f

    labelIndices.forEach { idx ->
      val text = points[idx].dateLabel
      val measured = textMeasurer.measure(text, axisTextStyle)
      val xCenter = coordinates[idx].x
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

    val last = coords.last()
    drawCircle(color = strokeColor, radius = 3.5.dp.toPx(), center = last)
    drawCircle(color = surfaceColor, radius = 1.6.dp.toPx(), center = last)
  }
}
