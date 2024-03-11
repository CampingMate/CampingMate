package com.brandon.campingmate.utils

sealed class UiState<out T> {
    data class Success<T>(val data: T) : UiState<T>()
    object Empty : UiState<Nothing>()
}