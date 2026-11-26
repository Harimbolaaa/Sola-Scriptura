package com.harimbola.solascriptura.core.appwrite

import com.harimbola.solascriptura.core.common.AppError
import io.appwrite.exceptions.AppwriteException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object AppwriteErrorMapper {

    fun map(throwable: Throwable): AppError = when (throwable) {
        is SocketTimeoutException -> AppError.Timeout
        is UnknownHostException, is IOException -> AppError.NoNetwork
        is AppwriteException -> mapAppwriteException(throwable)
        else -> AppError.Unknown(throwable.message)
    }

    private fun mapAppwriteException(e: AppwriteException): AppError {
        val code = e.code ?: 0
        val type = e.type ?: ""
        return when {
            code == 401 && type.contains("invalid_credentials", ignoreCase = true) ->
                AppError.InvalidCredentials
            code == 401 -> AppError.SessionExpired
            code == 403 -> AppError.PermissionDenied
            code == 404 -> AppError.ResourceNotFound
            code == 408 -> AppError.Timeout
            code >= 500 -> AppError.ServerError(code, e.message)
            else -> AppError.ServerError(code, e.message)
        }
    }
}
