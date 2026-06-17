package com.pzverkov.socialapp.core.store

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Observes state transitions and events flowing through a [Store]. The single point where
 * cross-cutting concerns (logging, analytics, crash breadcrumbs, test recording) plug in
 * without each ViewModel knowing about them. Callbacks receive [Any] because a [Store] is
 * generic; an interceptor inspects the runtime type it cares about.
 */
interface StoreInterceptor {
    fun onState(old: Any?, new: Any?)
    fun onEvent(event: Any?)
}

class Store<S, E>(
    initialState: S,
    private val interceptors: List<StoreInterceptor> = emptyList(),
) {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _events = MutableSharedFlow<E>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<E> = _events.asSharedFlow()

    fun updateState(reducer: (S) -> S) {
        val old = _state.value
        _state.update(reducer)
        val new = _state.value
        if (old !== new && interceptors.isNotEmpty()) {
            interceptors.forEach { it.onState(old, new) }
        }
    }

    fun emitEvent(event: E) {
        _events.tryEmit(event)
        if (interceptors.isNotEmpty()) {
            interceptors.forEach { it.onEvent(event) }
        }
    }
}
