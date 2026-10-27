package com.vampyrelord.ttsaicontroller.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vampyrelord.ttsaicontroller.core.designsystem.AppCard
import com.vampyrelord.ttsaicontroller.core.designsystem.CyberCyan
import com.vampyrelord.ttsaicontroller.core.designsystem.SectionHeader
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppThemeMode
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AudioOutputRouting
import com.vampyrelord.ttsaicontroller.features.settings.presentation.mvi.SettingsIntent
import com.vampyrelord.ttsaicontroller.features.settings.presentation.mvi.SettingsState

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    SettingsScreen(
        state = state,
        onIntent = viewModel::sendIntent,
        modifier = modifier
    )
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Platform Diagnostic Information
        item {
            AppCard {
                SectionHeader(
                    title = "System & Platform Diagnostic",
                    subtitle = "Shared Kotlin Multiplatform environment info"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Target Runtime:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = state.platformName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyberCyan
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Architecture:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Layered Modular Clean + MVI",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Appearance & Theme Mode
        item {
            AppCard {
                SectionHeader(
                    title = "Theme & Appearance",
                    subtitle = "Select application visual style"
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        val isSelected = state.settings.themeMode == mode
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
                                .clickable { onIntent(SettingsIntent.SetThemeMode(mode)) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Agent RTK & Audio Behaviors
        item {
            AppCard {
                SectionHeader(
                    title = "Autonomous Agent & Engine Rules",
                    subtitle = "Configure automated execution permissions"
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Autonomous Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Agent Autonomous Execution",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Allow AI agents to queue and trigger speech synthesis automatically",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.settings.agentAutonomousEnabled,
                        onCheckedChange = { onIntent(SettingsIntent.ToggleAgentAutonomous(it)) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto Play on Queue Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-play Sequential Speech",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Automatically advance through queued speech jobs upon completion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.settings.autoPlayOnQueue,
                        onCheckedChange = { onIntent(SettingsIntent.ToggleAutoPlay(it)) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Waveform Animation Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Audio Waveform Visualizer",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Render real-time reactive audio waveform bars when speaking",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.settings.showWaveformAnimation,
                        onCheckedChange = { onIntent(SettingsIntent.ToggleWaveform(it)) }
                    )
                }
            }
        }

        // Audio Output Routing
        item {
            AppCard {
                SectionHeader(
                    title = "Audio Output Device Routing",
                    subtitle = "Select audio sink hardware destination"
                )
                Spacer(modifier = Modifier.height(10.dp))

                AudioOutputRouting.entries.forEach { routing ->
                    val isSelected = state.settings.audioRouting == routing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyberCyan.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                            .clickable { onIntent(SettingsIntent.SetAudioRouting(routing)) }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (routing) {
                                AudioOutputRouting.DEFAULT_SPEAKERS -> "Default System Speakers"
                                AudioOutputRouting.HEADPHONES -> "Headphones / Monitor"
                                AudioOutputRouting.VIRTUAL_AUDIO_CABLE -> "Virtual Audio Cable (OBS / Stream)"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyberCyan
                            )
                        }
                    }
                }
            }
        }

        // Reset Settings Button
        item {
            OutlinedButton(
                onClick = { onIntent(SettingsIntent.ResetToDefaults) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset All Settings to Defaults")
            }
        }
    }
}
