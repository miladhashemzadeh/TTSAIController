package com.vampyrelord.ttsaicontroller.features.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vampyrelord.ttsaicontroller.core.designsystem.AppCard
import com.vampyrelord.ttsaicontroller.core.designsystem.CyberCyan
import com.vampyrelord.ttsaicontroller.core.designsystem.EmeraldGreen
import com.vampyrelord.ttsaicontroller.core.designsystem.NeonPurple
import com.vampyrelord.ttsaicontroller.core.designsystem.SectionHeader
import com.vampyrelord.ttsaicontroller.core.designsystem.StatusBadge
import com.vampyrelord.ttsaicontroller.core.designsystem.WaveformVisualizer
import com.vampyrelord.ttsaicontroller.core.navigation.Screen
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.mvi.DashboardIntent
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.mvi.DashboardState

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    DashboardScreen(
        state = state,
        onIntent = viewModel::sendIntent,
        modifier = modifier
    )
}

@Composable
fun DashboardScreen(
    state: DashboardState,
    onIntent: (DashboardIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = state.metrics

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Overview Card
        item {
            AppCard(
                borderColor = if (metrics.isListening) CyberCyan else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STT & TOUCH CONTROLLER",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = CyberCyan
                        )
                        Text(
                            text = "Clean Architecture • MVI • Multiplatform",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(
                        status = if (metrics.isListening) "LISTENING" else metrics.transcriptionStatus.name,
                        isActive = metrics.isListening
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                WaveformVisualizer(
                    isSpeaking = metrics.isListening,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (metrics.isListening) {
                        "Listening to audio input stream..."
                    } else if (metrics.lastTranscript != null) {
                        "Last Transcript: \"${metrics.lastTranscript}\""
                    } else {
                        "System standby. Speech-To-Text contracts ready for AI model (Whisper/Gemini/ONNX)."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Metrics KPI Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: STT Engine Status
                AppCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SPEECH TO TEXT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = metrics.transcriptionStatus.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        text = metrics.aiEngineName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Metric 2: Touch Automation Status
                AppCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOUCH AUTOMATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NeonPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (metrics.isTouchRunning) "RUNNING" else "STANDBY",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        text = "${metrics.totalSavedTouchConfigs} Saved Configs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Quick Command Actions
        item {
            AppCard {
                SectionHeader(
                    title = "Quick Operations",
                    subtitle = "One-click triggers to test MVI intent pipeline"
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!metrics.isListening) {
                        Button(
                            onClick = { onIntent(DashboardIntent.StartListening) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Text("Start Listening", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { onIntent(DashboardIntent.StopListening) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Stop & Transcribe", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onIntent(DashboardIntent.NavigateTo(Screen.TouchAutomation)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Touch Automation")
                    }
                    OutlinedButton(
                        onClick = { onIntent(DashboardIntent.NavigateTo(Screen.SttStudio)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Open STT Studio")
                    }
                }
            }
        }

        // Recent Activity Overview
        item {
            AppCard {
                SectionHeader(
                    title = "Recent System Activity",
                    subtitle = "Latest Speech-to-Text transcriptions"
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (state.recentTranscriptions.isEmpty()) {
                    Text(
                        text = "No recent transcriptions recorded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                state.recentTranscriptions.forEach { result ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "STT: \"${result.fullText.take(40)}\"",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Model: ${result.aiModelInfo}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(
                            status = "READY",
                            isActive = true,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }
    }
}
