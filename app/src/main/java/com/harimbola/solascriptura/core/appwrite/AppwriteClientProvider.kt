package com.harimbola.solascriptura.core.appwrite

import android.content.Context
import com.example.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Storage
import io.appwrite.services.TablesDB
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppwriteClientProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val endpoint: String = BuildConfig.APPWRITE_ENDPOINT
    val projectId: String = BuildConfig.APPWRITE_PROJECT_ID
    val databaseId: String = BuildConfig.APPWRITE_DATABASE_ID
    val catalogTableId: String = BuildConfig.APPWRITE_CATALOG_TABLE_ID
    val progressTableId: String = BuildConfig.APPWRITE_PROGRESS_TABLE_ID
    val studyBucketId: String = BuildConfig.APPWRITE_STUDY_BUCKET_ID

    fun isConfigured(): Boolean =
        projectId.isNotBlank() &&
            !projectId.equals("YOUR_PROJECT_ID", ignoreCase = true) &&
            endpoint.startsWith("https://")

    val client: Client by lazy {
        Client(context)
            .setEndpoint(endpoint)
            .setProject(projectId.ifBlank { "unconfigured" })
    }

    val account: Account by lazy { Account(client) }
    val tablesDB: TablesDB by lazy { TablesDB(client) }
    val storage: Storage by lazy { Storage(client) }
}
