package com.harimbola.solascriptura.core.appwrite

import com.harimbola.solascriptura.core.common.AppError
import com.harimbola.solascriptura.core.common.AppResult
import com.harimbola.solascriptura.domain.model.AppUser
import io.appwrite.ID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppwriteAccountDataSource @Inject constructor(
    private val clientProvider: AppwriteClientProvider
) {
    suspend fun getCurrentUser(): AppResult<AppUser?> {
        if (!clientProvider.isConfigured()) {
            return AppResult.Failure(AppError.AppwriteUnconfigured)
        }
        return try {
            val user = clientProvider.account.get()
            AppResult.Success(
                AppUser(
                    userId = user.id,
                    email = user.email,
                    name = user.name
                )
            )
        } catch (t: Throwable) {
            val mapped = AppwriteErrorMapper.map(t)
            if (mapped is AppError.SessionExpired) {
                AppResult.Success(null)
            } else {
                AppResult.Failure(mapped)
            }
        }
    }

    suspend fun createAccount(name: String, email: String, password: String): AppResult<AppUser> {
        if (!clientProvider.isConfigured()) {
            return AppResult.Failure(AppError.AppwriteUnconfigured)
        }
        return try {
            val created = clientProvider.account.create(
                userId = ID.unique(),
                email = email,
                password = password,
                name = name
            )
            clientProvider.account.createEmailPasswordSession(email = email, password = password)
            AppResult.Success(
                AppUser(
                    userId = created.id,
                    email = created.email,
                    name = created.name
                )
            )
        } catch (t: Throwable) {
            AppResult.Failure(AppwriteErrorMapper.map(t))
        }
    }

    suspend fun signIn(email: String, password: String): AppResult<AppUser> {
        if (!clientProvider.isConfigured()) {
            return AppResult.Failure(AppError.AppwriteUnconfigured)
        }
        return try {
            clientProvider.account.createEmailPasswordSession(email = email, password = password)
            val user = clientProvider.account.get()
            AppResult.Success(
                AppUser(
                    userId = user.id,
                    email = user.email,
                    name = user.name
                )
            )
        } catch (t: Throwable) {
            AppResult.Failure(AppwriteErrorMapper.map(t))
        }
    }

    suspend fun sendRecovery(email: String): AppResult<Unit> {
        if (!clientProvider.isConfigured()) {
            return AppResult.Failure(AppError.AppwriteUnconfigured)
        }
        return try {
            clientProvider.account.createRecovery(
                email = email,
                url = "https://solascriptura.app/recovery"
            )
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Failure(AppwriteErrorMapper.map(t))
        }
    }

    suspend fun signOut(): AppResult<Unit> {
        if (!clientProvider.isConfigured()) {
            return AppResult.Failure(AppError.AppwriteUnconfigured)
        }
        return try {
            clientProvider.account.deleteSession(sessionId = "current")
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Failure(AppwriteErrorMapper.map(t))
        }
    }
}
