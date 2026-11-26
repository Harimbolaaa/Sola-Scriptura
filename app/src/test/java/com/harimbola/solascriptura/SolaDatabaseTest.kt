package com.harimbola.solascriptura

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.harimbola.solascriptura.core.database.SolaScripturaDatabase
import com.harimbola.solascriptura.core.database.entity.DialogueProgressEntity
import com.harimbola.solascriptura.core.database.entity.FavoriteEntity
import com.harimbola.solascriptura.core.database.entity.StudyEntity
import com.harimbola.solascriptura.core.database.entity.UserNoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = android.app.Application::class)
class SolaDatabaseTest {

    private lateinit var db: SolaScripturaDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SolaScripturaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `room stores multi-locale studies and progress separately without overwriting`() = runTest {
        val studyDao = db.studyDao()
        val progressDao = db.progressDao()
        val noteDao = db.userNoteDao()
        val favDao = db.favoriteDao()

        val frStudy = StudyEntity(
            id = "etat_des_morts_001_fr",
            studyId = "etat_des_morts_001",
            schemaVersion = 1,
            contentVersion = 1,
            locale = "fr",
            title = "État des morts",
            subtitle = "Sub",
            categoryId = "forgotten_truths",
            description = "Desc",
            difficulty = "beginner",
            estimatedMinutes = 15,
            tagsJson = "[\"mort\"]",
            perspective = "SDA",
            jsonContent = "{}",
            checksum = "abc",
            editorialStatus = "REVIEWED",
            downloadedAt = 1L,
            lastUpdatedAt = 1L,
            isAvailable = true,
            isBundledAsset = true
        )
        val enStudy = frStudy.copy(
            id = "etat_des_morts_001_en",
            locale = "en",
            title = "State of the Dead"
        )
        studyDao.upsertStudies(listOf(frStudy, enStudy))

        val variants = studyDao.getStudiesById("etat_des_morts_001")
        assertEquals(2, variants.size)
        assertNotNull(studyDao.getStudyByIdAndLocale("etat_des_morts_001", "en"))

        progressDao.upsertProgress(
            DialogueProgressEntity(
                id = "etat_des_morts_001",
                studyId = "etat_des_morts_001",
                currentNodeId = "n2",
                historyJson = "{}",
                answersJson = "{}",
                unlockedNotionsJson = "[\"sleep\"]",
                personalConclusion = "Ma conclusion",
                isUnconvinced = false,
                isCompleted = true,
                startedAt = 10L,
                lastOpenedAt = 20L,
                completedAt = 20L,
                updatedAt = 20L
            )
        )
        val prog = progressDao.observeProgressForStudy("etat_des_morts_001").first()
        assertEquals("n2", prog?.currentNodeId)
        assertTrue(prog?.isCompleted == true)

        noteDao.upsertNote(
            UserNoteEntity("note_1", "etat_des_morts_001", null, "CONCLUSION", "Titre", "Contenu", "Jean 11:11", 10L, 10L)
        )
        assertEquals(1, noteDao.observeAllNotes().first().size)

        favDao.addFavorite(FavoriteEntity("fav_1", "etat_des_morts_001", 10L))
        assertTrue(favDao.observeIsFavorite("etat_des_morts_001").first())
    }
}
