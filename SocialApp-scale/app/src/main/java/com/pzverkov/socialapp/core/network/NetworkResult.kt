package com.pzverkov.socialapp.core.network

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Error(val type: ErrorType) : NetworkResult<Nothing>
}

enum class ErrorType {
    NETWORK,
    UNKNOWN,
}
