package com.osnordev.abaco.util

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val exception: Throwable, val message: String) : Result<Nothing>()
}
