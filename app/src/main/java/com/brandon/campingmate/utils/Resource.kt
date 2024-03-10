package com.brandon.campingmate.utils

// Retrofit 확장성
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Empty : Resource<Nothing>()
}