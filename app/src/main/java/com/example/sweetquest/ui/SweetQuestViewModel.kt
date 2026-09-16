package com.example.sweetquest.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sweetquest.audio.SoundManager
import com.example.sweetquest.data.AppDatabase
import com.example.sweetquest.data.GameRepository
import com.example.sweetquest.data.LevelProgressEntity
import com.example.sweetquest.data.LevelsData
import com.example.sweetquest.data.UserProgressEntity
import com.example.sweetquest.engine.Match3Engine
import com.example.sweetquest.haptics.VibrationManager
import com.example.sweetquest.model.BoosterType
import com.example.sweetquest.model.GameTheme
import com.example.sweetquest.model.LevelConfig
import com.example.sweetquest.model.ScorePopup
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenState {
  SPLASH,
  HOME,
  WORLD_MAP,
  LEVEL_PLAY,
  SHOP,
  DAILY_REWARDS,
  SETTINGS
}

enum class ActiveDialog {
  NONE,
  LEVEL_PREVIEW,
  PAUSE,
  LEVEL_COMPLETE,
  LEVEL_FAILED,
  REWARDED_AD,
  BUY_BOOSTER
}

enum class AdRewardType {
  EXTRA_MOVES,
  EXTRA_LIFE,
  FREE_BOOSTER
}

class SweetQuestViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: GameRepository
  val soundManager: SoundManager
  val vibrationManager: VibrationManager
    get() = soundManager.vibrationManager

  val userProgress: StateFlow<UserProgressEntity>
  val levelProgressMap: StateFlow<Map<Int, LevelProgressEntity>>

  private val _currentScreen = MutableStateFlow(ScreenState.SPLASH)
  val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

  private val _activeDialog = MutableStateFlow(ActiveDialog.NONE)
  val activeDialog: StateFlow<ActiveDialog> = _activeDialog.asStateFlow()

  private val _selectedLevel = MutableStateFlow(1)
  val selectedLevel: StateFlow<Int> = _selectedLevel.asStateFlow()

  // Match 3 Game Engine state
  var currentEngine: Match3Engine? = null
    private set

  private val _engineVersion = MutableStateFlow(0)
  val engineVersion: StateFlow<Int> = _engineVersion.asStateFlow()

  private val _selectedTile = MutableStateFlow<Pair<Int, Int>?>(null)
  val selectedTile: StateFlow<Pair<Int, Int>?> = _selectedTile.asStateFlow()

  private val _activeBooster = MutableStateFlow<BoosterType?>(null)
  val activeBooster: StateFlow<BoosterType?> = _activeBooster.asStateFlow()

  private val _scorePopups = MutableStateFlow<List<ScorePopup>>(emptyList())
  val scorePopups: StateFlow<List<ScorePopup>> = _scorePopups.asStateFlow()

  // Cascading physics states
  private val _isCascading = MutableStateFlow(false)
  val isCascading: StateFlow<Boolean> = _isCascading.asStateFlow()

  private val _clearingTiles = MutableStateFlow<Set<Pair<Int, Int>>>(emptySet())
  val clearingTiles: StateFlow<Set<Pair<Int, Int>>> = _clearingTiles.asStateFlow()

  private val _fallingTiles = MutableStateFlow<Map<Pair<Int, Int>, Int>>(emptyMap())
  val fallingTiles: StateFlow<Map<Pair<Int, Int>, Int>> = _fallingTiles.asStateFlow()

  private val _spawningTiles = MutableStateFlow<Set<Pair<Int, Int>>>(emptySet())
  val spawningTiles: StateFlow<Set<Pair<Int, Int>>> = _spawningTiles.asStateFlow()

  private val _currentCascadeCombo = MutableStateFlow(0)
  val currentCascadeCombo: StateFlow<Int> = _currentCascadeCombo.asStateFlow()

  // Rewarded ad simulation state
  private val _pendingReward = MutableStateFlow<AdRewardType?>(null)
  val pendingReward: StateFlow<AdRewardType?> = _pendingReward.asStateFlow()

  private val _adCountdown = MutableStateFlow(5)
  val adCountdown: StateFlow<Int> = _adCountdown.asStateFlow()

  private val _selectedBoosterToBuy = MutableStateFlow<BoosterType?>(null)
  val selectedBoosterToBuy: StateFlow<BoosterType?> = _selectedBoosterToBuy.asStateFlow()

  init {
    val db = AppDatabase.getDatabase(application)
    repository = GameRepository(db.userDao())
    soundManager = SoundManager(application)

    userProgress = repository.userProgress.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      UserProgressEntity()
    )

    levelProgressMap = repository.levelProgressMap.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyMap()
    )

    viewModelScope.launch {
      repository.ensureInitialized()
      repository.checkAndRegenerateLives()

      // Sync sound settings
      userProgress.collect { progress ->
        soundManager.soundEnabled = progress.soundEnabled
        soundManager.musicEnabled = progress.musicEnabled
        soundManager.hapticsEnabled = progress.hapticsEnabled
      }
    }
  }

  fun navigateTo(screen: ScreenState) {
    soundManager.playClick()
    _currentScreen.value = screen
    _activeDialog.value = ActiveDialog.NONE
  }

  fun openLevelPreview(levelNumber: Int) {
    soundManager.playClick()
    _selectedLevel.value = levelNumber
    _activeDialog.value = ActiveDialog.LEVEL_PREVIEW
  }

  fun startLevel(levelNumber: Int) {
    soundManager.playClick()
    val progress = userProgress.value
    if (progress.lives <= 0) {
      // Need life!
      openRewardedAd(AdRewardType.EXTRA_LIFE)
      return
    }

    val config = LevelsData.getLevel(levelNumber)
    currentEngine = Match3Engine(config)
    _selectedLevel.value = levelNumber
    _selectedTile.value = null
    _activeBooster.value = null
    _scorePopups.value = emptyList()
    _engineVersion.value++
    _activeDialog.value = ActiveDialog.NONE
    _currentScreen.value = ScreenState.LEVEL_PLAY
  }

  fun onTileClick(row: Int, col: Int) {
    val engine = currentEngine ?: return
    if (_isCascading.value || engine.isLevelWon || engine.isLevelLost) return

    val booster = _activeBooster.value
    if (booster != null) {
      useBoosterOnTile(booster, row, col)
      return
    }

    val selected = _selectedTile.value
    if (selected == null) {
      val tile = engine.board[row][col]
      if (tile?.isMovable == true || tile?.special != com.example.sweetquest.model.SpecialPiece.NONE) {
        _selectedTile.value = Pair(row, col)
        vibrationManager.onTileSelected()
        soundManager.playClick()
      }
    } else {
      val (r1, c1) = selected
      if (r1 == row && c1 == col) {
        // Tap same tile to deselect
        _selectedTile.value = null
      } else if (kotlin.math.abs(r1 - row) + kotlin.math.abs(c1 - col) == 1) {
        // Adjacent tile: Swap!
        trySwap(r1, c1, row, col)
      } else {
        // Non-adjacent tile: Switch selection
        val newTile = engine.board[row][col]
        if (newTile?.isMovable == true || newTile?.special != com.example.sweetquest.model.SpecialPiece.NONE) {
          _selectedTile.value = Pair(row, col)
          vibrationManager.onTileSelected()
          soundManager.playClick()
        } else {
          _selectedTile.value = null
        }
      }
    }
  }

  fun onSwipe(r1: Int, c1: Int, r2: Int, c2: Int) {
    val engine = currentEngine ?: return
    if (_isCascading.value || engine.isLevelWon || engine.isLevelLost) return
    trySwap(r1, c1, r2, c2)
  }

  fun trySwap(r1: Int, c1: Int, r2: Int, c2: Int) {
    val engine = currentEngine ?: return
    if (_isCascading.value || engine.isLevelWon || engine.isLevelLost) return

    _selectedTile.value = null

    if (!engine.canSwap(r1, c1, r2, c2)) {
      // Invalid swap: visually swap, bump haptic/sound, then swap back!
      viewModelScope.launch {
        _isCascading.value = true
        soundManager.playSwap()
        engine.swapTiles(r1, c1, r2, c2)
        _engineVersion.value++
        delay(140)

        engine.swapTiles(r1, c1, r2, c2)
        _engineVersion.value++
        soundManager.playClick()
        vibrationManager.onClick()
        delay(100)
        _isCascading.value = false
      }
      return
    }

    soundManager.playSwap()
    viewModelScope.launch {
      runCascadeSwap(r1, c1, r2, c2)
    }
  }

  suspend fun runCascadeSwap(r1: Int, c1: Int, r2: Int, c2: Int) {
    val engine = currentEngine ?: return
    _isCascading.value = true

    val t1 = engine.board[r1][c1]
    val t2 = engine.board[r2][c2]

    // Special combo activation?
    if (t1 != null && t2 != null && (t1.special != com.example.sweetquest.model.SpecialPiece.NONE || t2.special != com.example.sweetquest.model.SpecialPiece.NONE)) {
      val comboResult = engine.executeSwap(r1, c1, r2, c2)
      if (comboResult != null) {
        handleMatchResult(comboResult)
        _isCascading.value = false
        _engineVersion.value++
        return
      }
    }

    // Normal swap
    engine.movesRemaining--
    engine.swapTiles(r1, c1, r2, c2)
    _engineVersion.value++
    delay(140)

    runCascadingPhysicsLoop(lastSwapPos = Pair(r2, c2))
  }

  suspend fun runCascadingPhysicsLoop(lastSwapPos: Pair<Int, Int>? = null) {
    val engine = currentEngine ?: run {
      _isCascading.value = false
      return
    }
    _isCascading.value = true

    var combo = 1
    var iteration = 0
    var totalCandiesPopped = 0

    while (iteration < 15) {
      iteration++
      val swapPos = if (iteration == 1) lastSwapPos else null
      val step = engine.clearNextMatchesStep(lastSwapPos = swapPos, combo = combo)
      if (step == null) break

      // Phase 1: Matches Cleared - highlight tiles popping
      _clearingTiles.value = step.clearedPositions
      _currentCascadeCombo.value = combo
      soundManager.playMatch(combo, step.clearedPositions.size)

      if (step.newlyCreatedSpecials.isNotEmpty()) {
        val firstSpecial = step.newlyCreatedSpecials.first().special
        soundManager.playSpecialCreate(firstSpecial)
      }
      if (step.specialsActivated.isNotEmpty()) {
        val firstSpecial = step.specialsActivated.first().special
        soundManager.playExplosion(firstSpecial)
      }

      _scorePopups.value = step.popups
      _engineVersion.value++
      totalCandiesPopped += step.clearedPositions.size

      // Pop / clear duration
      delay(200)
      engine.finalizeClearStep(step)
      _clearingTiles.value = emptySet()
      _engineVersion.value++

      // Phase 2: Cascading Physics - Candies fall down smoothly into empty slots
      val gravityResult = engine.applyGravityOnly()
      if (gravityResult.movements.isNotEmpty()) {
        val dropDistances = gravityResult.movements.associate {
          Pair(it.toRow, it.toCol) to (it.toRow - it.fromRow)
        }
        _fallingTiles.value = dropDistances
        _engineVersion.value++

        // Fall animation duration
        delay(200)
        _fallingTiles.value = emptyMap()
      }

      // Phase 3: Refill empty slots from top with newly spawned candies
      val refillResult = engine.refillEmptySlots()
      if (refillResult.newTiles.isNotEmpty()) {
        _spawningTiles.value = refillResult.newTiles.map { Pair(it.row, it.col) }.toSet()
        _engineVersion.value++

        // Refill entrance duration
        delay(200)
        _spawningTiles.value = emptySet()
      }

      // Settle pause before next cascade evaluation
      delay(80)
      combo++
    }

    if (totalCandiesPopped > 0) {
      repository.recordGameStats(
        matchesMade = 1,
        candiesPopped = totalCandiesPopped,
        combo = maxOf(1, combo - 1)
      )
    }

    // Clean up popups after display
    viewModelScope.launch {
      delay(800)
      _scorePopups.value = emptyList()
    }

    // Phase 4: Settle and evaluate Win/Loss
    engine.checkGameStatus()
    _engineVersion.value++

    if (engine.isLevelWon) {
      val stars = engine.calculateStars()
      soundManager.playWin(stars)
      repository.recordLevelCompleted(
        levelNumber = engine.levelConfig.levelNumber,
        stars = stars,
        score = engine.currentScore,
        bonusCoins = 50 + (stars * 20)
      )
      _activeDialog.value = ActiveDialog.LEVEL_COMPLETE
    } else if (engine.isLevelLost) {
      soundManager.playLose()
      repository.loseLife()
      _activeDialog.value = ActiveDialog.LEVEL_FAILED
    }

    _isCascading.value = false
    _currentCascadeCombo.value = 0
  }

  private fun handleMatchResult(result: com.example.sweetquest.engine.MatchResult) {
    val engine = currentEngine ?: return
    soundManager.playMatch(result.comboCount, result.clearedCount)

    if (result.specialPiecesCreated.isNotEmpty()) {
      val firstSpecial = result.specialPiecesCreated.first().special
      soundManager.playSpecialCreate(firstSpecial)
    }
    if (result.specialPiecesActivated.isNotEmpty()) {
      val firstSpecial = result.specialPiecesActivated.first().special
      soundManager.playExplosion(firstSpecial)
    }

    // Add popups
    _scorePopups.value = result.scorePopups
    viewModelScope.launch {
      delay(1200)
      _scorePopups.value = emptyList()
    }

    viewModelScope.launch {
      repository.recordGameStats(
        matchesMade = 1,
        candiesPopped = result.clearedCount,
        combo = result.comboCount
      )
    }

    // Check game condition
    if (engine.isLevelWon) {
      val stars = engine.calculateStars()
      soundManager.playWin(stars)
      viewModelScope.launch {
        repository.recordLevelCompleted(
          levelNumber = engine.levelConfig.levelNumber,
          stars = stars,
          score = engine.currentScore,
          bonusCoins = 50 + (stars * 20)
        )
      }
      _activeDialog.value = ActiveDialog.LEVEL_COMPLETE
    } else if (engine.isLevelLost) {
      soundManager.playLose()
      viewModelScope.launch {
        repository.loseLife()
      }
      _activeDialog.value = ActiveDialog.LEVEL_FAILED
    }
  }

  fun selectBooster(type: BoosterType) {
    soundManager.playClick()
    if (type == BoosterType.SHUFFLE || type == BoosterType.RAINBOW_ORB || type == BoosterType.LIGHTNING) {
      // Instant board effects
      val engine = currentEngine ?: return
      viewModelScope.launch {
        if (repository.consumeBooster(type)) {
          soundManager.playBooster(type)
          val result = engine.useBooster(type)
          handleMatchResult(result)
          _engineVersion.value++
        }
      }
    } else {
      // Targetable booster (Hammer, Rocket)
      _activeBooster.value = if (_activeBooster.value == type) null else type
    }
  }

  private fun useBoosterOnTile(booster: BoosterType, row: Int, col: Int) {
    val engine = currentEngine ?: return
    _activeBooster.value = null
    viewModelScope.launch {
      if (repository.consumeBooster(booster)) {
        soundManager.playBooster(booster)
        val result = engine.useBooster(booster, row, col)
        handleMatchResult(result)
        _engineVersion.value++
      }
    }
  }

  fun openBoosterBuy(type: BoosterType) {
    soundManager.playClick()
    _selectedBoosterToBuy.value = type
    _activeDialog.value = ActiveDialog.BUY_BOOSTER
  }

  fun buyBoosterWithCoins(type: BoosterType) {
    viewModelScope.launch {
      if (repository.spendCoins(type.coinCost)) {
        repository.addBooster(type, 1)
        soundManager.playWin()
        _activeDialog.value = ActiveDialog.NONE
      }
    }
  }

  fun openPauseMenu() {
    soundManager.playClick()
    _activeDialog.value = ActiveDialog.PAUSE
  }

  fun resumeGame() {
    soundManager.playClick()
    _activeDialog.value = ActiveDialog.NONE
  }

  fun restartLevel() {
    soundManager.playClick()
    val level = _selectedLevel.value
    startLevel(level)
  }

  fun nextLevel() {
    soundManager.playClick()
    val next = (_selectedLevel.value + 1).coerceAtMost(30)
    startLevel(next)
  }

  fun openRewardedAd(reward: AdRewardType) {
    soundManager.playClick()
    _pendingReward.value = reward
    _adCountdown.value = 5
    _activeDialog.value = ActiveDialog.REWARDED_AD

    viewModelScope.launch {
      for (sec in 4 downTo 0) {
        delay(1000)
        _adCountdown.value = sec
      }
      // Grant reward
      grantPendingAdReward()
    }
  }

  private fun grantPendingAdReward() {
    val reward = _pendingReward.value ?: return
    viewModelScope.launch {
      when (reward) {
        AdRewardType.EXTRA_MOVES -> {
          currentEngine?.addMoves(5)
          _activeDialog.value = ActiveDialog.NONE
          soundManager.playWin()
        }
        AdRewardType.EXTRA_LIFE -> {
          repository.refillLives(1)
          _activeDialog.value = ActiveDialog.NONE
          soundManager.playWin()
        }
        AdRewardType.FREE_BOOSTER -> {
          val randomBooster = BoosterType.entries.random()
          repository.addBooster(randomBooster, 1)
          _activeDialog.value = ActiveDialog.NONE
          soundManager.playWin()
        }
      }
      _pendingReward.value = null
    }
  }

  fun closeDialog() {
    soundManager.playClick()
    _activeDialog.value = ActiveDialog.NONE
  }

  fun claimDailyReward(day: Int) {
    soundManager.playClick()
    viewModelScope.launch {
      val coins = when (day) {
        1 -> 50
        2 -> 75
        3 -> 100
        4 -> 125
        5 -> 150
        6 -> 200
        else -> 350
      }
      val booster = when (day) {
        2 -> BoosterType.HAMMER
        4 -> BoosterType.ROCKET
        6 -> BoosterType.RAINBOW_ORB
        7 -> BoosterType.LIGHTNING
        else -> null
      }
      repository.claimDailyReward(day, coins, booster)
      soundManager.playWin()
    }
  }

  fun buyTheme(theme: GameTheme) {
    soundManager.playClick()
    viewModelScope.launch {
      repository.unlockTheme(theme.id, theme.priceCoins)
    }
  }

  fun selectTheme(theme: GameTheme) {
    soundManager.playClick()
    viewModelScope.launch {
      repository.setActiveTheme(theme.id)
    }
  }

  fun buyBoosterPack(packId: String, costCoins: Int) {
    soundManager.playClick()
    viewModelScope.launch {
      if (repository.spendCoins(costCoins)) {
        when (packId) {
          "sweet_starter" -> {
            repository.addBooster(BoosterType.HAMMER, 2)
            repository.addBooster(BoosterType.ROCKET, 2)
            repository.addBooster(BoosterType.SHUFFLE, 2)
          }
          "master_chocolatier" -> {
            repository.addBooster(BoosterType.RAINBOW_ORB, 3)
            repository.addBooster(BoosterType.LIGHTNING, 3)
            repository.addBooster(BoosterType.HAMMER, 3)
          }
        }
        soundManager.playWin()
      }
    }
  }

  fun purchaseNoAds() {
    soundManager.playClick()
    viewModelScope.launch {
      if (repository.spendCoins(500)) {
        repository.setNoAdsPurchased()
        soundManager.playWin()
      }
    }
  }

  fun toggleSound() {
    viewModelScope.launch {
      val currentlyEnabled = userProgress.value.soundEnabled
      val willBeEnabled = !currentlyEnabled
      soundManager.soundEnabled = willBeEnabled
      repository.toggleSound()
      if (willBeEnabled) {
        soundManager.playMatch(1, 3)
      }
    }
  }

  fun toggleMusic() {
    viewModelScope.launch {
      val currentlyEnabled = userProgress.value.musicEnabled
      val willBeEnabled = !currentlyEnabled
      soundManager.musicEnabled = willBeEnabled
      repository.toggleMusic()
    }
  }

  fun toggleHaptics() {
    viewModelScope.launch {
      val currentlyEnabled = userProgress.value.hapticsEnabled
      val willBeEnabled = !currentlyEnabled
      soundManager.hapticsEnabled = willBeEnabled
      repository.toggleHaptics()
      if (willBeEnabled) {
        vibrationManager.onTileSelected()
      }
    }
  }

  fun resetAllProgress() {
    viewModelScope.launch {
      repository.resetAll()
      soundManager.playLose()
      _currentScreen.value = ScreenState.HOME
    }
  }

  override fun onCleared() {
    super.onCleared()
    soundManager.release()
  }
}
