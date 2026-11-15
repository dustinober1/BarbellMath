package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.WorkoutSessionEntity
import com.example.data.repository.SessionProgression
import com.example.model.Plate
import com.example.model.WeightUnit
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.PlateBlack
import com.example.ui.theme.PlateBlue
import com.example.ui.theme.PlateCyan
import com.example.ui.theme.PlateGreen
import com.example.ui.theme.PlateOrange
import com.example.ui.theme.PlatePurple
import com.example.ui.theme.PlateRed
import com.example.ui.theme.PlateWhite
import com.example.ui.theme.PlateYellow
import kotlin.math.abs

@Composable
fun RecentSessionsCard(
  sessions: List<WorkoutSessionEntity>,
  progressions: Map<Long, SessionProgression>,
  onSaveCurrentSession: () -> Unit,
  onSelectSession: (WorkoutSessionEntity) -> Unit,
  onDeleteSession: (Long) -> Unit,
  onViewAllSessions: () -> Unit,
  onOpenProgressCharts: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("recent_sessions_card"),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header
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
              .size(34.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(18.dp)
            )
          }

          Column {
            Text(
              text = "Recent Sessions",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (sessions.isEmpty()) {
                "Log calculated lifts to track progression"
              } else {
                "${sessions.size} ${if (sessions.size == 1) "session" else "sessions"} logged"
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (sessions.isNotEmpty()) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            TextButton(
              onClick = onSaveCurrentSession,
              modifier = Modifier.testTag("header_log_session_button")
            ) {
              Text(
                text = "+ Log",
                fontWeight = FontWeight.Bold
              )
            }
            TextButton(
              onClick = onViewAllSessions,
              modifier = Modifier.testTag("view_all_sessions_button")
            ) {
              Text(
                text = "View All",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      if (sessions.isEmpty()) {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_recent_sessions_state")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text(
              text = "No sessions saved yet. Log your sets, reps, and barbell load to track lifting history over time.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(
              onClick = onSaveCurrentSession,
              modifier = Modifier.testTag("empty_state_save_session_button")
            ) {
              Icon(
                imageVector = Icons.Default.BookmarkAdd,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Log Sets & Reps", fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        RecentSessionsTrendPreview(
          sessions = sessions,
          onOpenProgressCharts = onOpenProgressCharts
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          sessions.take(5).forEach { session ->
            WorkoutSessionRow(
              session = session,
              progression = progressions[session.id],
              onSelect = { onSelectSession(session) },
              onDelete = { onDeleteSession(session.id) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun RecentSessionsTrendPreview(
  sessions: List<WorkoutSessionEntity>,
  onOpenProgressCharts: () -> Unit,
  modifier: Modifier = Modifier
) {
  val latest = sessions.firstOrNull() ?: return
  val activeUnit = latest.weightUnit
  val chronologicalSameUnit = sessions
    .filter { it.weightUnit == activeUnit }
    .sortedWith(compareBy<WorkoutSessionEntity> { it.timestamp }.thenBy { it.id })
    .takeLast(12)

  val weights = chronologicalSameUnit.map { it.achievedWeight }
  val netDelta = if (weights.size >= 2) weights.last() - weights.first() else null
  val bestWeight = weights.maxOrNull() ?: latest.achievedWeight

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ),
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onOpenProgressCharts)
      .testTag("recent_sessions_trend_preview")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "Strength Trend",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (netDelta != null && abs(netDelta) >= 0.01) {
            val sign = if (netDelta > 0) "+" else "-"
            val fg = if (netDelta > 0) PlateGreen else MaterialTheme.colorScheme.onSurfaceVariant
            Text(
              text = "$sign${Plate.formatWeight(abs(netDelta))} ${activeUnit.shortLabel}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = fg
            )
          }
        }
        Text(
          text = "Best ${Plate.formatWeight(bestWeight)} ${activeUnit.shortLabel} • Tap for full charts",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }

      if (weights.size >= 2) {
        MiniProgressionSparkline(
          weights = weights,
          isPositiveOrFlat = (netDelta ?: 0.0) >= 0.0,
          modifier = Modifier
            .width(84.dp)
            .height(34.dp)
            .padding(horizontal = 6.dp)
        )
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primary
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ShowChart,
            contentDescription = "Open Progress Charts",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "Charts",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 11.sp
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutSessionRow(
  session: WorkoutSessionEntity,
  progression: SessionProgression?,
  onSelect: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val plates = session.parsePlates()
  val delta = progression?.deltaFromPrevious

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    ),
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onSelect)
      .testTag("session_item_${session.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // Top row: Weight, Lift Tag, Progression Badge, Time & Delete
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f)
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

          if (session.reps.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier
                .semantics(mergeDescendants = true) {}
                .testTag("session_reps_badge_${session.id}")
            ) {
              Text(
                text = session.reps,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                fontSize = 10.sp
              )
            }
          }

          if (progression?.isPersonalBest == true) {
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
                  imageVector = Icons.Default.EmojiEvents,
                  contentDescription = "Personal Best",
                  tint = Color(0xFF996B00),
                  modifier = Modifier.size(11.dp)
                )
                Text(
                  text = "PR",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF996B00),
                  fontSize = 10.sp
                )
              }
            }
          }

          if (delta != null && abs(delta) >= 0.01) {
            val isPositive = delta > 0
            val badgeBg = if (isPositive) PlateGreen.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant
            val badgeFg = if (isPositive) PlateGreen else MaterialTheme.colorScheme.onSurfaceVariant
            val sign = if (isPositive) "+" else "-"
            val formattedDelta = Plate.formatWeight(abs(delta))

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = badgeBg
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
              ) {
                Icon(
                  imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                  contentDescription = null,
                  tint = badgeFg,
                  modifier = Modifier.size(11.dp)
                )
                Text(
                  text = "$sign$formattedDelta ${session.weightUnit.shortLabel}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = badgeFg,
                  fontSize = 10.sp
                )
              }
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          Text(
            text = session.getRelativeTimeSpan(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("delete_session_${session.id}")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete session",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Equipment + optional volume/notes
      val computedVolume = session.totalVolume()
      val detailParts = buildList {
        add(session.barName)
        if (computedVolume != null) {
          add("${Plate.formatWeight(computedVolume)} ${session.weightUnit.shortLabel} vol")
        }
        if (session.notes.isNotBlank()) add(session.notes)
      }
      Text(
        text = detailParts.joinToString(" • "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      // Plates per side chips
      if (plates.isEmpty()) {
        Text(
          text = "Empty Bar",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      } else {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          plates.forEach { (weight, qty) ->
            SessionPlateMiniChip(
              weight = weight,
              quantity = qty,
              unit = session.weightUnit
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SessionPlateMiniChip(
  weight: Double,
  quantity: Int,
  unit: WeightUnit,
  modifier: Modifier = Modifier
) {
  val formatted = Plate.formatWeight(weight)
  val plateColor = resolveSessionPlateColor(weight, unit)

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surface,
    modifier = modifier
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(plateColor)
      )
      Text(
        text = "$formatted × $quantity",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private fun resolveSessionPlateColor(weight: Double, unit: WeightUnit): Color {
  return if (unit == WeightUnit.LBS) {
    when (weight) {
      45.0 -> PlateRed
      35.0 -> PlateBlue
      25.0 -> PlateYellow
      10.0 -> PlateGreen
      5.0 -> PlateWhite
      2.5 -> PlateBlack
      1.25 -> PlatePurple
      0.75 -> PlateOrange
      0.5 -> PlateCyan
      else -> Color(0xFF757575)
    }
  } else {
    when (weight) {
      25.0 -> PlateRed
      20.0 -> PlateBlue
      15.0 -> PlateYellow
      10.0 -> PlateGreen
      5.0 -> PlateWhite
      2.5 -> PlateBlack
      1.25 -> PlatePurple
      0.5 -> PlateCyan
      0.25 -> PlateOrange
      else -> Color(0xFF757575)
    }
  }
}
