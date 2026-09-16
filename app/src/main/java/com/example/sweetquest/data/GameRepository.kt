package com.example.sweetquest.data

import com.example.sweetquest.model.BoosterType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class GameRepository(private val userDao: UserDao) {

  val userProgress: Flow<UserProgressEntity> = userDao.getUserProgress().map { it ?: UserProgressEntity() }

  val levelProgressMap: Flow<Map<Int, LevelProgressEntity>> = userDao.getAllLevelProgress().map { list ->
    list.associateBy { it.levelNumber }
  }

  suspend fun ensureInitialized() {
    val current = userDao.getUserProgress().firstOrNull()
    if (current == null) {
      userDao.insertOrUpdateUserProgress(UserProgressEntity())
    }
  }

  suspend fun getCurrentProgress(): UserProgressEntity {
    return userDao.getUserProgress().firstOrNull() ?: UserProgressEntity().also {
      userDao.insertOrUpdateUserProgress(it)
    }
  }

  suspend fun updateProgress(transform: (UserProgressEntity) -> UserProgressEntity) {
    val current = getCurrentProgress()
    val updated = transform(current)
    userDao.insertOrUpdateUserProgress(updated)
  }

  suspend fun addCoins(amount: Int) {
    updateProgress { it.copy(coins = it.coins + amount) }
  }

  suspend fun spendCoins(amount: Int): Boolean {
    val current = getCurrentProgress()
    if (current.coins >= amount) {
      userDao.insertOrUpdateUserProgress(current.copy(coins = current.coins - amount))
      return true
    }
    return false
  }

  suspend fun addBooster(type: BoosterType, count: Int = 1) {
    updateProgress { current ->
      when (type) {
        BoosterType.HAMMER -> current.copy(hammerCount = current.hammerCount + count)
        BoosterType.ROCKET -> current.copy(rocketCount = current.rocketCount + count)
        BoosterType.RAINBOW_ORB -> current.copy(rainbowOrbCount = current.rainbowOrbCount + count)
        BoosterType.SHUFFLE -> current.copy(shuffleCount = current.shuffleCount + count)
        BoosterType.LIGHTNING -> current.copy(lightningCount = current.lightningCount + count)
      }
    }
  }

  suspend fun consumeBooster(type: BoosterType): Boolean {
    val current = getCurrentProgress()
    val hasBooster = when (type) {
      BoosterType.HAMMER -> current.hammerCount > 0
      BoosterType.ROCKET -> current.rocketCount > 0
      BoosterType.RAINBOW_ORB -> current.rainbowOrbCount > 0
      BoosterType.SHUFFLE -> current.shuffleCount > 0
      BoosterType.LIGHTNING -> current.lightningCount > 0
    }
    if (hasBooster) {
      updateProgress { curr ->
        when (type) {
          BoosterType.HAMMER -> curr.copy(hammerCount = curr.hammerCount - 1)
          BoosterType.ROCKET -> curr.copy(rocketCount = curr.rocketCount - 1)
          BoosterType.RAINBOW_ORB -> curr.copy(rainbowOrbCount = curr.rainbowOrbCount - 1)
          BoosterType.SHUFFLE -> curr.copy(shuffleCount = curr.shuffleCount - 1)
          BoosterType.LIGHTNING -> curr.copy(lightningCount = curr.lightningCount - 1)
        }
      }
      return true
    }
    return false
  }

  suspend fun checkAndRegenerateLives() {
    val current = getCurrentProgress()
    if (current.lives < 5 && current.lastLifeLostTimestamp > 0) {
      val now = System.currentTimeMillis()
      val millisPerLife = 20 * 60 * 1000L // 20 minutes
      val elapsed = now - current.lastLifeLostTimestamp
      val livesToAdd = (elapsed / millisPerLife).toInt()
      if (livesToAdd > 0) {
        val newLives = (current.lives + livesToAdd).coerceAtMost(5)
        val newTimestamp = if (newLives >= 5) 0L else current.lastLifeLostTimestamp + (livesToAdd * millisPerLife)
        userDao.insertOrUpdateUserProgress(current.copy(lives = newLives, lastLifeLostTimestamp = newTimestamp))
      }
    }
  }

  suspend fun loseLife() {
    val current = getCurrentProgress()
    if (current.lives > 0) {
      val newLives = current.lives - 1
      val now = System.currentTimeMillis()
      val newTimestamp = if (current.lastLifeLostTimestamp == 0L) now else current.lastLifeLostTimestamp
      userDao.insertOrUpdateUserProgress(current.copy(lives = newLives, lastLifeLostTimestamp = newTimestamp))
    }
  }

  suspend fun refillLives(amount: Int = 5) {
    updateProgress { current ->
      current.copy(lives = (current.lives + amount).coerceAtMost(5), lastLifeLostTimestamp = 0L)
    }
  }

  suspend fun recordLevelCompleted(levelNumber: Int, stars: Int, score: Int, bonusCoins: Int = 50) {
    val currentProgress = getCurrentProgress()
    val existing = userDao.getLevelProgress(levelNumber).firstOrNull()
    val previousStars = existing?.starsEarned ?: 0
    val newStars = maxOf(previousStars, stars)
    val newHighScore = maxOf(existing?.highScore ?: 0, score)

    userDao.insertOrUpdateLevelProgress(
      LevelProgressEntity(
        levelNumber = levelNumber,
        starsEarned = newStars,
        highScore = newHighScore,
        isCompleted = true
      )
    )

    val starDiff = if (stars > previousStars) stars - previousStars else 0
    val nextUnlocked = if (levelNumber == currentProgress.unlockedLevel && levelNumber < 30) {
      levelNumber + 1
    } else {
      currentProgress.unlockedLevel
    }

    updateProgress {
      it.copy(
        totalStars = it.totalStars + starDiff,
        unlockedLevel = nextUnlocked,
        coins = it.coins + bonusCoins,
        levelsWon = it.levelsWon + 1
      )
    }
  }

  suspend fun recordGameStats(matchesMade: Int, candiesPopped: Int, combo: Int) {
    updateProgress {
      it.copy(
        totalMatchesMade = it.totalMatchesMade + matchesMade,
        totalCandiesPopped = it.totalCandiesPopped + candiesPopped,
        highestCombo = maxOf(it.highestCombo, combo)
      )
    }
  }

  suspend fun claimDailyReward(day: Int, coinsAwarded: Int, boosterAwarded: BoosterType?): Boolean {
    val current = getCurrentProgress()
    val now = System.currentTimeMillis()
    val updated = current.copy(
      lastDailyRewardDay = day,
      lastDailyClaimDate = now,
      currentDailyStreak = current.currentDailyStreak + 1,
      coins = current.coins + coinsAwarded,
      hammerCount = current.hammerCount + (if (boosterAwarded == BoosterType.HAMMER) 1 else 0),
      rocketCount = current.rocketCount + (if (boosterAwarded == BoosterType.ROCKET) 1 else 0),
      rainbowOrbCount = current.rainbowOrbCount + (if (boosterAwarded == BoosterType.RAINBOW_ORB) 1 else 0),
      shuffleCount = current.shuffleCount + (if (boosterAwarded == BoosterType.SHUFFLE) 1 else 0),
      lightningCount = current.lightningCount + (if (boosterAwarded == BoosterType.LIGHTNING) 1 else 0)
    )
    userDao.insertOrUpdateUserProgress(updated)
    return true
  }

  suspend fun unlockTheme(themeId: String, cost: Int): Boolean {
    val current = getCurrentProgress()
    if (current.coins >= cost) {
      val themes = current.unlockedThemes.split(",").toMutableSet()
      themes.add(themeId)
      userDao.insertOrUpdateUserProgress(
        current.copy(
          coins = current.coins - cost,
          unlockedThemes = themes.joinToString(","),
          activeTheme = themeId
        )
      )
      return true
    }
    return false
  }

  suspend fun setActiveTheme(themeId: String) {
    updateProgress { it.copy(activeTheme = themeId) }
  }

  suspend fun toggleSound() {
    updateProgress { it.copy(soundEnabled = !it.soundEnabled) }
  }

  suspend fun toggleMusic() {
    updateProgress { it.copy(musicEnabled = !it.musicEnabled) }
  }

  suspend fun toggleHaptics() {
    updateProgress { it.copy(hapticsEnabled = !it.hapticsEnabled) }
  }

  suspend fun setNoAdsPurchased() {
    updateProgress { it.copy(noAdsPurchased = true) }
  }

  suspend fun resetAll() {
    userDao.clearLevelProgress()
    userDao.clearUserProgress()
    userDao.insertOrUpdateUserProgress(UserProgressEntity())
  }
}
