package com.pzverkov.socialapp.core.domain

import com.pzverkov.socialapp.core.model.ErrorType

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Error(val type: ErrorType) : NetworkResult<Nothing>
}
