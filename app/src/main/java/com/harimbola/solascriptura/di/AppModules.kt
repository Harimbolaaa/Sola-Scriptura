package com.harimbola.solascriptura.di

import android.content.Context
import androidx.room.Room
import com.harimbola.solascriptura.core.common.AppConstants
import com.harimbola.solascriptura.core.database.SolaScripturaDatabase
import com.harimbola.solascriptura.core.database.dao.CategoryDao
import com.harimbola.solascriptura.core.database.dao.FavoriteDao
import com.harimbola.solascriptura.core.database.dao.ProgressDao
import com.harimbola.solascriptura.core.database.dao.ReadingHistoryDao
import com.harimbola.solascriptura.core.database.dao.StudyDao
import com.harimbola.solascriptura.core.database.dao.SyncQueueDao
import com.harimbola.solascriptura.core.database.dao.UserNoteDao
import com.harimbola.solascriptura.core.datastore.PreferencesDataStore
import com.harimbola.solascriptura.core.datastore.solaDataStore
import com.harimbola.solascriptura.core.engine.DialogueEngine
import com.harimbola.solascriptura.core.engine.DialogueProgressManager
import com.harimbola.solascriptura.core.json.AssetStudyLoader
import com.harimbola.solascriptura.data.repository.AuthRepositoryImpl
import com.harimbola.solascriptura.data.repository.NotebookRepositoryImpl
import com.harimbola.solascriptura.data.repository.PreferencesRepositoryImpl
import com.harimbola.solascriptura.data.repository.ProgressRepositoryImpl
import com.harimbola.solascriptura.data.repository.StudyRepositoryImpl
import com.harimbola.solascriptura.data.repository.SyncRepositoryImpl
import com.harimbola.solascriptura.domain.repository.AuthRepository
import com.harimbola.solascriptura.domain.repository.NotebookRepository
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.ProgressRepository
import com.harimbola.solascriptura.domain.repository.StudyRepository
import com.harimbola.solascriptura.domain.repository.SyncRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SolaScripturaDatabase =
        Room.databaseBuilder(
            context,
            SolaScripturaDatabase::class.java,
            AppConstants.DATABASE_NAME
        )
            .addMigrations(SolaScripturaDatabase.MIGRATION_1_2)
            .build()

    @Provides fun provideStudyDao(db: SolaScripturaDatabase): StudyDao = db.studyDao()
    @Provides fun provideCategoryDao(db: SolaScripturaDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideProgressDao(db: SolaScripturaDatabase): ProgressDao = db.progressDao()
    @Provides fun provideUserNoteDao(db: SolaScripturaDatabase): UserNoteDao = db.userNoteDao()
    @Provides fun provideFavoriteDao(db: SolaScripturaDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideSyncQueueDao(db: SolaScripturaDatabase): SyncQueueDao = db.syncQueueDao()
    @Provides fun provideReadingHistoryDao(db: SolaScripturaDatabase): ReadingHistoryDao = db.readingHistoryDao()
}

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): PreferencesDataStore =
        PreferencesDataStore(context.solaDataStore)

    @Provides
    @Singleton
    fun provideAssetStudyLoader(@ApplicationContext context: Context): AssetStudyLoader =
        AssetStudyLoader(context)

    @Provides
    @Singleton
    fun provideDialogueEngine(): DialogueEngine = DialogueEngine()

    @Provides
    @Singleton
    fun provideDialogueProgressManager(): DialogueProgressManager = DialogueProgressManager()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindStudyRepository(impl: StudyRepositoryImpl): StudyRepository

    @Binds @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds @Singleton
    abstract fun bindNotebookRepository(impl: NotebookRepositoryImpl): NotebookRepository

    @Binds @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository
}
