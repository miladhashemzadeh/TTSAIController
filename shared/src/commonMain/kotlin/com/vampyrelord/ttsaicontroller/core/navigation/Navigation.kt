package com.vampyrelord.ttsaicontroller.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Screen routes for the Touch Automation & STT Controller application.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val iconTag: String
) {
    data object TouchAutomation : Screen("touch_automation", "Touch", "touch")
    data object Dashboard : Screen("dashboard", "Dashboard", "dashboard")
    data object SttStudio : Screen("stt_studio", "Speech To Text", "stt")
    data object Settings : Screen("settings", "Settings", "settings")

    companion object {
        val all: List<Screen> get() = listOf(TouchAutomation, Dashboard, SttStudio, Settings)
    }
}

/**
 * Multiplatform reactive navigator.
 */
class Navigator(initialScreen: Screen = Screen.TouchAutomation) {
    private val backStack = mutableListOf<Screen>(initialScreen)
    private val _currentScreen = MutableStateFlow(initialScreen)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            backStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun goBack(): Boolean {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            _currentScreen.value = backStack.last()
            return true
        }
        return false
    }
}
