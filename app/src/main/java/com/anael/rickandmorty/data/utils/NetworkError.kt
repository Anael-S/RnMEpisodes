package com.anael.rickandmorty.data.utils

sealed class NetworkError : Throwable() {
    object NoConnection : NetworkError()
    object Timeout : NetworkError()
    data class Http(val code: Int, val body: String?) : NetworkError()
    object Unknown : NetworkError()
}

fun Throwable.toNetworkError(): NetworkError = when (this) {
    is java.io.IOException -> NetworkError.NoConnection
    is java.net.SocketTimeoutException -> NetworkError.Timeout
    is retrofit2.HttpException -> NetworkError.Http(code(), response()?.errorBody()?.string())
    else -> NetworkError.Unknown
}
