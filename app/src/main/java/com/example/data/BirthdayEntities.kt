package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LetterSectionType {
    PARAGRAPH,
    PHOTO
}

@Entity(tableName = "celebration_profile")
data class CelebrationProfileEntity(
    @PrimaryKey val id: Int = 1,
    val friendName: String,
    val senderName: String,
    val letterTitle: String,
    val closingLine: String,
    val savedWish: String = ""
)

@Entity(tableName = "letter_sections")
data class LetterSectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sortOrder: Int,
    val type: String, // LetterSectionType.name
    val textContent: String = "",
    val photoSource: String = "", // either "drawable:<res_name>" or absolute internal file path
    val photoCaption: String = "",
    val tiltDegrees: Float = 0f
)
