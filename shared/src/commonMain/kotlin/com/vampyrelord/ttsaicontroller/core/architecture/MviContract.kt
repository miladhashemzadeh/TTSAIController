package com.vampyrelord.ttsaicontroller.core.architecture

/**
 * Marker interface for all MVI UI states.
 * States must be immutable data classes representing the complete screen state.
 */
interface MviState

/**
 * Marker interface for all MVI UI intents / user actions.
 * Intents are sealed interfaces/classes representing actions dispatched by the UI or an Agent.
 */
interface MviIntent

/**
 * Marker interface for all MVI one-off side effects.
 * Effects represent transient events like navigation, displaying a toast/snackbar, or audio triggers.
 */
interface MviEffect
