package com.vampyrelord.ttsaicontroller.features.stt.presentation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vampyrelord.ttsaicontroller.core.designsystem.AmberWarning
import com.vampyrelord.ttsaicontroller.core.designsystem.AppCard
import com.vampyrelord.ttsaicontroller.core.designsystem.CyberCyan
import com.vampyrelord.ttsaicontroller.core.designsystem.EmeraldGreen
import com.vampyrelord.ttsaicontroller.core.designsystem.NeonPurple
import com.vampyrelord.ttsaicontroller.core.designsystem.RoseError
import com.vampyrelord.ttsaicontroller.core.designsystem.SectionHeader
import com.vampyrelord.ttsaicontroller.core.designsystem.StatusBadge
import com.vampyrelord.ttsaicontroller.core.designsystem.WaveformVisualizer
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus
import com.vampyrelord.ttsaicontroller.features.stt.presentation.mvi.SttIntent
import com.vampyrelord.ttsaicontroller.features.stt.presentation.mvi.SttState

@Composable
fun SttScreen(
    viewModel: SttViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    SttScreen(
        state = state,
        onIntent = viewModel::sendIntent,
        modifier = modifier
    )
}

@Composable
fun SttScreen(
    state: SttState,
    onIntent: (SttIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = state.status == TranscriptionStatus.LISTENING

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Model Placeholder Banner
        item {
            AppCard(borderColor = NeonPurple) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI SPEECH-TO-TEXT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = NeonPurple
                        )
                        Text(
                            text = state.capability.statusDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(
                        status = state.status.name,
                        isActive = isListening,
                        color = if (isListening) CyberCyan else NeonPurple
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                WaveformVisualizer(
                    isSpeaking = isListening,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Live Audio Controls (Start / Stop Listening)
        item {
            AppCard {
                SectionHeader(
                    title = "Live Speech Recognition",
                    subtitle = "Shared STT contract functions ready for AI model execution"
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isListening) {
                        Button(
                            onClick = { onIntent(SttIntent.StartListening) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Text("🎙 Start Listening", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { onIntent(SttIntent.StopListening) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("⏹ Stop & Transcribe", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onIntent(SttIntent.CancelListening) },
                            modifier = Modifier.weight(0.7f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError)
                        ) {
                            Text("Cancel")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // Language Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Language:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    listOf("en", "es", "fr", "de", "auto").forEach { lang ->
                        val isSelected = state.audioConfig.language == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) CyberCyan else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onIntent(SttIntent.UpdateLanguage(lang)) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = lang.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Live Transcript Output Area
        item {
            AppCard(borderColor = if (isListening) CyberCyan else MaterialTheme.colorScheme.surfaceVariant) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRANSCRIPTION OUTPUT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CyberCyan
                    )
                    if (state.liveTranscript.isNotEmpty()) {
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = RoseError,
                            modifier = Modifier.clickable { onIntent(SttIntent.ClearTranscript) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(14.dp)
                ) {
                    Text(
                        text = if (state.liveTranscript.isNotEmpty()) {
                            state.liveTranscript
                        } else if (isListening) {
                            "Listening to microphone input... (AI model placeholder)"
                        } else {
                            "No transcript available yet. Press 'Start Listening' to initiate voice capture."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        ),
                        color = if (state.liveTranscript.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                state.lastResult?.let { res ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Model: ${res.aiModelInfo ?: "AI Model"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonPurple
                        )
                        Text(
                            text = "Confidence: ${(res.confidence * 100).toInt()}% • ${res.durationMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }

        // AI Model Integration Guidance Card
        item {
            AppCard(borderColor = AmberWarning) {
                Text(
                    text = "AI MODEL INTEGRATION INFO",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AmberWarning
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "All shared Speech-to-Text contracts, use cases, MVI architecture, and RTK events are wired and ready. To enable speech recognition, plug your chosen AI model (e.g. OpenAI Whisper, Google Gemini Audio API, or Sherpa-ONNX) into the SpeechToTextRepository implementation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
