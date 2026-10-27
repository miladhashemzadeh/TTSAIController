package com.vampyrelord.ttsaicontroller.features.touch.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vampyrelord.ttsaicontroller.core.designsystem.AmberWarning
import com.vampyrelord.ttsaicontroller.core.designsystem.AppCard
import com.vampyrelord.ttsaicontroller.core.designsystem.CyberCyan
import com.vampyrelord.ttsaicontroller.core.designsystem.DeepIndigo
import com.vampyrelord.ttsaicontroller.core.designsystem.EmeraldGreen
import com.vampyrelord.ttsaicontroller.core.designsystem.NeonPurple
import com.vampyrelord.ttsaicontroller.core.designsystem.RoseError
import com.vampyrelord.ttsaicontroller.core.designsystem.SectionHeader
import com.vampyrelord.ttsaicontroller.core.designsystem.StatusBadge
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.AutomaticPlacementStrategy
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlacementMode
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.RushwulfSettings
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchActionType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionStatus
import com.vampyrelord.ttsaicontroller.features.touch.presentation.mvi.TouchAutomationIntent
import com.vampyrelord.ttsaicontroller.features.touch.presentation.mvi.TouchAutomationState
import kotlin.math.roundToInt

@Composable
fun TouchAutomationScreen(
    viewModel: TouchAutomationViewModel,
    modifier: Modifier = Modifier,
    onRequestAccessibilitySettings: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    TouchAutomationScreen(
        state = state,
        onIntent = viewModel::sendIntent,
        modifier = modifier,
        onRequestAccessibilitySettings = onRequestAccessibilitySettings
    )
}

@Composable
fun TouchAutomationScreen(
    state: TouchAutomationState,
    onIntent: (TouchAutomationIntent) -> Unit,
    modifier: Modifier = Modifier,
    onRequestAccessibilitySettings: (() -> Unit)? = null
) {
    val isRunning = state.executionState.status == TouchExecutionStatus.RUNNING

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status & Rushwulf Header
        item {
            AppCard(
                borderColor = if (isRunning) CyberCyan else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOUCH AUTOMATION",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = CyberCyan
                        )
                        Text(
                            text = "Rushwulf Engine • ${state.rushwulfSettings.profileName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(
                        status = state.executionState.status.name,
                        isActive = isRunning,
                        color = when (state.executionState.status) {
                            TouchExecutionStatus.RUNNING -> CyberCyan
                            TouchExecutionStatus.COMPLETED -> EmeraldGreen
                            TouchExecutionStatus.ERROR -> RoseError
                            TouchExecutionStatus.CANCELLED -> AmberWarning
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onIntent(TouchAutomationIntent.OpenSettingsDialog) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("⚙ Rushwulf Settings")
                    }
                    OutlinedButton(
                        onClick = { onIntent(TouchAutomationIntent.OpenSaveDialog) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("💾 Save Routine")
                    }
                }
            }
        }

        // Saved Configurations Row
        item {
            AppCard {
                SectionHeader(
                    title = "Saved Automation Configurations",
                    subtitle = "Load previous touch patterns or presets"
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.savedConfigs) { config ->
                        val isSelected = state.selectedConfigId == config.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) CyberCyan else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (isSelected) CyberCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                )
                                .clickable { onIntent(TouchAutomationIntent.LoadConfig(config)) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = config.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${config.points.size} points • ${config.strategy.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Configuration Controls (Touch Count, Mode, Strategy)
        item {
            AppCard {
                SectionHeader(
                    title = "Touch Point Configuration",
                    subtitle = "Configure point count, coordinate placement, and distribution"
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Point Count Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Touch Points: ${state.touchCount}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 2, 3, 4, 5, 8).forEach { count ->
                            val isSelected = state.touchCount == count
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) NeonPurple else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onIntent(TouchAutomationIntent.SetTouchCount(count)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Toggle: Automatic vs Manual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlacementMode.entries.forEach { mode ->
                        val isSelected = state.placementMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) CyberCyan else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (isSelected) CyberCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                )
                                .clickable { onIntent(TouchAutomationIntent.SetPlacementMode(mode)) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (mode == PlacementMode.AUTOMATIC) "Auto Strategy" else "Manual Positioning",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Automatic Strategy Selector (if Automatic mode)
                AnimatedVisibility(visible = state.placementMode == PlacementMode.AUTOMATIC) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "Automatic Placement Strategy:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(AutomaticPlacementStrategy.entries) { strategy ->
                                val isSelected = state.automaticStrategy == strategy
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) NeonPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonPurple else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            onIntent(TouchAutomationIntent.SetAutomaticStrategy(strategy))
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = strategy.name.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) NeonPurple else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Interactive Touch Preview Canvas
        item {
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Visual Position Preview",
                        subtitle = if (state.placementMode == PlacementMode.MANUAL) "Tap canvas to place / move selected point" else "Real-time layout preview"
                    )
                    if (state.placementMode == PlacementMode.AUTOMATIC) {
                        OutlinedButton(
                            onClick = { onIntent(TouchAutomationIntent.RegeneratePositions) }
                        ) {
                            Text("Regenerate")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Canvas Container (aspect ratio ~ 9:14 phone screen preview)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepIndigo)
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .pointerInput(state.placementMode, state.selectedPointIndex) {
                            detectTapGestures { offset ->
                                val normalizedX = (offset.x / size.width).coerceIn(0.05f, 0.95f)
                                val normalizedY = (offset.y / size.height).coerceIn(0.05f, 0.95f)
                                val sel = state.selectedPointIndex ?: 0
                                onIntent(
                                    TouchAutomationIntent.UpdatePointPosition(sel, normalizedX, normalizedY)
                                )
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Draw subtle grid lines
                        val gridCols = 4
                        val gridRows = 6
                        for (c in 1 until gridCols) {
                            val x = canvasWidth * (c.toFloat() / gridCols)
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(x, 0f),
                                end = Offset(x, canvasHeight),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )
                        }
                        for (r in 1 until gridRows) {
                            val y = canvasHeight * (r.toFloat() / gridRows)
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )
                        }

                        // Draw path lines connecting sequential points
                        val points = state.touchPoints
                        if (points.size > 1) {
                            for (i in 0 until points.size - 1) {
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                val o1 = Offset(p1.x * canvasWidth, p1.y * canvasHeight)
                                val o2 = Offset(p2.x * canvasWidth, p2.y * canvasHeight)
                                drawLine(
                                    color = CyberCyan.copy(alpha = 0.5f),
                                    start = o1,
                                    end = o2,
                                    strokeWidth = 3f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                                )
                            }
                        }

                        // Draw point markers
                        points.forEachIndexed { index, pt ->
                            val center = Offset(pt.x * canvasWidth, pt.y * canvasHeight)
                            val isSelected = state.selectedPointIndex == index
                            val isActiveStep = isRunning && state.executionState.activeStepIndex == index

                            // Outer glow if active or selected
                            if (isActiveStep) {
                                drawCircle(
                                    color = EmeraldGreen.copy(alpha = 0.4f),
                                    radius = 28f,
                                    center = center
                                )
                            } else if (isSelected) {
                                drawCircle(
                                    color = NeonPurple.copy(alpha = 0.35f),
                                    radius = 26f,
                                    center = center
                                )
                            }

                            // Main circle badge
                            drawCircle(
                                color = if (isActiveStep) EmeraldGreen else if (isSelected) NeonPurple else CyberCyan,
                                radius = 18f,
                                center = center
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 18f,
                                center = center,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                            )
                        }
                    }

                    // Overlay HTML/Text badges on top of canvas for touch points
                    state.touchPoints.forEachIndexed { index, pt ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = (pt.x * 280).dp.coerceAtLeast(0.dp),
                                    top = (pt.y * 200).dp.coerceAtLeast(0.dp)
                                )
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                ),
                                modifier = Modifier
                                    .padding(start = 12.dp, top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Validation Errors or Platform Permissions Alerts
        item {
            state.validationError?.let { error ->
                AppCard(borderColor = RoseError) {
                    Text(
                        text = "VALIDATION ERROR",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = RoseError
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = error, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (state.platformCapability.requiresAccessibilityService && !state.platformCapability.isAccessibilityServiceEnabled) {
                AppCard(borderColor = AmberWarning) {
                    Text(
                        text = "ANDROID ACCESSIBILITY PERMISSION REQUIRED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AmberWarning
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.platformCapability.guidanceMessage,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (onRequestAccessibilitySettings != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRequestAccessibilitySettings,
                            colors = ButtonDefaults.buttonColors(containerColor = AmberWarning)
                        ) {
                            Text("Open Accessibility Settings", color = Color.Black)
                        }
                    }
                }
            }
        }

        // Execution Status & Progress
        item {
            AnimatedVisibility(visible = isRunning || state.executionState.status == TouchExecutionStatus.COMPLETED) {
                AppCard(
                    borderColor = if (isRunning) CyberCyan else EmeraldGreen
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRunning) "AUTOMATION EXECUTING..." else "SEQUENCE FINISHED",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isRunning) CyberCyan else EmeraldGreen
                        )
                        Text(
                            text = "Step ${state.executionState.activeStepIndex + 1}/${state.touchPoints.size} (Loop ${state.executionState.currentIteration}/${state.executionState.totalIterations})",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = if (state.touchPoints.isNotEmpty()) {
                        (state.executionState.activeStepIndex + 1).toFloat() / state.touchPoints.size
                    } else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Single Confirm & Start Button + Stop Control
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onIntent(TouchAutomationIntent.ConfirmAndStart) },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    enabled = !isRunning
                ) {
                    Text(
                        text = if (isRunning) "Executing..." else "Confirm and Start Automation",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                if (isRunning) {
                    Button(
                        onClick = { onIntent(TouchAutomationIntent.StopAutomation) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                    ) {
                        Text("Stop", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Position List & Point Editor
        item {
            SectionHeader(
                title = "Configured Touch Points (${state.touchPoints.size})",
                subtitle = "Reorder, edit action type, duration, and delay for each touch step"
            )
        }

        itemsIndexed(state.touchPoints) { index, point ->
            val isSelected = state.selectedPointIndex == index
            AppCard(
                borderColor = if (isSelected) NeonPurple else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonPurple else CyberCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                        }
                        Column {
                            Text(
                                text = "Point #${index + 1} (${point.actionType.name})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "X: ${((point.x * 100).roundToInt())}% • Y: ${((point.y * 100).roundToInt())}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Reorder & Delete Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (index > 0) {
                            OutlinedButton(
                                onClick = { onIntent(TouchAutomationIntent.ReorderPoints(index, index - 1)) },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("▲")
                            }
                        }
                        if (index < state.touchPoints.lastIndex) {
                            OutlinedButton(
                                onClick = { onIntent(TouchAutomationIntent.ReorderPoints(index, index + 1)) },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("▼")
                            }
                        }
                        OutlinedButton(
                            onClick = { onIntent(TouchAutomationIntent.RemovePoint(index)) },
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError)
                        ) {
                            Text("✕")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Type Selector
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TouchActionType.entries) { action ->
                        val isActionSelected = point.actionType == action
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    1.dp,
                                    if (isActionSelected) CyberCyan else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(6.dp)
                                )
                                .background(
                                    if (isActionSelected) CyberCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                )
                                .clickable {
                                    onIntent(
                                        TouchAutomationIntent.UpdatePointAction(
                                            index = index,
                                            action = action,
                                            durationMs = point.durationMillis,
                                            delayMs = point.delayMillis
                                        )
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = action.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isActionSelected) CyberCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Step Delay Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Pre-step Delay: ${point.delayMillis}ms", style = MaterialTheme.typography.bodySmall)
                }
                Slider(
                    value = point.delayMillis.toFloat(),
                    onValueChange = {
                        onIntent(
                            TouchAutomationIntent.UpdatePointAction(
                                index = index,
                                action = point.actionType,
                                durationMs = point.durationMillis,
                                delayMs = it.toLong()
                            )
                        )
                    },
                    valueRange = 0f..1000f,
                    steps = 19
                )
            }
        }

        // Add Manual Point Button
        item {
            OutlinedButton(
                onClick = { onIntent(TouchAutomationIntent.AddPoint()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Add Another Touch Position")
            }
        }
    }

    // Rushwulf Settings Modal Dialog
    if (state.isSettingsDialogOpen) {
        RushwulfSettingsDialog(
            settings = state.rushwulfSettings,
            onDismiss = { onIntent(TouchAutomationIntent.CloseSettingsDialog) },
            onUpdate = { transform ->
                onIntent(TouchAutomationIntent.UpdateRushwulfSettings(transform))
            }
        )
    }

    // Save Configuration Modal Dialog
    if (state.isSaveDialogOpen) {
        SaveConfigurationDialog(
            currentName = state.configNameInput,
            onDismiss = { onIntent(TouchAutomationIntent.CloseSaveDialog) },
            onSave = { name ->
                onIntent(TouchAutomationIntent.UpdateConfigNameInput(name))
                onIntent(TouchAutomationIntent.SaveCurrentConfig)
            }
        )
    }
}

@Composable
fun RushwulfSettingsDialog(
    settings: RushwulfSettings,
    onDismiss: () -> Unit,
    onUpdate: ((RushwulfSettings) -> RushwulfSettings) -> Unit
) {
    var profileName by remember { mutableStateOf(settings.profileName) }
    var tapDuration by remember { mutableStateOf(settings.defaultTapDurationMs.toFloat()) }
    var stepDelay by remember { mutableStateOf(settings.defaultStepDelayMs.toFloat()) }
    var repeatCount by remember { mutableStateOf(settings.repeatCount) }
    var stopOnError by remember { mutableStateOf(settings.stopOnError) }
    var showOverlay by remember { mutableStateOf(settings.showVisualOverlay) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Rushwulf Automation Settings", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profile Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Default Step Delay: ${stepDelay.toInt()}ms", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = stepDelay,
                    onValueChange = { stepDelay = it },
                    valueRange = 20f..1000f,
                    steps = 19
                )

                Text(text = "Tap Duration: ${tapDuration.toInt()}ms", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = tapDuration,
                    onValueChange = { tapDuration = it },
                    valueRange = 10f..300f,
                    steps = 28
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Repeat Loop: $repeatCount times", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 2, 5, 10).forEach { r ->
                            Button(
                                onClick = { repeatCount = r },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (repeatCount == r) CyberCyan else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text("$r", color = Color.Black)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Halt on Error", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = stopOnError, onCheckedChange = { stopOnError = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Visual Overlay Cues", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = showOverlay, onCheckedChange = { showOverlay = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate {
                        it.copy(
                            profileName = profileName,
                            defaultTapDurationMs = tapDuration.toLong(),
                            defaultStepDelayMs = stepDelay.toLong(),
                            repeatCount = repeatCount,
                            stopOnError = stopOnError,
                            showVisualOverlay = showOverlay
                        )
                    }
                    onDismiss()
                }
            ) {
                Text("Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SaveConfigurationDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Touch Routine", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter a unique name to save and reuse this touch position configuration:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Configuration Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
