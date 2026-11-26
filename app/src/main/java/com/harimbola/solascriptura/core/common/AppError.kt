package com.harimbola.solascriptura.core.common

import androidx.annotation.StringRes
import com.example.R

sealed class AppError(
    @param:StringRes val messageResId: Int,
    val detailMessage: String? = null
) {
    data object NoNetwork : AppError(R.string.error_no_network)
    data object SessionExpired : AppError(R.string.error_generic, "Session expired")
    data object InvalidCredentials : AppError(R.string.error_generic, "Invalid credentials")
    data object AppwriteUnconfigured : AppError(R.string.error_appwrite_unconfigured)
    data object ResourceNotFound : AppError(R.string.error_generic, "Resource not found")
    data class CorruptedJson(val reason: String) : AppError(R.string.error_invalid_json, reason)
    data class UnsupportedSchema(val version: Int) : AppError(R.string.error_invalid_json, "Unsupported schema v$version")
    data object PermissionDenied : AppError(R.string.error_generic, "Permission denied")
    data object InsufficientStorage : AppError(R.string.error_generic, "Insufficient storage")
    data object Timeout : AppError(R.string.error_no_network, "Request timeout")
    data class ServerError(val code: Int, val reason: String? = null) : AppError(R.string.error_generic, reason)
    data class Unknown(val reason: String? = null) : AppError(R.string.error_generic, reason)
}

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}
