package com.vampyrelord.ttsaicontroller.features.settings.domain.usecase

import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppSettings
import com.vampyrelord.ttsaicontroller.features.settings.domain.repository.SettingsRepository

class GetSettingsUseCase(private val repository: SettingsRepository) {
    operator fun invoke() = repository.settings
}

class UpdateSettingsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(transform: (AppSettings) -> AppSettings): AppResult<Unit> {
        return try {
            repository.updateSettings(transform)
            AppResult.Success(Unit)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}

class ResetSettingsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): AppResult<Unit> {
        return try {
            repository.resetDefaults()
            AppResult.Success(Unit)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}
