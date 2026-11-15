package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.WeightUnit
import com.example.ui.components.BarSelectorSheet
import com.example.ui.components.BarbellCanvasSection
import com.example.ui.components.InventoryManagerSheet
import com.example.ui.components.InventorySettingsScreen
import com.example.ui.components.LoadingHistorySheet
import com.example.ui.components.PlateBreakdownCard
import com.example.ui.components.ReversePlateBuilderSection
import com.example.ui.components.TargetWeightControlCard
import com.example.ui.components.WarmupLadderSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarbellAppScreen(
  viewModel: BarbellViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val recentPlans by viewModel.recentPlans.collectAsStateWithLifecycle()

  var isBarSelectorSheetOpen by remember { mutableStateOf(false) }
  var isInventorySheetOpen by remember { mutableStateOf(false) }
  var isHistorySheetOpen by remember { mutableStateOf(false) }

  val effectiveBarWeight = if (uiState.selectedBar.isCustom) {
    uiState.customBarWeight
  } else {
    uiState.selectedBar.getWeight(uiState.unit)
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsPadding(WindowInsets.statusBars)
          .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Plate Master",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
          )
          Text(
            text = "Barbell Calculator",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          IconButton(
            onClick = { isHistorySheetOpen = true },
            modifier = Modifier.testTag("open_history_button")
          ) {
            if (recentPlans.isNotEmpty()) {
              BadgedBox(
                badge = {
                  Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                  ) {
                    Text(
                      text = "${recentPlans.size}",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Default.History,
                  contentDescription = "Workout History",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            } else {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = "Workout History",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(
            onClick = { viewModel.setActiveTab(3) },
            modifier = Modifier.testTag("open_inventory_button")
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Home Gym Settings",
              tint = if (uiState.activeTab == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.FitnessCenter,
              contentDescription = "Barbell",
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
          .testTag("main_navigation_bar")
          .windowInsetsPadding(WindowInsets.navigationBars)
      ) {
        NavigationBarItem(
          selected = uiState.activeTab == 0,
          onClick = { viewModel.setActiveTab(0) },
          icon = {
            Icon(
              imageVector = Icons.Default.Calculate,
              contentDescription = "Calculator"
            )
          },
          label = { Text("Calculator") },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary
          ),
          modifier = Modifier.testTag("nav_tab_calculator")
        )

        NavigationBarItem(
          selected = uiState.activeTab == 1,
          onClick = { viewModel.setActiveTab(1) },
          icon = {
            Icon(
              imageVector = Icons.Default.TrendingUp,
              contentDescription = "Warm-up"
            )
          },
          label = { Text("Warm-up") },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary
          ),
          modifier = Modifier.testTag("nav_tab_warmup")
        )

        NavigationBarItem(
          selected = uiState.activeTab == 2,
          onClick = { viewModel.setActiveTab(2) },
          icon = {
            Icon(
              imageVector = Icons.Default.Layers,
              contentDescription = "Racker"
            )
          },
          label = { Text("Reverse Rack") },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary
          ),
          modifier = Modifier.testTag("nav_tab_reverse_rack")
        )

        NavigationBarItem(
          selected = uiState.activeTab == 3,
          onClick = { viewModel.setActiveTab(3) },
          icon = {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Settings"
            )
          },
          label = { Text("Settings") },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary
          ),
          modifier = Modifier.testTag("nav_tab_settings")
        )
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))
      }

      // Tab 0: Standard Plate Calculator
      if (uiState.activeTab == 0) {
        val activePlates = if (uiState.unit == WeightUnit.LBS) uiState.lbsPlates else uiState.kgPlates
        item {
          TargetWeightControlCard(
            targetWeight = uiState.targetWeight,
            unit = uiState.unit,
            selectedBar = uiState.selectedBar,
            customBarWeight = uiState.customBarWeight,
            onTargetWeightChange = { viewModel.setTargetWeight(it) },
            onAdjustWeight = { viewModel.adjustTargetWeight(it) },
            onUnitChange = { viewModel.setUnit(it) },
            onOpenBarSelector = { isBarSelectorSheetOpen = true },
            availablePlates = activePlates
          )
        }

        item {
          BarbellCanvasSection(
            platesPerSide = uiState.calculationResult.platesPerSide,
            unit = uiState.unit,
            barName = uiState.selectedBar.name,
            barWeightFormatted = uiState.unit.format(effectiveBarWeight),
            isFullBar = uiState.isFullBarView,
            onToggleFullBar = { viewModel.toggleFullBarView() },
            showPlateLabels = uiState.showPlateLabels,
            onTogglePlateLabels = { viewModel.togglePlateLabels() }
          )
        }

        item {
          PlateBreakdownCard(
            result = uiState.calculationResult,
            onOpenInventorySettings = { viewModel.setActiveTab(3) }
          )
        }
      }

      // Tab 1: Warm-up Sets Ladder
      if (uiState.activeTab == 1) {
        val activePlates = if (uiState.unit == WeightUnit.LBS) uiState.lbsPlates else uiState.kgPlates
        item {
          TargetWeightControlCard(
            targetWeight = uiState.targetWeight,
            unit = uiState.unit,
            selectedBar = uiState.selectedBar,
            customBarWeight = uiState.customBarWeight,
            onTargetWeightChange = { viewModel.setTargetWeight(it) },
            onAdjustWeight = { viewModel.adjustTargetWeight(it) },
            onUnitChange = { viewModel.setUnit(it) },
            onOpenBarSelector = { isBarSelectorSheetOpen = true },
            availablePlates = activePlates
          )
        }

        item {
          WarmupLadderSection(
            warmupSets = uiState.warmupSets,
            unit = uiState.unit,
            onSelectWeight = {
              viewModel.setTargetWeight(it)
              viewModel.setActiveTab(0)
            }
          )
        }
      }

      // Tab 2: Reverse Plate Racker
      if (uiState.activeTab == 2) {
        item {
          ReversePlateBuilderSection(
            reversePlatesPerSide = uiState.reversePlatesPerSide,
            reverseTotalWeight = uiState.reverseTotalWeight,
            availablePlates = if (uiState.unit == com.example.model.WeightUnit.LBS) uiState.lbsPlates else uiState.kgPlates,
            bar = uiState.selectedBar,
            customBarWeight = uiState.customBarWeight,
            collarsEnabled = uiState.collarsEnabled,
            unit = uiState.unit,
            onAddPlate = { viewModel.addReversePlate(it) },
            onRemovePlate = { viewModel.removeReversePlate(it) },
            onClearPlates = { viewModel.clearReversePlates() },
            onApplyToTarget = { viewModel.applyReverseWeightToTarget() }
          )
        }
      }

      // Tab 3: Home Gym Inventory Settings Screen
      if (uiState.activeTab == 3) {
        item {
          InventorySettingsScreen(
            plates = if (uiState.unit == WeightUnit.LBS) uiState.lbsPlates else uiState.kgPlates,
            unit = uiState.unit,
            selectedBar = uiState.selectedBar,
            customBarWeight = uiState.customBarWeight,
            collarsEnabled = uiState.collarsEnabled,
            onTogglePlate = { viewModel.togglePlateEnabled(it) },
            onUpdatePairs = { weight, pairs -> viewModel.updatePlateAvailablePairs(weight, pairs) },
            onAddCustomPlate = { weight, color, pairs -> viewModel.addCustomPlate(weight, color, pairs) },
            onUpdateCustomPlate = { original, newWeight, color, pairs ->
              viewModel.updateCustomPlate(original, newWeight, color, pairs)
            },
            onDeleteCustomPlate = { viewModel.deleteCustomPlate(it) },
            onUnitChange = { viewModel.setUnit(it) },
            onExclude35s = { viewModel.exclude35LbPlates() },
            onEnableAll = { viewModel.enableAllPlates() },
            onApplyMinimalist = { viewModel.applyMinimalistPreset() },
            onResetDefaults = { viewModel.resetPlateInventory() },
            onOpenBarSelector = { isBarSelectorSheetOpen = true },
            onNavigateBack = { viewModel.setActiveTab(0) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // Bar Selector Modal Sheet
  if (isBarSelectorSheetOpen) {
    BarSelectorSheet(
      currentBar = uiState.selectedBar,
      customBarWeight = uiState.customBarWeight,
      unit = uiState.unit,
      collarsEnabled = uiState.collarsEnabled,
      onSelectBar = { viewModel.selectBar(it) },
      onUpdateCustomWeight = { viewModel.setCustomBarWeight(it) },
      onToggleCollars = { viewModel.toggleCollars(it) },
      onDismiss = { isBarSelectorSheetOpen = false }
    )
  }

  // Gym Inventory Manager Modal Sheet
  if (isInventorySheetOpen) {
    InventoryManagerSheet(
      plates = if (uiState.unit == com.example.model.WeightUnit.LBS) uiState.lbsPlates else uiState.kgPlates,
      unit = uiState.unit,
      onTogglePlateEnabled = { viewModel.togglePlateEnabled(it) },
      onUpdateAvailablePairs = { weight, pairs -> viewModel.updatePlateAvailablePairs(weight, pairs) },
      onResetInventory = { viewModel.resetPlateInventory() },
      onDismiss = { isInventorySheetOpen = false }
    )
  }

  // Workout History Modal Sheet
  if (isHistorySheetOpen) {
    LoadingHistorySheet(
      recentPlans = recentPlans,
      onSelectPlan = { viewModel.loadPlan(it) },
      onDeletePlan = { viewModel.deleteHistoryPlan(it) },
      onClearAll = { viewModel.clearHistory() },
      onDismiss = { isHistorySheetOpen = false }
    )
  }
}
