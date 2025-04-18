package com.android.chatappcompose.auth.domain.resource

sealed class ResultState<out T> {
    object Loading : ResultState<Nothing>()
    data class Progress(val progress: Float) : ResultState<Nothing>()
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Failure(val error: Throwable) : ResultState<Nothing>()
}