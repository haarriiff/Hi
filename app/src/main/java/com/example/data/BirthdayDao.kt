package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BirthdayDao {

    @Query("SELECT * FROM celebration_profile WHERE id = 1")
    fun observeProfile(): Flow<CelebrationProfileEntity?>

    @Query("SELECT * FROM celebration_profile WHERE id = 1")
    suspend fun getProfileOnce(): CelebrationProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: CelebrationProfileEntity)

    @Query("UPDATE celebration_profile SET savedWish = :wish WHERE id = 1")
    suspend fun updateWish(wish: String)

    @Query("SELECT * FROM letter_sections ORDER BY sortOrder ASC, id ASC")
    fun observeSections(): Flow<List<LetterSectionEntity>>

    @Query("SELECT COUNT(*) FROM letter_sections")
    suspend fun getSectionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: LetterSectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<LetterSectionEntity>)

    @Update
    suspend fun updateSection(section: LetterSectionEntity)

    @Query("UPDATE letter_sections SET sortOrder = sortOrder + 1 WHERE sortOrder >= :fromOrder")
    suspend fun shiftSortOrdersFrom(fromOrder: Int)

    @Query("DELETE FROM letter_sections WHERE id = :id")
    suspend fun deleteSectionById(id: Long)

    @Query("SELECT MAX(sortOrder) FROM letter_sections")
    suspend fun getMaxSortOrder(): Int?

    @Transaction
    suspend fun insertSectionAtOrder(section: LetterSectionEntity) {
        shiftSortOrdersFrom(section.sortOrder)
        insertSection(section)
    }
}
