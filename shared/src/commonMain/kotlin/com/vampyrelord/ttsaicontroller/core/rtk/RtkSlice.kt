package com.vampyrelord.ttsaicontroller.core.rtk

/**
 * Redux Toolkit (RTK) Slice contract.
 * Encapsulates a distinct domain partition of state, its initial state,
 * and pure reducer function responding to [RtkAction].
 */
interface RtkSlice<S> {
    val name: String
    val initialState: S
    fun reduce(state: S, action: RtkAction<*>): S
}
