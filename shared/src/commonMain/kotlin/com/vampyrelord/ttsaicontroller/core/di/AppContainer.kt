package com.vampyrelord.ttsaicontroller.core.di

import com.vampyrelord.ttsaicontroller.core.navigation.Navigator
import com.vampyrelord.ttsaicontroller.features.dashboard.domain.usecase.GetDashboardMetricsUseCase
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.DashboardViewModel
import com.vampyrelord.ttsaicontroller.features.settings.data.datasource.InMemorySettingsDataSource
import com.vampyrelord.ttsaicontroller.features.settings.data.repository.SettingsRepositoryImpl
import com.vampyrelord.ttsaicontroller.features.settings.domain.repository.SettingsRepository
import com.vampyrelord.ttsaicontroller.features.settings.domain.usecase.GetSettingsUseCase
import com.vampyrelord.ttsaicontroller.features.settings.domain.usecase.ResetSettingsUseCase
import com.vampyrelord.ttsaicontroller.features.settings.domain.usecase.UpdateSettingsUseCase
import com.vampyrelord.ttsaicontroller.features.settings.presentation.SettingsViewModel
import com.vampyrelord.ttsaicontroller.features.stt.data.repository.PendingAiSpeechToTextRepository
import com.vampyrelord.ttsaicontroller.features.stt.domain.repository.SpeechToTextRepository
import com.vampyrelord.ttsaicontroller.features.stt.domain.usecase.CancelSpeechToTextUseCase
import com.vampyrelord.ttsaicontroller.features.stt.domain.usecase.GetSttCapabilityUseCase
import com.vampyrelord.ttsaicontroller.features.stt.domain.usecase.StartSpeechToTextUseCase
import com.vampyrelord.ttsaicontroller.features.stt.domain.usecase.StopSpeechToTextUseCase
import com.vampyrelord.ttsaicontroller.features.stt.domain.usecase.TranscribeAudioUseCase
import com.vampyrelord.ttsaicontroller.features.stt.domain.usecase.TranscribeFileUseCase
import com.vampyrelord.ttsaicontroller.features.stt.presentation.SttViewModel
import com.vampyrelord.ttsaicontroller.features.touch.data.InMemoryTouchConfigRepository
import com.vampyrelord.ttsaicontroller.features.touch.data.createPlatformTouchController
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchConfigRepository
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchController
import com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.CancelTouchSequenceUseCase
import com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.DeleteTouchConfigUseCase
import com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.ExecuteTouchSequenceUseCase
import com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.GenerateTouchPositionsUseCase
import com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.SaveTouchConfigUseCase
import com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.UpdateRushwulfSettingsUseCase
import com.vampyrelord.ttsaicontroller.features.touch.presentation.TouchAutomationViewModel

/**
 * Clean dependency injection container and service locator.
 * Provides unified lifecycle management across Android, JVM Desktop, and iOS.
 */
class AppContainer {

    // 1. Navigation
    val navigator = Navigator()

    // 2. Data Sources & Platform Controllers
    val settingsDataSource = InMemorySettingsDataSource()
    val platformTouchController: TouchController = createPlatformTouchController()

    // 3. Repositories
    val speechToTextRepository: SpeechToTextRepository = PendingAiSpeechToTextRepository()
    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(
        dataSource = settingsDataSource
    )
    val touchConfigRepository: TouchConfigRepository = InMemoryTouchConfigRepository()

    // 4. Use Cases
    val startSpeechToTextUseCase = StartSpeechToTextUseCase(speechToTextRepository)
    val stopSpeechToTextUseCase = StopSpeechToTextUseCase(speechToTextRepository)
    val cancelSpeechToTextUseCase = CancelSpeechToTextUseCase(speechToTextRepository)
    val transcribeAudioUseCase = TranscribeAudioUseCase(speechToTextRepository)
    val transcribeFileUseCase = TranscribeFileUseCase(speechToTextRepository)
    val getSttCapabilityUseCase = GetSttCapabilityUseCase(speechToTextRepository)

    val getSettingsUseCase = GetSettingsUseCase(settingsRepository)
    val updateSettingsUseCase = UpdateSettingsUseCase(settingsRepository)
    val resetSettingsUseCase = ResetSettingsUseCase(settingsRepository)

    val getDashboardMetricsUseCase = GetDashboardMetricsUseCase(
        speechToTextRepository = speechToTextRepository,
        touchConfigRepository = touchConfigRepository,
        touchController = platformTouchController
    )

    val executeTouchSequenceUseCase = ExecuteTouchSequenceUseCase(platformTouchController)
    val cancelTouchSequenceUseCase = CancelTouchSequenceUseCase(platformTouchController)
    val generateTouchPositionsUseCase = GenerateTouchPositionsUseCase()
    val saveTouchConfigUseCase = SaveTouchConfigUseCase(touchConfigRepository)
    val deleteTouchConfigUseCase = DeleteTouchConfigUseCase(touchConfigRepository)
    val updateRushwulfSettingsUseCase = UpdateRushwulfSettingsUseCase(touchConfigRepository)

    // 5. ViewModels
    val dashboardViewModel: DashboardViewModel by lazy {
        DashboardViewModel(
            getDashboardMetricsUseCase = getDashboardMetricsUseCase,
            speechToTextRepository = speechToTextRepository,
            touchConfigRepository = touchConfigRepository,
            touchController = platformTouchController,
            navigator = navigator
        )
    }

    val sttViewModel: SttViewModel by lazy {
        SttViewModel(
            repository = speechToTextRepository,
            startSpeechToTextUseCase = startSpeechToTextUseCase,
            stopSpeechToTextUseCase = stopSpeechToTextUseCase,
            cancelSpeechToTextUseCase = cancelSpeechToTextUseCase,
            getSttCapabilityUseCase = getSttCapabilityUseCase
        )
    }

    val settingsViewModel: SettingsViewModel by lazy {
        SettingsViewModel(
            settingsRepository = settingsRepository,
            updateSettingsUseCase = updateSettingsUseCase,
            resetSettingsUseCase = resetSettingsUseCase
        )
    }

    val touchAutomationViewModel: TouchAutomationViewModel by lazy {
        TouchAutomationViewModel(
            repository = touchConfigRepository,
            controller = platformTouchController,
            executeSequenceUseCase = executeTouchSequenceUseCase,
            cancelSequenceUseCase = cancelTouchSequenceUseCase,
            generatePositionsUseCase = generateTouchPositionsUseCase,
            saveConfigUseCase = saveTouchConfigUseCase,
            deleteConfigUseCase = deleteTouchConfigUseCase,
            updateRushwulfSettingsUseCase = updateRushwulfSettingsUseCase
        )
    }
}
