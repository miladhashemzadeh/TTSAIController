package com.vampyrelord.ttsaicontroller.features.stt.domain.model

enum class AudioEncoding {
    PCM_16BIT,
    WAV,
    MP3,
    AAC,
    FLAC
}

enum class AudioInputSource {
    MICROPHONE,
    AUDIO_FILE,
    STREAM_BUFFER
}

enum class TranscriptionStatus {
    IDLE,
    LISTENING,
    RECORDING,
    PROCESSING,
    COMPLETED,
    ERROR
}

data class AudioConfig(
    val sampleRate: Int = 16000,
    val channels: Int = 1,
    val bitDepth: Int = 16,
    val language: String? = "en",
    val encoding: AudioEncoding = AudioEncoding.PCM_16BIT
)

data class TranscriptionSegment(
    val id: String,
    val text: String,
    val startOffsetMs: Long,
    val endOffsetMs: Long,
    val confidence: Float = 1.0f,
    val isFinal: Boolean = true
)

data class TranscriptionResult(
    val id: String,
    val fullText: String,
    val segments: List<TranscriptionSegment> = emptyList(),
    val durationMs: Long = 0L,
    val languageDetected: String? = "en",
    val confidence: Float = 0.95f,
    val aiModelInfo: String? = "AI Speech Model (Pending Integration)"
)

data class SttEngineCapability(
    val engineName: String = "AI Speech-To-Text (Pending)",
    val isSupported: Boolean = false,
    val supportsLiveStreaming: Boolean = true,
    val supportedLanguages: List<String> = listOf("en", "es", "fr", "de", "auto"),
    val aiModelRequired: Boolean = true,
    val statusDescription: String = "AI speech-to-text model pending integration (e.g. Whisper, Gemini Audio, Sherpa-ONNX)."
)
