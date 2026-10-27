package com.vampyrelord.ttsaicontroller.features.dashboard.presentation.mvi

import com.vampyrelord.ttsaicontroller.core.architecture.MviEffect
import com.vampyrelord.ttsaicontroller.core.architecture.MviIntent
import com.vampyrelord.ttsaicontroller.core.architecture.MviState
import com.vampyrelord.ttsaicontroller.core.navigation.Screen
import com.vampyrelord.ttsaicontroller.features.dashboard.domain.model.DashboardMetrics
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionResult

data class DashboardState(
    val metrics: DashboardMetrics = DashboardMetrics(),
    val recentTranscriptions: List<TranscriptionResult> = emptyList(),
    val isRefreshing: Boolean = false
) : MviState

sealed interface DashboardIntent : MviIntent {
    data object Refresh : DashboardIntent
    data object StartListening : DashboardIntent
    data object StopListening : DashboardIntent
    data class NavigateTo(val screen: Screen) : DashboardIntent
}

sealed interface DashboardEffect : MviEffect {
    data class ShowToast(val message: String) : DashboardEffect
    data class Navigate(val screen: Screen) : DashboardEffect
}
