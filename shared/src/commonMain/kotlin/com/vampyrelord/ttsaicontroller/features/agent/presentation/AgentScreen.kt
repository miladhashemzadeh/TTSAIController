package com.vampyrelord.ttsaicontroller.features.agent.presentation

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.sp
import com.vampyrelord.ttsaicontroller.core.designsystem.AmberWarning
import com.vampyrelord.ttsaicontroller.core.designsystem.AppCard
import com.vampyrelord.ttsaicontroller.core.designsystem.CyberCyan
import com.vampyrelord.ttsaicontroller.core.designsystem.EmeraldGreen
import com.vampyrelord.ttsaicontroller.core.designsystem.NeonPurple
import com.vampyrelord.ttsaicontroller.core.designsystem.RoseError
import com.vampyrelord.ttsaicontroller.core.designsystem.SectionHeader
import com.vampyrelord.ttsaicontroller.core.designsystem.StatusBadge
import com.vampyrelord.ttsaicontroller.core.rtk.AgentExecutionStatus
import com.vampyrelord.ttsaicontroller.features.agent.presentation.mvi.AgentIntent
import com.vampyrelord.ttsaicontroller.features.agent.presentation.mvi.AgentState
import com.vampyrelord.ttsaicontroller.features.agent.presentation.mvi.AgentWorkspaceTab

@Composable
fun AgentScreen(
    viewModel: AgentViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    AgentScreen(
        state = state,
        onIntent = viewModel::sendIntent,
        modifier = modifier
    )
}

@Composable
fun AgentScreen(
    state: AgentState,
    onIntent: (AgentIntent) -> Unit,
    modifier: Modifier = Modifier
) {

    val quickCommands = listOf(
        "speak System diagnostics complete. RTK store verified.",
        "stop",
        "status",
        "list voices",
        "pause",
        "resume"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Profile Selector
        item {
            AppCard {
                SectionHeader(
                    title = "Agent Persona & Role",
                    subtitle = "Select which AI agent is commanding the TTS engine"
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.availableProfiles) { profile ->
                        val isSelected = state.selectedProfile?.id == profile.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) NeonPurple else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (isSelected) NeonPurple.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                )
                                .clickable { onIntent(AgentIntent.SelectProfile(profile)) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) NeonPurple else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = profile.role,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tab Navigation for Workspace
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AgentWorkspaceTab.entries.forEach { tab ->
                    val isSelected = state.activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) CyberCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onIntent(AgentIntent.SwitchTab(tab)) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (tab) {
                                AgentWorkspaceTab.CONSOLE -> "Console"
                                AgentWorkspaceTab.TOOLS -> "Tools (${state.availableTools.size})"
                                AgentWorkspaceTab.RTK_STATE -> "RTK State"
                                AgentWorkspaceTab.AUDIT_LOG -> "Logs (${state.auditLogs.size})"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Tab Content
        when (state.activeTab) {
            AgentWorkspaceTab.CONSOLE -> {
                item {
                    AppCard {
                        SectionHeader(
                            title = "Autonomous Agent Prompt Console",
                            subtitle = "Enter an agent directive or RTK action command"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.promptInput,
                            onValueChange = { onIntent(AgentIntent.UpdatePrompt(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            placeholder = { Text("e.g. speak Audio test sequence completed or stop") }
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onIntent(AgentIntent.ExecutePrompt) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                enabled = !state.isExecuting
                            ) {
                                Text(
                                    text = if (state.isExecuting) "Dispatching..." else "Execute Agent Directive",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Quick Directives:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(quickCommands) { cmd ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                        .clickable { onIntent(AgentIntent.UpdatePrompt(cmd)) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cmd,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                    )
                                }
                            }
                        }
                    }
                }

                // Execution Result Card
                item {
                    state.lastResult?.let { result ->
                        AppCard(
                            borderColor = if (result.isSuccess) EmeraldGreen else RoseError
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LAST EXECUTION REPORT",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (result.isSuccess) EmeraldGreen else RoseError
                                )
                                StatusBadge(
                                    status = if (result.isSuccess) "SUCCESS" else "FAILED",
                                    isActive = result.isSuccess,
                                    color = if (result.isSuccess) EmeraldGreen else RoseError
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Command: ${result.commandText}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Output: ${result.summary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            AgentWorkspaceTab.TOOLS -> {
                item {
                    SectionHeader(
                        title = "Available Agent RTK Tools",
                        subtitle = "Autonomous capabilities callable by LLMs and scripts"
                    )
                }

                items(state.availableTools) { tool ->
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tool.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = CyberCyan
                                )
                                Text(
                                    text = tool.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Button(
                                onClick = {
                                    val defaultParams = when (tool.name) {
                                        "speak_text" -> mapOf("text" to "Agent tool test execution verified.")
                                        "query_state_slice" -> mapOf("sliceName" to "tts")
                                        "dispatch_rtk_action" -> mapOf("type" to "agent/custom_ping", "payload" to "ping")
                                        else -> emptyMap()
                                    }
                                    onIntent(AgentIntent.ExecuteToolDirectly(tool.name, defaultParams))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = "Run Tool",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (tool.parameters.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Parameters: " + tool.parameters.joinToString { "${it.name}: ${it.type}" },
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            AgentWorkspaceTab.RTK_STATE -> {
                item {
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader(
                                title = "Live RTK Store Slices",
                                subtitle = "Synchronized Redux state accessible to agents"
                            )
                            OutlinedButton(onClick = { onIntent(AgentIntent.RefreshRtkSnapshot) }) {
                                Text("Refresh")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (state.rtkStoreSnapshot.isEmpty()) {
                            Text(
                                text = "Store is currently initializing...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        state.rtkStoreSnapshot.forEach { (sliceKey, sliceVal) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Slice: $sliceKey",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CyberCyan
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = sliceVal,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AgentWorkspaceTab.AUDIT_LOG -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(
                            title = "Agent Audit & Trace Trail",
                            subtitle = "Chronological record of all agent RTK tool executions"
                        )
                        if (state.auditLogs.isNotEmpty()) {
                            OutlinedButton(onClick = { onIntent(AgentIntent.ClearAuditLogs) }) {
                                Text("Clear Logs")
                            }
                        }
                    }
                }

                if (state.auditLogs.isEmpty()) {
                    item {
                        Text(
                            text = "No audit events logged yet. Execute an agent command or tool to view audit trail.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(state.auditLogs) { record ->
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${record.toolName} • ${record.caller}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = "Duration: ${record.durationMs}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(
                                status = record.status.name,
                                isActive = record.status == AgentExecutionStatus.SUCCESS,
                                color = when (record.status) {
                                    AgentExecutionStatus.SUCCESS -> EmeraldGreen
                                    AgentExecutionStatus.FAILED -> RoseError
                                    AgentExecutionStatus.PENDING -> AmberWarning
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = record.outputSummary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
