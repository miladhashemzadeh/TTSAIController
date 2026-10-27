package com.vampyrelord.ttsaicontroller

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform