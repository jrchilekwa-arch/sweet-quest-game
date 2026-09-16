package com.example.sweetquest.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM user_progress WHERE id = 1")
  fun getUserProgress(): Flow<UserProgressEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateUserProgress(progress: UserProgressEntity)

  @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
  fun getAllLevelProgress(): Flow<List<LevelProgressEntity>>

  @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber")
  fun getLevelProgress(levelNumber: Int): Flow<LevelProgressEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateLevelProgress(progress: LevelProgressEntity)

  @Query("DELETE FROM level_progress")
  suspend fun clearLevelProgress()

  @Query("DELETE FROM user_progress")
  suspend fun clearUserProgress()
}
