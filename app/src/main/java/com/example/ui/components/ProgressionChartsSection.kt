package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.WorkoutSessionEntity
import com.example.data.repository.ChartPoint
import com.example.data.repository.ChartTimeRange
import com.example.data.repository.LiftPrSummary
import com.example.data.repository.ProgressionChartState
import com.example.data.repository.SessionProgression
import com.example.model.Plate
import com.example.model.WeightUnit
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.PlateGreen
import kotlin.math.abs

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

  val activePoint = selectedPointIndex?.let { idx ->
    chartState.points.getOrNull(idx)
  } ?: chartState.points.lastOrNull()

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

        // Timeframe Filter Row (7D, 30D, 90D, All)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                .defaultMinSize(minHeight = 40.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onTimeRangeChange(range) }
                .testTag("chart_range_${range.label.lowercase()}")
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 8.dp)
              ) {
                Text(
                  text = range.label,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
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
      ProgressionKpiRow(chartState = chartState)

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (chartState.selectedLift == "All") {
                  "Achieved Weight Over Time (${chartState.unit.shortLabel.uppercase()})"
                } else {
                  "${chartState.selectedLift} Progression (${chartState.unit.shortLabel.uppercase()})"
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
          }

          WeightProgressionCanvasChart(
            points = chartState.points,
            minWeight = chartState.minWeight,
            maxWeight = chartState.maxWeight,
            unit = chartState.unit,
            selectedIndex = selectedPointIndex,
            onSelectIndex = { selectedPointIndex = it }
          )

          // Legend Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            ChartLegendItem(
              color = MaterialTheme.colorScheme.primary,
              label = "Achieved Load"
            )
            ChartLegendItem(
              color = AccentAmber,
              label = "Personal Best"
            )
          }

          // Selected Session Inspector
          if (activePoint != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            SelectedSessionInspectorCard(
              point = activePoint,
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
private fun ProgressionKpiRow(
  chartState: ProgressionChartState,
  modifier: Modifier = Modifier
) {
  val unitLabel = chartState.unit.shortLabel
  val best = chartState.personalBestSession
  val net = chartState.netChange
  val avg = chartState.averageWeight

  Row(
    modifier = modifier
      .fillMaxWidth()
      .testTag("progression_kpi_row"),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Card 1: Personal Best
    KpiMetricCard(
      title = "Best (PR)",
      value = best?.let { "${Plate.formatWeight(it.achievedWeight)} $unitLabel" } ?: "--",
      subtitle = best?.liftName ?: "No data",
      accentColor = AccentAmber,
      icon = {
        Icon(
          imageVector = Icons.Default.EmojiEvents,
          contentDescription = "Personal Best",
          tint = Color(0xFF996B00),
          modifier = Modifier.size(16.dp)
        )
      },
      modifier = Modifier
        .weight(1f)
        .testTag("kpi_personal_best")
    )

    // Card 2: Net Change
    val isPositive = (net ?: 0.0) >= 0.0
    val netText = when {
      net == null -> "--"
      abs(net) < 0.01 -> "0 $unitLabel"
      net > 0 -> "+${Plate.formatWeight(net)} $unitLabel"
      else -> "-${Plate.formatWeight(abs(net))} $unitLabel"
    }
    KpiMetricCard(
      title = "Net Trend",
      value = netText,
      subtitle = chartState.latestSession?.let { "Latest: ${Plate.formatWeight(it.achievedWeight)}" } ?: "--",
      accentColor = if (net != null && net > 0) PlateGreen else MaterialTheme.colorScheme.primary,
      icon = {
        Icon(
          imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
          contentDescription = "Net Trend",
          tint = if (net != null && net > 0) PlateGreen else MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp)
        )
      },
      modifier = Modifier
        .weight(1f)
        .testTag("kpi_net_trend")
    )

    // Card 3: Sessions & Avg
    KpiMetricCard(
      title = "Sessions",
      value = "${chartState.points.size}",
      subtitle = avg?.let { "Avg ${Plate.formatWeight(it)} $unitLabel" } ?: "--",
      accentColor = MaterialTheme.colorScheme.secondary,
      icon = {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ShowChart,
          contentDescription = "Session Count",
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(16.dp)
        )
      },
      modifier = Modifier
        .weight(1f)
        .testTag("kpi_session_count")
    )
  }
}

@Composable
private fun KpiMetricCard(
  title: String,
  value: String,
  subtitle: String,
  accentColor: Color,
  icon: @Composable () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.16f)),
          contentAlignment = Alignment.Center
        ) {
          icon()
        }
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontSize = 10.sp
      )
    }
  }
}

@Composable
private fun SelectedSessionInspectorCard(
  point: ChartPoint,
  onLoadSession: () -> Unit,
  modifier: Modifier = Modifier
) {
  val session = point.session
  val delta = point.deltaFromPrevious

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    modifier = modifier
      .fillMaxWidth()
      .testTag("selected_session_inspector")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = session.formattedAchievedWeight(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
          )
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
          ) {
            Text(
              text = session.liftName,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
              fontSize = 10.sp
            )
          }
          if (point.isPersonalBest) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = AccentAmber.copy(alpha = 0.22f)
            ) {
              Text(
                text = "PR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFF996B00),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 10.sp
              )
            }
          }
          if (delta != null && abs(delta) >= 0.01) {
            val sign = if (delta > 0) "+" else "-"
            val fg = if (delta > 0) PlateGreen else MaterialTheme.colorScheme.onSurfaceVariant
            Text(
              text = "$sign${Plate.formatWeight(abs(delta))}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = fg
            )
          }
        }

        val details = buildList {
          add(session.getFormattedDate())
          add(session.barName)
          if (session.reps.isNotBlank()) add(session.reps)
        }.joinToString(" • ")

        Text(
          text = details,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        if (session.notes.isNotBlank()) {
          Text(
            text = session.notes,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      FilledTonalButton(
        onClick = onLoadSession,
        modifier = Modifier
          .defaultMinSize(minHeight = 48.dp)
          .testTag("inspector_load_on_bar_button")
      ) {
        Icon(
          imageVector = Icons.Default.FitnessCenter,
          contentDescription = null,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Load", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun LiftPersonalBestsCard(
  summaries: List<LiftPrSummary>,
  onSelectLift: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("lift_personal_bests_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text(
        text = "Personal Bests by Lift",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      summaries.forEach { summary ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelectLift(summary.liftName) }
            .testTag("lift_pr_row_${summary.liftName.lowercase().replace(" ", "_")}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = summary.liftName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${summary.sessionCount} ${if (summary.sessionCount == 1) "session" else "sessions"} • ${summary.bestSession.getRelativeTimeSpan()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = summary.bestSession.formattedAchievedWeight(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
              )
              Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Best Lift",
                tint = Color(0xFF996B00),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ChartLegendItem(
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
private fun ChartsEmptyStateCard(
  title: String,
  description: String,
  actionLabel: String,
  onAction: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ShowChart,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(28.dp)
        )
      }
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(2.dp))
      FilledTonalButton(
        onClick = onAction,
        modifier = Modifier
          .defaultMinSize(minHeight = 48.dp)
          .testTag("charts_empty_action_button")
      ) {
        Icon(
          imageVector = Icons.Default.BookmarkAdd,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(actionLabel, fontWeight = FontWeight.Bold)
      }
    }
  }
}
