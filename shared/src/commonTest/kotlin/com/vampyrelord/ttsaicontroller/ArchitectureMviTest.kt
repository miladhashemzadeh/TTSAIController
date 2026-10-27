package com.vampyrelord.ttsaicontroller

import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.core.di.AppContainer
import com.vampyrelord.ttsaicontroller.core.navigation.Screen
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppThemeMode
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.AudioConfig
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
    fun testSpeechToTextUseCaseAndRepositoryFlow() = runTest {
        val container = AppContainer()
        val sttRepo = container.speechToTextRepository
        val startUseCase = container.startSpeechToTextUseCase
        val stopUseCase = container.stopSpeechToTextUseCase
        val capabilityUseCase = container.getSttCapabilityUseCase

        // Check initial state
        assertEquals(TranscriptionStatus.IDLE, sttRepo.transcriptionStatus.value)

        // Check capability contract
        val cap = capabilityUseCase()
        assertTrue(cap.aiModelRequired)
        assertTrue(cap.supportsLiveStreaming)

        // Start listening
        val startRes = startUseCase(AudioConfig(language = "en", sampleRate = 16000))
        assertTrue(startRes is AppResult.Success)
        assertEquals(TranscriptionStatus.LISTENING, sttRepo.transcriptionStatus.value)

        // Stop listening and transcribe
        val stopRes = stopUseCase()
        assertTrue(stopRes is AppResult.Success)
        assertEquals(TranscriptionStatus.COMPLETED, sttRepo.transcriptionStatus.value)

        val transcription = stopRes.data
        assertNotNull(transcription)
        assertTrue(transcription.fullText.isNotEmpty())
        assertEquals(true, transcription.aiModelInfo?.contains("Pending AI Model"))
    }

    @Test
    fun testNavigatorFlow() {
        val container = AppContainer()
        val navigator = container.navigator

        assertEquals(Screen.TouchAutomation, navigator.currentScreen.value)

        navigator.navigateTo(Screen.SttStudio)
        assertEquals(Screen.SttStudio, navigator.currentScreen.value)

        val popped = navigator.goBack()
        assertEquals(true, popped)
        assertEquals(Screen.TouchAutomation, navigator.currentScreen.value)
    }
}
