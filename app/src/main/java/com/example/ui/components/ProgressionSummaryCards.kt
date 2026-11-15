package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ChartPoint
import com.example.data.repository.LiftPrSummary
import com.example.data.repository.ProgressionChartState
import com.example.model.Plate
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberDark
import com.example.ui.theme.PlateGreen
import kotlin.math.abs

@Composable
fun ProgressionKpiRow(
  chartState: ProgressionChartState,
  prMilestoneCount: Int,
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
    val prSubtitle = when {
      best == null -> "No data"
      prMilestoneCount > 1 -> "${best.liftName} • $prMilestoneCount PRs"
      else -> best.liftName
    }
    KpiMetricCard(
      title = "Best (PR)",
      value = best?.let { "${Plate.formatWeight(it.achievedWeight)} $unitLabel" } ?: "--",
      subtitle = prSubtitle,
      accentColor = AccentAmber,
      icon = {
        Icon(
          imageVector = Icons.Default.EmojiEvents,
          contentDescription = "Personal Best",
          tint = AccentAmberDark,
          modifier = Modifier.size(16.dp)
        )
      },
      modifier = Modifier
        .weight(1f)
        .testTag("kpi_personal_best")
    )

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
fun SelectedSessionInspectorCard(
  point: ChartPoint,
  isPrMilestone: Boolean,
  onLoadSession: () -> Unit,
  modifier: Modifier = Modifier
) {
  val session = point.session
  val delta = point.deltaFromPrevious

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    border = if (isPrMilestone) {
      BorderStroke(1.dp, AccentAmber.copy(alpha = 0.45f))
    } else {
      null
    },
    modifier = modifier
      .fillMaxWidth()
      .semantics(mergeDescendants = true) {}
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
            color = if (isPrMilestone) AccentAmberDark else MaterialTheme.colorScheme.primary
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
          if (isPrMilestone) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = AccentAmber.copy(alpha = 0.22f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = null,
                  tint = AccentAmberDark,
                  modifier = Modifier.size(10.dp)
                )
                Text(
                  text = "PR",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Black,
                  color = AccentAmberDark,
                  fontSize = 10.sp
                )
              }
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

        val volume = session.totalVolume()
        val details = buildList {
          add(session.getFormattedDate())
          add(session.barName)
          if (session.reps.isNotBlank()) add(session.reps)
          if (volume != null) add("${Plate.formatWeight(volume)} ${session.weightUnit.shortLabel} vol")
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
fun LiftPersonalBestsCard(
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
                tint = AccentAmberDark,
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
fun ChartsEmptyStateCard(
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
