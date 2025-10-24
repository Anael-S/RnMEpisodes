package com.anael.rickandmorty.data.utils

/**
 * Small helpers class to handle network error
 */
sealed class NetworkError(cause: Throwable? = null) : Throwable(cause) {
    object NoConnection : NetworkError()
    object Timeout : NetworkError()
    data class Http(val code: Int, val body: String?) : NetworkError()
    data class Unknown(val original: Throwable? = null) : NetworkError(original)
}

fun Throwable.toNetworkError(): NetworkError = when (this) {
    is java.net.SocketTimeoutException -> NetworkError.Timeout
    is java.io.IOException -> NetworkError.NoConnection
    is retrofit2.HttpException -> NetworkError.Http(code(), response()?.errorBody()?.string())
    else -> NetworkError.Unknown(this)
}