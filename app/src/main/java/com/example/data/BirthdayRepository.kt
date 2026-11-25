package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class BirthdayRepository(
    private val context: Context,
    private val dao: BirthdayDao
) {
    val profileFlow: Flow<CelebrationProfileEntity> = dao.observeProfile().map {
        it ?: defaultProfile()
    }

    val sectionsFlow: Flow<List<LetterSectionEntity>> = dao.observeSections()

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (dao.getProfileOnce() == null) {
            dao.upsertProfile(defaultProfile())
        }
        if (dao.getSectionCount() == 0) {
            dao.insertSections(defaultLetterSections())
        }
    }

    suspend fun updateProfile(
        friendName: String,
        senderName: String,
        letterTitle: String,
        closingLine: String
    ) = withContext(Dispatchers.IO) {
        val current = dao.getProfileOnce() ?: defaultProfile()
        dao.upsertProfile(
            current.copy(
                friendName = friendName.trim().ifEmpty { "Bestie" },
                senderName = senderName.trim().ifEmpty { "Your Friend" },
                letterTitle = letterTitle.trim().ifEmpty { "A Letter For You" },
                closingLine = closingLine.trim().ifEmpty { "Always cheering for you," }
            )
        )
    }

    suspend fun saveWish(wish: String) = withContext(Dispatchers.IO) {
        if (dao.getProfileOnce() == null) {
            dao.upsertProfile(defaultProfile())
        }
        dao.updateWish(wish.trim())
    }

    suspend fun addPickedPhoto(
        uri: Uri,
        caption: String,
        insertAtOrder: Int?
    ): Boolean = withContext(Dispatchers.IO) {
        val savedPath = copyPhotoToInternalStorage(uri) ?: return@withContext false
        val targetOrder = insertAtOrder ?: ((dao.getMaxSortOrder() ?: -1) + 1)
        val tilt = listOf(-2.2f, 1.8f, -1.4f, 2.4f).random()
        val section = LetterSectionEntity(
            sortOrder = targetOrder,
            type = LetterSectionType.PHOTO.name,
            photoSource = savedPath,
            photoCaption = caption.trim().ifEmpty { "One of my favorite memories with you" },
            tiltDegrees = tilt
        )
        if (insertAtOrder != null) {
            dao.insertSectionAtOrder(section)
        } else {
            dao.insertSection(section)
        }
        true
    }

    suspend fun addParagraph(text: String, insertAtOrder: Int? = null) = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return@withContext
        val targetOrder = insertAtOrder ?: ((dao.getMaxSortOrder() ?: -1) + 1)
        val section = LetterSectionEntity(
            sortOrder = targetOrder,
            type = LetterSectionType.PARAGRAPH.name,
            textContent = trimmed
        )
        if (insertAtOrder != null) {
            dao.insertSectionAtOrder(section)
        } else {
            dao.insertSection(section)
        }
    }

    suspend fun updateSection(section: LetterSectionEntity) = withContext(Dispatchers.IO) {
        dao.updateSection(section)
    }

    suspend fun deleteSection(section: LetterSectionEntity) = withContext(Dispatchers.IO) {
        if (section.type == LetterSectionType.PHOTO.name && !section.photoSource.startsWith("drawable:")) {
            runCatching { File(section.photoSource).delete() }
        }
        dao.deleteSectionById(section.id)
    }

    private fun copyPhotoToInternalStorage(uri: Uri): String? {
        return runCatching {
            val dir = File(context.filesDir, "letter_photos").apply { mkdirs() }
            val destFile = File(dir, "memory_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            destFile.absolutePath
        }.getOrNull()
    }

    private fun defaultProfile() = CelebrationProfileEntity(
        id = 1,
        friendName = "Bestie",
        senderName = "Your Forever Friend",
        letterTitle = "For You, On Your Birthday",
        closingLine = "With all the love and cake in the world,"
    )

    private fun defaultLetterSections(): List<LetterSectionEntity> = listOf(
        LetterSectionEntity(
            sortOrder = 0,
            type = LetterSectionType.PARAGRAPH.name,
            textContent = "Happy Birthday to the person who makes every ordinary Tuesday feel like a little holiday. I wanted to build something special for you today—a tiny pocket celebration you can open whenever you need a reminder of how deeply loved you are."
        ),
        LetterSectionEntity(
            sortOrder = 1,
            type = LetterSectionType.PHOTO.name,
            photoSource = "drawable:img_memory_cafe",
            photoCaption = "Slow mornings, iced coffee, and talking about everything",
            tiltDegrees = -2.0f
        ),
        LetterSectionEntity(
            sortOrder = 2,
            type = LetterSectionType.PARAGRAPH.name,
            textContent = "Thinking back over all the laughs we have shared, the late-night voice notes, and the spontaneous detours that turned into our best stories, I am endlessly grateful that life put us in the same chapter."
        ),
        LetterSectionEntity(
            sortOrder = 3,
            type = LetterSectionType.PARAGRAPH.name,
            textContent = "You have this rare magic of making everyone around you feel heard, brave, and completely themselves. Even on the busy or messy days, your kindness leaves everything a little softer and brighter."
        ),
        LetterSectionEntity(
            sortOrder = 4,
            type = LetterSectionType.PHOTO.name,
            photoSource = "drawable:img_memory_picnic",
            photoCaption = "Golden hour skies & our favorite little traditions",
            tiltDegrees = 1.8f
        ),
        LetterSectionEntity(
            sortOrder = 5,
            type = LetterSectionType.PARAGRAPH.name,
            textContent = "For this new year ahead of you, I wish you mornings without rushing, big bold dreams that surprise even you, gentle peace when things feel loud, and a hundred more reasons to laugh until your cheeks hurt."
        ),
        LetterSectionEntity(
            sortOrder = 6,
            type = LetterSectionType.PHOTO.name,
            photoSource = "drawable:img_memory_sparklers",
            photoCaption = "Here is to shining even brighter this year!",
            tiltDegrees = -1.5f
        ),
        LetterSectionEntity(
            sortOrder = 7,
            type = LetterSectionType.PARAGRAPH.name,
            textContent = "No matter how fast time flies or where our calendars take us, I will always be in your front row—cheering for your wins, holding space on hard days, and saving you the corner piece of cake with extra frosting."
        )
    )
}
