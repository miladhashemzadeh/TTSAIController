package com.vampyrelord.ttsaicontroller

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vampyrelord.ttsaicontroller.core.designsystem.AppBottomNav
import com.vampyrelord.ttsaicontroller.core.designsystem.AppTheme
import com.vampyrelord.ttsaicontroller.core.designsystem.CyberCyan
import com.vampyrelord.ttsaicontroller.core.di.AppContainer
import com.vampyrelord.ttsaicontroller.core.navigation.Screen
import com.vampyrelord.ttsaicontroller.features.agent.presentation.AgentScreen
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.DashboardScreen
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppThemeMode
import com.vampyrelord.ttsaicontroller.features.settings.presentation.SettingsScreen
import com.vampyrelord.ttsaicontroller.features.tts.presentation.TtsScreen

/**
 * Universal Root Application UI for Android, iOS, and JVM Desktop.
 * Shares 100% of the UI, state, and business logic with zero differences across targets.
 */
@Composable
fun App(
    container: AppContainer = remember { AppContainer() }
) {
    val settingsState by container.settingsViewModel.state.collectAsState()
    val isSystemDark = isSystemInDarkTheme()

    val isDark = when (settingsState.settings.themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    val currentScreen by container.navigator.currentScreen.collectAsState()

    AppTheme(darkTheme = isDark) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Unified Header Bar
                TopAppBarHeader(
                    currentScreen = currentScreen
                )

                // Active Feature Screen Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (currentScreen) {
                        Screen.Dashboard -> DashboardScreen(viewModel = container.dashboardViewModel)
                        Screen.TtsStudio -> TtsScreen(viewModel = container.ttsViewModel)
                        Screen.AgentWorkspace -> AgentScreen(viewModel = container.agentViewModel)
                        Screen.Settings -> SettingsScreen(viewModel = container.settingsViewModel)
                    }
                }

                // Shared Universal Bottom Navigation Bar
                AppBottomNav(
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        container.navigator.navigateTo(screen)
                    }
                )
            }
        }
    }
}

@Composable
private fun TopAppBarHeader(
    currentScreen: Screen
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TTS-AI",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = CyberCyan
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "| ${currentScreen.title}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
