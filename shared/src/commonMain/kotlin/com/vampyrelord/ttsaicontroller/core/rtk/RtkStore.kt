package com.vampyrelord.ttsaicontroller.core.rtk

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

typealias RtkMiddleware = (action: RtkAction<*>, next: (RtkAction<*>) -> Unit) -> Unit

/**
 * Redux Toolkit (RTK) Store for Multiplatform state management.
 * Provides a single source of truth for all slices, middleware pipeline,
 * and reactive [StateFlow] observation for UI and autonomous agents.
 */
class RtkStore(
    slices: List<RtkSlice<*>> = emptyList(),
    private val middlewares: List<RtkMiddleware> = emptyList()
) {
    private val sliceMap = slices.associateBy { it.name }.toMutableMap()
    private val _state = MutableStateFlow<Map<String, Any?>>(
        slices.associate { it.name to it.initialState }
    )
    val state: StateFlow<Map<String, Any?>> = _state.asStateFlow()

    fun <S> registerSlice(slice: RtkSlice<S>) {
        sliceMap[slice.name] = slice
        _state.update { current ->
            if (!current.containsKey(slice.name)) {
                current + (slice.name to slice.initialState)
            } else {
                current
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <S> getSliceState(name: String): S? {
        return _state.value[name] as? S
    }

    fun dispatch(action: RtkAction<*>) {
        var chainIndex = 0
        fun next(act: RtkAction<*>) {
            if (chainIndex < middlewares.size) {
                val currentMiddleware = middlewares[chainIndex++]
                currentMiddleware(act) { nextAct -> next(nextAct) }
            } else {
                applyReducers(act)
            }
        }
        next(action)
    }

    @Suppress("UNCHECKED_CAST")
    private fun applyReducers(action: RtkAction<*>) {
        _state.update { currentState ->
            val updated = currentState.toMutableMap()
            sliceMap.forEach { (name, slice) ->
                val currentSliceState = updated[name]
                if (currentSliceState != null) {
                    val typedSlice = slice as RtkSlice<Any>
                    updated[name] = typedSlice.reduce(currentSliceState, action)
                }
            }
            updated.toMap()
        }
    }
}
