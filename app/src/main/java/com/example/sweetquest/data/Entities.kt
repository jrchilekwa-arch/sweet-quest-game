package com.example.sweetquest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
  @PrimaryKey val id: Int = 1,
  val coins: Int = 250,
  val totalStars: Int = 0,
  val unlockedLevel: Int = 1,
  val lives: Int = 5,
  val lastLifeLostTimestamp: Long = 0L,
  val hammerCount: Int = 3,
  val rocketCount: Int = 3,
  val rainbowOrbCount: Int = 2,
  val shuffleCount: Int = 3,
  val lightningCount: Int = 2,
  val lastDailyRewardDay: Int = 0,
  val lastDailyClaimDate: Long = 0L,
  val currentDailyStreak: Int = 0,
  val activeTheme: String = "classic",
  val unlockedThemes: String = "classic",
  val soundEnabled: Boolean = true,
  val musicEnabled: Boolean = true,
  val hapticsEnabled: Boolean = true,
  val noAdsPurchased: Boolean = false,
  val totalMatchesMade: Int = 0,
  val totalCandiesPopped: Int = 0,
  val highestCombo: Int = 0,
  val levelsWon: Int = 0
)

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
  @PrimaryKey val levelNumber: Int,
  val starsEarned: Int = 0,
  val highScore: Int = 0,
  val isCompleted: Boolean = false
)
