package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.WorkoutSessionEntity
import com.example.data.repository.ChartTimeRange
import com.example.data.repository.ProgressionChartState
import com.example.data.repository.SessionProgression
import com.example.model.WeightUnit
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberDark

@Composable
fun ProgressionChartsSection(
  chartState: ProgressionChartState,
  sessionProgressions: Map<Long, SessionProgression>,
  onUnitChange: (WeightUnit) -> Unit,
  onLiftFilterChange: (String) -> Unit,
  onTimeRangeChange: (ChartTimeRange) -> Unit,
  onSelectSession: (WorkoutSessionEntity) -> Unit,
  onDeleteSession: (Long) -> Unit,
  onSaveCurrentSession: () -> Unit,
  onExportCsv: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

  LaunchedEffect(
    chartState.points.size,
    chartState.unit,
    chartState.selectedLift,
    chartState.selectedTimeRange
  ) {
    selectedPointIndex = if (chartState.points.isNotEmpty()) chartState.points.lastIndex else null
  }

  val prMilestones = remember(chartState.points) {
    resolvePrMilestoneIndices(chartState.points)
  }
  val peakPrIndex = remember(chartState.points) {
    if (chartState.points.isEmpty()) {
      null
    } else {
      val maxWeight = chartState.points.maxOf { it.weight }
      chartState.points.indexOfLast { kotlin.math.abs(it.weight - maxWeight) < 0.001 }
        .takeIf { it >= 0 }
    }
  }

  val resolvedActiveIndex = (selectedPointIndex ?: chartState.points.lastIndex)
    .takeIf { chartState.points.isNotEmpty() }
    ?.coerceIn(0, chartState.points.lastIndex)
  val activePoint = resolvedActiveIndex?.let { chartState.points.getOrNull(it) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("progression_charts_section"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Header Card: Title, Unit Toggle, Time Range, and Lift Filter Chips
    Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Insights,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
              )
            }
            Column {
              Text(
                text = "Strength Progression",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = when {
                  chartState.lbsCount + chartState.kgCount == 0 -> "Log workouts to visualize strength trends"
                  else -> "${chartState.totalInUnitCount} ${chartState.unit.shortLabel} sessions logged"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // LBS / KG Segmented Toggle
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
          ) {
            Row(
              modifier = Modifier.padding(3.dp),
              horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              WeightUnit.entries.forEach { unit ->
                val isSelected = chartState.unit == unit
                Surface(
                  shape = RoundedCornerShape(11.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                  modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .clickable { onUnitChange(unit) }
                    .testTag("chart_unit_toggle_${unit.name.lowercase()}")
                ) {
                  Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = unit.shortLabel.uppercase(),
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }
          }
        }

        // Date Range Filter Row (1M, 3M, 6M, 1Y, All)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("chart_date_range_row"),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ChartTimeRange.entries.forEach { range ->
            val isSelected = chartState.selectedTimeRange == range
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = if (isSelected) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
              } else null,
              modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onTimeRangeChange(range) }
                .testTag("chart_range_${range.label.lowercase()}")
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
              ) {
                Text(
                  text = range.label,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  softWrap = false,
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        // Lift Filter Chips
        if (chartState.availableLifts.size > 1) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            chartState.availableLifts.forEach { lift ->
              val isSelected = chartState.selectedLift.equals(lift, ignoreCase = true)
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier
                  .defaultMinSize(minHeight = 38.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { onLiftFilterChange(lift) }
                  .testTag("chart_lift_filter_${lift.lowercase().replace(" ", "_")}")
              ) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                  Text(
                    text = lift,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    }

    // 2. Empty States or Active Chart Experience
    if (chartState.lbsCount + chartState.kgCount == 0) {
      ChartsEmptyStateCard(
        title = "No Logged Sessions Yet",
        description = "Save any barbell setup from the Calculator tab to start plotting your strength curve, PR milestones, and session deltas.",
        actionLabel = "Save Current Load",
        onAction = onSaveCurrentSession,
        modifier = Modifier.testTag("charts_empty_state")
      )
    } else if (chartState.points.isEmpty()) {
      val otherUnit = if (chartState.unit == WeightUnit.LBS) WeightUnit.KG else WeightUnit.LBS
      val otherUnitCount = if (chartState.unit == WeightUnit.LBS) chartState.kgCount else chartState.lbsCount
      when {
        chartState.totalInUnitCount == 0 && otherUnitCount > 0 -> {
          ChartsEmptyStateCard(
            title = "No ${chartState.unit.shortLabel.uppercase()} Sessions Logged",
            description = "You have $otherUnitCount ${if (otherUnitCount == 1) "session" else "sessions"} logged in ${otherUnit.shortLabel.uppercase()}. Switch units to view your progression chart.",
            actionLabel = "Switch to ${otherUnit.shortLabel.uppercase()}",
            onAction = { onUnitChange(otherUnit) },
            modifier = Modifier.testTag("charts_unit_empty_state")
          )
        }
        else -> {
          ChartsEmptyStateCard(
            title = "No Sessions in ${chartState.selectedTimeRange.label}",
            description = "No matching ${chartState.selectedLift} sessions were logged in this time window.",
            actionLabel = "Show All Time",
            onAction = {
              onTimeRangeChange(ChartTimeRange.ALL)
              onLiftFilterChange("All")
            },
            modifier = Modifier.testTag("charts_filter_empty_state")
          )
        }
      }
    } else {
      // 3. Summary KPI Row
      ProgressionKpiRow(
        chartState = chartState,
        prMilestoneCount = prMilestones.size
      )

      // 4. Main Progression Chart Card + Selected Point Inspector
      Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("progression_chart_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (chartState.selectedLift == "All") {
                  "Lift Weights Over Time (${chartState.unit.shortLabel.uppercase()})"
                } else {
                  "${chartState.selectedLift} Over Time (${chartState.unit.shortLabel.uppercase()})"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (chartState.points.size == 1) {
                  "Log one more session to draw your progression curve"
                } else {
                  "Tap or drag across points to inspect any session"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            OutlinedButton(
              onClick = onExportCsv,
              shape = RoundedCornerShape(14.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
              modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .testTag("export_progression_csv_button")
            ) {
              Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Export CSV",
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Export CSV",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
              )
            }
          }

          WeightProgressionCanvasChart(
            points = chartState.points,
            minWeight = chartState.minWeight,
            maxWeight = chartState.maxWeight,
            unit = chartState.unit,
            selectedIndex = resolvedActiveIndex,
            onSelectIndex = { selectedPointIndex = it }
          )

          // Legend + Jump to PR Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              ChartCircleLegendItem(
                color = MaterialTheme.colorScheme.primary,
                label = "Session"
              )
              ChartPrStarLegendItem(
                label = "Personal Record (${prMilestones.size})"
              )
            }

            if (peakPrIndex != null && chartState.points.size >= 2) {
              val isFocusedOnPeak = resolvedActiveIndex == peakPrIndex
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isFocusedOnPeak) {
                  AccentAmber.copy(alpha = 0.25f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                },
                border = BorderStroke(
                  1.dp,
                  if (isFocusedOnPeak) AccentAmber else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { selectedPointIndex = peakPrIndex }
                  .testTag("jump_to_pr_button")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Focus Peak PR",
                    tint = AccentAmberDark,
                    modifier = Modifier.size(13.dp)
                  )
                  Text(
                    text = "Focus PR",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentAmberDark
                  )
                }
              }
            }
          }

          // Selected Session Inspector
          if (activePoint != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            SelectedSessionInspectorCard(
              point = activePoint,
              isPrMilestone = resolvedActiveIndex in prMilestones,
              onLoadSession = { onSelectSession(activePoint.session) }
            )
          }
        }
      }

      // 5. Lift Personal Bests Breakdown (when viewing All with 2+ distinct lifts)
      if (chartState.selectedLift == "All" && chartState.liftSummaries.size > 1) {
        LiftPersonalBestsCard(
          summaries = chartState.liftSummaries,
          onSelectLift = onLiftFilterChange
        )
      }

      // 6. Filtered Sessions History List
      Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("chart_matching_sessions_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Matching Sessions (${chartState.points.size})",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Tap to load on bar",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          chartState.points.asReversed().forEach { pt ->
            WorkoutSessionRow(
              session = pt.session,
              progression = sessionProgressions[pt.session.id],
              onSelect = { onSelectSession(pt.session) },
              onDelete = { onDeleteSession(pt.session.id) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ChartCircleLegendItem(
  color: Color,
  label: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Box(
      modifier = Modifier
        .size(9.dp)
        .clip(CircleShape)
        .background(color)
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun ChartPrStarLegendItem(
  label: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    modifier = Modifier
      .semantics(mergeDescendants = true) {}
      .testTag("pr_legend_badge")
  ) {
    Canvas(modifier = Modifier.size(12.dp)) {
      val cx = size.width / 2f
      val cy = size.height / 2f
      val outer = size.minDimension / 2f
      val inner = outer * 0.44f
      val path = Path().apply {
        moveTo(cx, cy - outer)
        lineTo(cx + inner, cy - inner)
        lineTo(cx + outer, cy)
        lineTo(cx + inner, cy + inner)
        lineTo(cx, cy + outer)
        lineTo(cx - inner, cy + inner)
        lineTo(cx - outer, cy)
        lineTo(cx - inner, cy - inner)
        close()
      }
      drawPath(path = path, color = AccentAmber)
      drawPath(
        path = path,
        color = AccentAmberDark,
        style = Stroke(width = 1.2.dp.toPx(), join = StrokeJoin.Round)
      )
    }
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.SemiBold,
      color = AccentAmberDark
    )
  }
}
