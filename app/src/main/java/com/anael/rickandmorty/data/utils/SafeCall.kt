package com.anael.rickandmorty.data.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

suspend inline fun <T> safeCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    crossinline block: suspend () -> T
): Result<T> = withContext(dispatcher) {
    try { Result.success(block()) }
    catch (e: HttpException) { Result.failure(e) }
    catch (e: IOException)   { Result.failure(e) }
    catch (e: Exception)     { Result.failure(e) }
}
