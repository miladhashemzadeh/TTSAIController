package com.vampyrelord.ttsaicontroller

import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.core.di.AppContainer
import com.vampyrelord.ttsaicontroller.core.navigation.Screen
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppThemeMode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArchitectureMviTest {

    @Test
    fun testSettingsUseCaseAndRepositoryFlow() = runTest {
        val container = AppContainer()
        val repo = container.settingsRepository
        val updateUseCase = container.updateSettingsUseCase

        assertEquals(AppThemeMode.SYSTEM, repo.settings.value.themeMode)

        val res = updateUseCase { it.copy(themeMode = AppThemeMode.DARK, autoPlayOnQueue = false) }
        assertTrue(res is AppResult.Success)

        assertEquals(AppThemeMode.DARK, repo.settings.value.themeMode)
        assertEquals(false, repo.settings.value.autoPlayOnQueue)
    }

    @Test
    fun testTtsUseCaseAndRepositoryFlow() = runTest {
        val container = AppContainer()
        val ttsRepo = container.ttsRepository
        val speakUseCase = container.speakTextUseCase
        val stopUseCase = container.stopSpeechUseCase

        val voices = ttsRepo.getAvailableVoices()
        assertTrue(voices.isNotEmpty())

        val speakRes = speakUseCase(
            text = "Clean architecture domain test",
            voice = voices.first(),
            pitch = 1.0f,
            rate = 1.0f
        )
        assertTrue(speakRes is AppResult.Success)
        assertEquals("Clean architecture domain test", speakRes.data.text)

        // Wait brief moment for asynchronous engine background coroutine to start
        var waited = 0
        while (!ttsRepo.engineStatus.value.isSpeaking && waited < 40) {
            Thread.sleep(25)
            waited++
        }
        assertTrue(ttsRepo.engineStatus.value.isSpeaking)

        val stopRes = stopUseCase()
        assertTrue(stopRes is AppResult.Success)
        var stopWaited = 0
        while (ttsRepo.engineStatus.value.isSpeaking && stopWaited < 40) {
            Thread.sleep(25)
            stopWaited++
        }
        assertEquals(false, ttsRepo.engineStatus.value.isSpeaking)
    }

    @Test
    fun testNavigatorFlow() {
        val container = AppContainer()
        val navigator = container.navigator

        assertEquals(Screen.TouchAutomation, navigator.currentScreen.value)

        navigator.navigateTo(Screen.TtsStudio)
        assertEquals(Screen.TtsStudio, navigator.currentScreen.value)

        val popped = navigator.goBack()
        assertEquals(true, popped)
        assertEquals(Screen.TouchAutomation, navigator.currentScreen.value)
    }
}
