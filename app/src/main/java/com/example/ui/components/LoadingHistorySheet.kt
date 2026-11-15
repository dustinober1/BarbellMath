package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.LoadingPlanEntity
import com.example.data.entity.WorkoutSessionEntity
import com.example.data.repository.SessionProgression
import com.example.model.WeightUnit
import com.example.ui.theme.PlateBlack
import com.example.ui.theme.PlateBlue
import com.example.ui.theme.PlateCyan
import com.example.ui.theme.PlateGreen
import com.example.ui.theme.PlateOrange
import com.example.ui.theme.PlatePurple
import com.example.ui.theme.PlateRed
import com.example.ui.theme.PlateWhite
import com.example.ui.theme.PlateYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingHistorySheet(
  recentPlans: List<LoadingPlanEntity>,
  onSelectPlan: (LoadingPlanEntity) -> Unit,
  onDeletePlan: (Long) -> Unit,
  onClearAll: () -> Unit,
  onDismiss: () -> Unit,
  recentSessions: List<WorkoutSessionEntity> = emptyList(),
  sessionProgressions: Map<Long, SessionProgression> = emptyMap(),
  onSelectSession: (WorkoutSessionEntity) -> Unit = {},
  onDeleteSession: (Long) -> Unit = {},
  onClearAllSessions: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var showClearConfirmDialog by remember { mutableStateOf(false) }
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Recent Sessions, 1: Auto-Saved Plans
  var selectedLiftFilter by remember { mutableStateOf("All") }

  val liftFilters = remember(recentSessions) {
    val distinctLifts = recentSessions.map { it.liftName }.distinct()
    listOf("All") + distinctLifts
  }

  val filteredSessions = remember(recentSessions, selectedLiftFilter) {
    if (selectedLiftFilter == "All") {
      recentSessions
    } else {
      recentSessions.filter { it.liftName == selectedLiftFilter }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    modifier = modifier.testTag("loading_history_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(20.dp)
            )
          }

          Column {
            Text(
              text = if (selectedTab == 0) "Recent Sessions" else "Auto-Saved Plans",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (selectedTab == 0) {
                if (recentSessions.isNotEmpty()) "${recentSessions.size} logged sessions" else "Track your lift progression over time"
              } else {
                if (recentPlans.isNotEmpty()) "${recentPlans.size} of 10 auto-saved" else "Up to 10 calculations kept"
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        val canClear = if (selectedTab == 0) recentSessions.isNotEmpty() else recentPlans.isNotEmpty()
        if (canClear) {
          TextButton(
            onClick = { showClearConfirmDialog = true },
            modifier = Modifier.testTag("clear_history_button")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteSweep,
              contentDescription = "Clear all",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Clear All",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.error
            )
          }
        }
      }

      // Segmented Tab Switcher
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (selectedTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
            shadowElevation = if (selectedTab == 0) 1.dp else 0.dp,
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { selectedTab = 0 }
              .testTag("history_tab_sessions")
          ) {
            Text(
              text = "Recent Sessions (${recentSessions.size})",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (selectedTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
            shadowElevation = if (selectedTab == 1) 1.dp else 0.dp,
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { selectedTab = 1 }
              .testTag("history_tab_autosaved")
          ) {
            Text(
              text = "Auto-Saved (${recentPlans.size})",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      }

      if (selectedTab == 0) {
        if (recentSessions.isEmpty()) {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 20.dp)
              .testTag("empty_sessions_sheet_indicator")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(52.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.History,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(26.dp)
                )
              }
              Text(
                text = "No Logged Sessions Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Use \"Save to Recent Sessions\" on the Calculator to log your lifts and track progression deltas and PRs over time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        } else {
          // Lift Filter Chips
          if (liftFilters.size > 2) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 10.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              liftFilters.forEach { lift ->
                val isSelected = selectedLiftFilter == lift
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { selectedLiftFilter = lift }
                ) {
                  Text(
                    text = lift,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }

          // Progression Summary Banner for filtered sessions
          val bestSession = filteredSessions.maxByOrNull { it.achievedWeight }
          if (bestSession != null) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = if (selectedLiftFilter == "All") "Top Logged Load" else "$selectedLiftFilter Best",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                  Text(
                    text = bestSession.formattedAchievedWeight(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "Sessions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                  Text(
                    text = "${filteredSessions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                }
              }
            }
          }

          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(380.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(
              items = filteredSessions,
              key = { it.id }
            ) { session ->
              WorkoutSessionRow(
                session = session,
                progression = sessionProgressions[session.id],
                onSelect = {
                  onSelectSession(session)
                  onDismiss()
                },
                onDelete = { onDeleteSession(session.id) }
              )
            }
            item {
              Spacer(modifier = Modifier.height(16.dp))
            }
          }
        }
      } else {
        // Auto-saved plans tab
        if (recentPlans.isEmpty()) {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp)
              .testTag("empty_history_indicator")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.History,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(28.dp)
                )
              }
              Text(
                text = "No Auto-Saved Plans Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Calculate any target weight on the barbell and your last 10 loading plans will automatically be preserved here for instant recall.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(400.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(
              items = recentPlans,
              key = { it.id }
            ) { plan ->
              HistoryPlanCard(
                plan = plan,
                onSelect = {
                  onSelectPlan(plan)
                  onDismiss()
                },
                onDelete = { onDeletePlan(plan.id) }
              )
            }

            item {
              Spacer(modifier = Modifier.height(16.dp))
            }
          }
        }
      }
    }
  }

  // Confirmation dialog for clearing all items in active tab
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = {
        Text(if (selectedTab == 0) "Clear All Recent Sessions?" else "Clear Auto-Saved Plans?")
      },
      text = {
        Text(
          if (selectedTab == 0) {
            "Are you sure you want to remove all logged workout sessions? This action cannot be undone."
          } else {
            "Are you sure you want to remove all auto-saved loading plans? This action cannot be undone."
          }
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (selectedTab == 0) onClearAllSessions() else onClearAll()
            showClearConfirmDialog = false
          }
        ) {
          Text("Clear", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryPlanCard(
  plan: LoadingPlanEntity,
  onSelect: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val plates = plan.parsePlates()

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    ),
    shadowElevation = 0.dp,
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .clickable(onClick = onSelect)
      .testTag("history_item_${plan.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Top Row: Target Weight, Exact Badge, Timestamp, Delete Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = plan.formattedWeight(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp
          )

          if (plan.isExact) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = PlateGreen.copy(alpha = 0.15f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = PlateGreen,
                  modifier = Modifier.size(12.dp)
                )
                Text(
                  text = "Exact",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = PlateGreen,
                  fontSize = 10.sp
                )
              }
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = plan.getRelativeTimeSpan(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(32.dp)
              .testTag("delete_history_item_${plan.id}")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete from history",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Barbell & Equipment info
      Text(
        text = "${plan.barName} • ${plan.weightUnit.format(plan.barWeight)}" +
          if (plan.collarsEnabled) " (+ Collars)" else "",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      // Plates Breakdown chips & Quick Tap to Load row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (plates.isEmpty()) {
          Text(
            text = "Empty Bar",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            plates.forEach { (weight, qty) ->
              PlateMiniChip(weight = weight, quantity = qty, unit = plan.weightUnit)
            }
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.padding(start = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "Load",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(10.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PlateMiniChip(
  weight: Double,
  quantity: Int,
  unit: WeightUnit,
  modifier: Modifier = Modifier
) {
  val formatted = if (weight % 1.0 == 0.0) "${weight.toInt()}" else "$weight"
  val plateColor = resolvePlateColor(weight, unit)

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
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
          .border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
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

private fun resolvePlateColor(weight: Double, unit: WeightUnit): Color {
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
