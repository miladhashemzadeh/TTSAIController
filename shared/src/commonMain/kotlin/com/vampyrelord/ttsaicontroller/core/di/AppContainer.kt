package com.vampyrelord.ttsaicontroller.core.di

import com.vampyrelord.ttsaicontroller.core.navigation.Navigator
import com.vampyrelord.ttsaicontroller.core.rtk.AgentActionDispatcher
import com.vampyrelord.ttsaicontroller.core.rtk.AgentAuditLog
import com.vampyrelord.ttsaicontroller.core.rtk.AgentRtkSlice
import com.vampyrelord.ttsaicontroller.core.rtk.AgentToolRegistry
import com.vampyrelord.ttsaicontroller.core.rtk.RtkStore
import com.vampyrelord.ttsaicontroller.core.rtk.TouchRtkSlice
import com.vampyrelord.ttsaicontroller.core.rtk.TtsRtkSlice
import com.vampyrelord.ttsaicontroller.features.agent.data.datasource.DispatchRtkActionTool
import com.vampyrelord.ttsaicontroller.features.agent.data.datasource.ExecuteTouchRoutineTool
import com.vampyrelord.ttsaicontroller.features.agent.data.datasource.GetPlaybackStatusTool
import com.vampyrelord.ttsaicontroller.features.agent.data.datasource.GetTouchStatusTool
import com.vampyrelord.ttsaicontroller.features.agent.data.datasource.ListVoicesTool
import com.vampyrelord.ttsaicontroller.features.agent.data.datasource.PauseSpeechTool
import com.vampyrelord.ttsaicontroller.features.agent.data.datasource.QueryStateSliceTool
import com.vampyrelord.ttsaicontroller.features.agent.data.datasource.ResumeSpeechTool
import com.vampyrelord.ttsaicontroller.features.agent.data.datasource.SpeakTextTool
import com.vampyrelord.ttsaicontroller.features.agent.data.datasource.StopSpeechTool
import com.vampyrelord.ttsaicontroller.features.agent.data.datasource.StopTouchAutomationTool
import com.vampyrelord.ttsaicontroller.features.agent.data.repository.AgentRepositoryImpl
import com.vampyrelord.ttsaicontroller.features.agent.domain.repository.AgentRepository
import com.vampyrelord.ttsaicontroller.features.agent.domain.usecase.ClearAgentLogsUseCase
import com.vampyrelord.ttsaicontroller.features.agent.domain.usecase.ExecuteAgentCommandUseCase
import com.vampyrelord.ttsaicontroller.features.agent.domain.usecase.ExecuteDirectToolUseCase
import com.vampyrelord.ttsaicontroller.features.agent.domain.usecase.GetAgentToolsUseCase
import com.vampyrelord.ttsaicontroller.features.agent.presentation.AgentViewModel
import com.vampyrelord.ttsaicontroller.features.dashboard.domain.usecase.GetDashboardMetricsUseCase
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.DashboardViewModel
import com.vampyrelord.ttsaicontroller.features.settings.data.datasource.InMemorySettingsDataSource
import com.vampyrelord.ttsaicontroller.features.settings.data.repository.SettingsRepositoryImpl
import com.vampyrelord.ttsaicontroller.features.settings.domain.repository.SettingsRepository
import com.vampyrelord.ttsaicontroller.features.settings.domain.usecase.GetSettingsUseCase
import com.vampyrelord.ttsaicontroller.features.settings.domain.usecase.ResetSettingsUseCase
import com.vampyrelord.ttsaicontroller.features.settings.domain.usecase.UpdateSettingsUseCase
import com.vampyrelord.ttsaicontroller.features.settings.presentation.SettingsViewModel
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
import com.vampyrelord.ttsaicontroller.features.tts.data.datasource.InMemoryTtsQueueDataSource
import com.vampyrelord.ttsaicontroller.features.tts.data.datasource.TtsEngineDataSource
import com.vampyrelord.ttsaicontroller.features.tts.data.repository.TtsRepositoryImpl
import com.vampyrelord.ttsaicontroller.features.tts.domain.repository.TtsRepository
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.ClearQueueUseCase
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.GetVoicesUseCase
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.PauseSpeechUseCase
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.ResumeSpeechUseCase
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.SpeakTextUseCase
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.StopSpeechUseCase
import com.vampyrelord.ttsaicontroller.features.tts.presentation.TtsViewModel

/**
 * Clean dependency injection container and service locator.
 * Provides unified lifecycle management across Android, JVM Desktop, and iOS.
 */
class AppContainer {

    // 1. Core RTK & Navigation
    val navigator = Navigator()
    val rtkStore = RtkStore(
        slices = listOf(TtsRtkSlice(), AgentRtkSlice(), TouchRtkSlice())
    )
    val agentAuditLog = AgentAuditLog()
    val agentToolRegistry = AgentToolRegistry()
    val agentActionDispatcher = AgentActionDispatcher(agentToolRegistry, rtkStore, agentAuditLog)

    // 2. Data Sources & Platform Controllers
    val ttsEngineDataSource = TtsEngineDataSource()
    val ttsQueueDataSource = InMemoryTtsQueueDataSource()
    val settingsDataSource = InMemorySettingsDataSource()
    val platformTouchController: TouchController = createPlatformTouchController()

    // 3. Repositories
    val ttsRepository: TtsRepository = TtsRepositoryImpl(
        engineDataSource = ttsEngineDataSource,
        queueDataSource = ttsQueueDataSource,
        rtkStore = rtkStore
    )
    val agentRepository: AgentRepository = AgentRepositoryImpl(
        toolRegistry = agentToolRegistry,
        dispatcher = agentActionDispatcher,
        auditLog = agentAuditLog
    )
    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(
        dataSource = settingsDataSource,
        rtkStore = rtkStore
    )
    val touchConfigRepository: TouchConfigRepository = InMemoryTouchConfigRepository(
        rtkStore = rtkStore
    )

    // 4. Use Cases
    val speakTextUseCase = SpeakTextUseCase(ttsRepository)
    val stopSpeechUseCase = StopSpeechUseCase(ttsRepository)
    val pauseSpeechUseCase = PauseSpeechUseCase(ttsRepository)
    val resumeSpeechUseCase = ResumeSpeechUseCase(ttsRepository)
    val getVoicesUseCase = GetVoicesUseCase(ttsRepository)
    val clearQueueUseCase = ClearQueueUseCase(ttsRepository)

    val executeAgentCommandUseCase = ExecuteAgentCommandUseCase(agentRepository)
    val executeDirectToolUseCase = ExecuteDirectToolUseCase(agentRepository)
    val getAgentToolsUseCase = GetAgentToolsUseCase(agentRepository)
    val clearAgentLogsUseCase = ClearAgentLogsUseCase(agentRepository)

    val getSettingsUseCase = GetSettingsUseCase(settingsRepository)
    val updateSettingsUseCase = UpdateSettingsUseCase(settingsRepository)
    val resetSettingsUseCase = ResetSettingsUseCase(settingsRepository)

    val getDashboardMetricsUseCase = GetDashboardMetricsUseCase(
        ttsRepository = ttsRepository,
        agentRepository = agentRepository,
        settingsRepository = settingsRepository
    )

    val executeTouchSequenceUseCase = ExecuteTouchSequenceUseCase(platformTouchController)
    val cancelTouchSequenceUseCase = CancelTouchSequenceUseCase(platformTouchController)
    val generateTouchPositionsUseCase = GenerateTouchPositionsUseCase()
    val saveTouchConfigUseCase = SaveTouchConfigUseCase(touchConfigRepository)
    val deleteTouchConfigUseCase = DeleteTouchConfigUseCase(touchConfigRepository)
    val updateRushwulfSettingsUseCase = UpdateRushwulfSettingsUseCase(touchConfigRepository)

    init {
        // Register default Agent RTK Tools
        agentToolRegistry.registerAll(
            listOf(
                SpeakTextTool(ttsRepository),
                StopSpeechTool(ttsRepository),
                PauseSpeechTool(ttsRepository),
                ResumeSpeechTool(ttsRepository),
                GetPlaybackStatusTool(ttsRepository),
                ListVoicesTool(ttsRepository),
                QueryStateSliceTool(rtkStore),
                DispatchRtkActionTool(rtkStore),
                ExecuteTouchRoutineTool(platformTouchController, touchConfigRepository),
                StopTouchAutomationTool(platformTouchController),
                GetTouchStatusTool(platformTouchController)
            )
        )
    }

    // 5. ViewModels
    val dashboardViewModel: DashboardViewModel by lazy {
        DashboardViewModel(
            getDashboardMetricsUseCase = getDashboardMetricsUseCase,
            speakTextUseCase = speakTextUseCase,
            stopSpeechUseCase = stopSpeechUseCase,
            ttsRepository = ttsRepository,
            agentRepository = agentRepository,
            navigator = navigator
        )
    }

    val ttsViewModel: TtsViewModel by lazy {
        TtsViewModel(
            ttsRepository = ttsRepository,
            speakTextUseCase = speakTextUseCase,
            stopSpeechUseCase = stopSpeechUseCase,
            pauseSpeechUseCase = pauseSpeechUseCase,
            resumeSpeechUseCase = resumeSpeechUseCase,
            getVoicesUseCase = getVoicesUseCase,
            clearQueueUseCase = clearQueueUseCase
        )
    }

    val agentViewModel: AgentViewModel by lazy {
        AgentViewModel(
            agentRepository = agentRepository,
            executeCommandUseCase = executeAgentCommandUseCase,
            executeDirectToolUseCase = executeDirectToolUseCase,
            getAgentToolsUseCase = getAgentToolsUseCase,
            clearLogsUseCase = clearAgentLogsUseCase,
            rtkStore = rtkStore
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
