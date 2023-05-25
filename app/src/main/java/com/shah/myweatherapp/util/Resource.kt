package com.shah.myweatherapp.util

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T?): Resource<T>(data)
    class Error<T>(message: String, data: T? = null): Resource<T>(data, message)
    class Loading<T>(val isLoading: Boolean = true): Resource<T>(null)
}

fun Resource<*>.isSuccess(): Boolean {
    return this is Resource.Success
}

fun Resource<*>.isError(): Boolean {
    return this is Resource.Error
}

fun Resource<*>.isLoading(): Boolean {
    return this is Resource.Loading
}
