package com.vampyrelord.ttsaicontroller.features.tts.presentation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vampyrelord.ttsaicontroller.core.designsystem.AppCard
import com.vampyrelord.ttsaicontroller.core.designsystem.CyberCyan
import com.vampyrelord.ttsaicontroller.core.designsystem.EmeraldGreen
import com.vampyrelord.ttsaicontroller.core.designsystem.NeonPurple
import com.vampyrelord.ttsaicontroller.core.designsystem.RoseError
import com.vampyrelord.ttsaicontroller.core.designsystem.SectionHeader
import com.vampyrelord.ttsaicontroller.core.designsystem.StatusBadge
import com.vampyrelord.ttsaicontroller.core.designsystem.WaveformVisualizer
import com.vampyrelord.ttsaicontroller.features.tts.presentation.mvi.TtsIntent
import com.vampyrelord.ttsaicontroller.features.tts.presentation.mvi.TtsState
import kotlin.math.roundToInt

@Composable
fun TtsScreen(
    viewModel: TtsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    TtsScreen(
        state = state,
        onIntent = viewModel::sendIntent,
        modifier = modifier
    )
}

@Composable
fun TtsScreen(
    state: TtsState,
    onIntent: (TtsIntent) -> Unit,
    modifier: Modifier = Modifier
) {

    val quickPresets = listOf(
        "AI Agent dispatch completed successfully.",
        "System online. All audio pipelines operational.",
        "Executing autonomous task sequence with high priority."
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Status & Waveform Banner
        item {
            AppCard(
                borderColor = if (state.engineStatus.isSpeaking) CyberCyan else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ENGINE STATUS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                state.engineStatus.isSpeaking -> "Speaking: \"${state.engineStatus.activeJob?.text?.take(30)}...\""
                                state.engineStatus.isPaused -> "Audio Paused"
                                else -> "Engine Idle & Ready"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    StatusBadge(
                        status = when {
                            state.engineStatus.isSpeaking -> "PLAYING"
                            state.engineStatus.isPaused -> "PAUSED"
                            else -> "IDLE"
                        },
                        isActive = state.engineStatus.isSpeaking
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                WaveformVisualizer(
                    isSpeaking = state.engineStatus.isSpeaking,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Text Input & Quick Prompts
        item {
            AppCard {
                SectionHeader(
                    title = "Speech Synthesis Input",
                    subtitle = "Enter text for synthesis or test voice presets"
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.inputText,
                    onValueChange = { onIntent(TtsIntent.UpdateInputText(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { Text("Enter text to speak...") }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${state.inputText.length} characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickPresets) { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    onIntent(TtsIntent.UpdateInputText(preset))
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset.take(24) + "...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // Voice Profile Selection
        item {
            AppCard {
                SectionHeader(
                    title = "Voice Selection",
                    subtitle = "Select AI neural voice model"
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.availableVoices) { voice ->
                        val isSelected = state.selectedVoice?.id == voice.id
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
                                .clickable {
                                    onIntent(TtsIntent.SelectVoice(voice))
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = voice.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (voice.isNeural) {
                                        Text(
                                            text = "NEURAL",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = NeonPurple
                                        )
                                    }
                                }
                                Text(
                                    text = "${voice.language} • ${voice.gender}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modulation Sliders (Pitch, Rate, Volume)
        item {
            AppCard {
                SectionHeader(
                    title = "Voice Modulation & Parameters",
                    subtitle = "Adjust pitch, speech rate, and playback volume"
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Pitch Slider
                Text(
                    text = "Pitch: ${((state.pitch * 100).roundToInt() / 100f)}x",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Slider(
                    value = state.pitch,
                    onValueChange = { onIntent(TtsIntent.UpdatePitch(it)) },
                    valueRange = 0.5f..2.0f
                )

                // Rate Slider
                Text(
                    text = "Speech Rate: ${((state.rate * 100).roundToInt() / 100f)}x",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Slider(
                    value = state.rate,
                    onValueChange = { onIntent(TtsIntent.UpdateRate(it)) },
                    valueRange = 0.5f..2.5f
                )

                // Volume Slider
                Text(
                    text = "Volume: ${(state.volume * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Slider(
                    value = state.volume,
                    onValueChange = { onIntent(TtsIntent.UpdateVolume(it)) },
                    valueRange = 0.0f..1.0f
                )
            }
        }

        // Action Controls (Speak, Pause, Resume, Stop)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onIntent(TtsIntent.SpeakCurrentText) },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Speak Now", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }

                if (state.engineStatus.isSpeaking && !state.engineStatus.isPaused) {
                    OutlinedButton(
                        onClick = { onIntent(TtsIntent.PausePlayback) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pause")
                    }
                } else if (state.engineStatus.isPaused) {
                    Button(
                        onClick = { onIntent(TtsIntent.ResumePlayback) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text("Resume")
                    }
                }

                OutlinedButton(
                    onClick = { onIntent(TtsIntent.StopPlayback) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError)
                ) {
                    Text("Stop")
                }
            }
        }

        // Speech Queue & History
        item {
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Speech Queue & History",
                        subtitle = "${state.queue.size} in queue • ${state.history.size} past jobs"
                    )
                    if (state.queue.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { onIntent(TtsIntent.ClearQueue) }
                        ) {
                            Text("Clear Queue")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (state.queue.isEmpty() && state.history.isEmpty()) {
                    Text(
                        text = "No jobs processed yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                state.queue.forEach { job ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = job.text,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                            Text(
                                text = "Source: ${job.source} • Voice: ${job.voice.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = "QUEUED", isActive = false, color = CyberCyan)
                    }
                }

                state.history.take(5).forEach { job ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = job.text,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                            Text(
                                text = "Source: ${job.source} • Voice: ${job.voice.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = job.status.name, isActive = job.status.name == "COMPLETED")
                    }
                }
            }
        }
    }
}
