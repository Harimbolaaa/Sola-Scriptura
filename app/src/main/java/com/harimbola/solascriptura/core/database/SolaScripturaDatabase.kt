package com.harimbola.solascriptura.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.harimbola.solascriptura.core.database.dao.CategoryDao
import com.harimbola.solascriptura.core.database.dao.FavoriteDao
import com.harimbola.solascriptura.core.database.dao.ProgressDao
import com.harimbola.solascriptura.core.database.dao.ReadingHistoryDao
import com.harimbola.solascriptura.core.database.dao.StudyDao
import com.harimbola.solascriptura.core.database.dao.SyncQueueDao
import com.harimbola.solascriptura.core.database.dao.UserNoteDao
import com.harimbola.solascriptura.core.database.entity.CategoryEntity
import com.harimbola.solascriptura.core.database.entity.DialogueProgressEntity
import com.harimbola.solascriptura.core.database.entity.FavoriteEntity
import com.harimbola.solascriptura.core.database.entity.ReadingHistoryEntity
import com.harimbola.solascriptura.core.database.entity.StudyEntity
import com.harimbola.solascriptura.core.database.entity.SyncQueueEntity
import com.harimbola.solascriptura.core.database.entity.UserNoteEntity

@Database(
    entities = [
        StudyEntity::class,
        DialogueProgressEntity::class,
        UserNoteEntity::class,
        FavoriteEntity::class,
        CategoryEntity::class,
        SyncQueueEntity::class,
        ReadingHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SolaScripturaDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao
    abstract fun categoryDao(): CategoryDao
    abstract fun progressDao(): ProgressDao
    abstract fun userNoteDao(): UserNoteDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun readingHistoryDao(): ReadingHistoryDao

    companion object {
        /**
         * Reference versioned migration template for future schema updates.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Future schema migrations will be added explicitly here.
            }
        }
    }
}
