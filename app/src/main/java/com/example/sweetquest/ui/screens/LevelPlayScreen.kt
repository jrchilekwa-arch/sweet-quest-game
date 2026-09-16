package com.example.sweetquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.sweetquest.model.BoosterType
import com.example.sweetquest.model.GameTheme
import com.example.sweetquest.ui.ActiveDialog
import com.example.sweetquest.ui.AdRewardType
import com.example.sweetquest.ui.SweetQuestViewModel
import com.example.sweetquest.ui.components.BoardView
import com.example.sweetquest.ui.components.BoosterTray
import com.example.sweetquest.ui.components.GameHeader
import com.example.sweetquest.ui.components.LevelCompleteDialog
import com.example.sweetquest.ui.components.LevelFailedDialog
import com.example.sweetquest.ui.components.LevelPreviewDialog
import com.example.sweetquest.ui.components.PauseMenuDialog
import com.example.sweetquest.ui.components.QuickBuyBoosterDialog
import com.example.sweetquest.ui.components.RewardedAdDialog

@Composable
fun LevelPlayScreen(
  viewModel: SweetQuestViewModel,
  onQuitToMap: () -> Unit,
  modifier: Modifier = Modifier
) {
  val userProgress by viewModel.userProgress.collectAsState()
  val activeDialog by viewModel.activeDialog.collectAsState()
  val selectedTile by viewModel.selectedTile.collectAsState()
  val activeBooster by viewModel.activeBooster.collectAsState()
  val scorePopups by viewModel.scorePopups.collectAsState()
  val adCountdown by viewModel.adCountdown.collectAsState()
  val pendingReward by viewModel.pendingReward.collectAsState()
  val selectedBoosterToBuy by viewModel.selectedBoosterToBuy.collectAsState()
  val clearingTiles by viewModel.clearingTiles.collectAsState()
  val fallingTiles by viewModel.fallingTiles.collectAsState()
  val spawningTiles by viewModel.spawningTiles.collectAsState()

  val engineVersion by viewModel.engineVersion.collectAsState()
  val engine = viewModel.currentEngine

  val currentTheme = GameTheme.entries.find { it.id == userProgress.activeTheme } ?: GameTheme.CLASSIC
  val boardBgColor = Color(currentTheme.boardBgHex)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            Color(currentTheme.primaryColorHex).copy(alpha = 0.45f),
            Color(currentTheme.accentColorHex).copy(alpha = 0.25f),
            boardBgColor
          )
        )
      )
      .testTag("level_play_screen")
  ) {
    if (engine != null) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Game HUD Header
        GameHeader(
          levelConfig = engine.levelConfig,
          movesRemaining = engine.movesRemaining,
          score = engine.currentScore,
          stars = engine.calculateStars(),
          jelliesRemaining = engine.jelliesRemaining,
          blockersRemaining = engine.blockersRemaining,
          ingredientsCollected = engine.ingredientsCollected,
          candiesCollected = engine.candiesCollected,
          onPauseClick = { viewModel.openPauseMenu() }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Match-3 Puzzle Board
        BoardView(
          board = engine.board,
          selectedTile = selectedTile,
          activeBooster = activeBooster,
          scorePopups = scorePopups,
          clearingTiles = clearingTiles,
          fallingTiles = fallingTiles,
          spawningTiles = spawningTiles,
          boardBgColor = boardBgColor,
          engineVersion = engineVersion,
          onTileClick = { r, c -> viewModel.onTileClick(r, c) },
          onSwipe = { r1, c1, r2, c2 -> viewModel.onSwipe(r1, c1, r2, c2) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Booster Tray
        BoosterTray(
          hammerCount = userProgress.hammerCount,
          rocketCount = userProgress.rocketCount,
          rainbowCount = userProgress.rainbowOrbCount,
          shuffleCount = userProgress.shuffleCount,
          lightningCount = userProgress.lightningCount,
          activeBooster = activeBooster,
          onBoosterClick = { booster -> viewModel.selectBooster(booster) },
          onAddBoosterClick = { booster -> viewModel.openBoosterBuy(booster) }
        )
      }
    }

    // Modal Dialog Overlays
    when (activeDialog) {
      ActiveDialog.PAUSE -> {
        PauseMenuDialog(
          soundEnabled = userProgress.soundEnabled,
          musicEnabled = userProgress.musicEnabled,
          hapticsEnabled = userProgress.hapticsEnabled,
          onResume = { viewModel.resumeGame() },
          onRestart = { viewModel.restartLevel() },
          onToggleSound = { viewModel.toggleSound() },
          onToggleMusic = { viewModel.toggleMusic() },
          onToggleHaptics = { viewModel.toggleHaptics() },
          onQuitToMap = onQuitToMap
        )
      }
      ActiveDialog.LEVEL_COMPLETE -> {
        if (engine != null) {
          val stars = engine.calculateStars()
          LevelCompleteDialog(
            levelNumber = engine.levelConfig.levelNumber,
            stars = stars,
            score = engine.currentScore,
            coinsBonus = 50 + (stars * 20),
            onNextLevel = { viewModel.nextLevel() },
            onMapClick = onQuitToMap
          )
        }
      }
      ActiveDialog.LEVEL_FAILED -> {
        if (engine != null) {
          LevelFailedDialog(
            score = engine.currentScore,
            userCoins = userProgress.coins,
            onWatchAdForMoves = { viewModel.openRewardedAd(AdRewardType.EXTRA_MOVES) },
            onBuyMovesWithCoins = {
              if (userProgress.coins >= 100) {
                viewModel.buyBoosterWithCoins(BoosterType.HAMMER) // deducts coins
                engine.addMoves(5)
                viewModel.closeDialog()
              }
            },
            onRetry = { viewModel.restartLevel() },
            onQuitToMap = onQuitToMap
          )
        }
      }
      ActiveDialog.REWARDED_AD -> {
        RewardedAdDialog(
          countdown = adCountdown,
          rewardType = pendingReward ?: AdRewardType.EXTRA_MOVES
        )
      }
      ActiveDialog.BUY_BOOSTER -> {
        selectedBoosterToBuy?.let { booster ->
          QuickBuyBoosterDialog(
            boosterType = booster,
            userCoins = userProgress.coins,
            onBuyCoins = { viewModel.buyBoosterWithCoins(booster) },
            onWatchAd = { viewModel.openRewardedAd(AdRewardType.FREE_BOOSTER) },
            onDismiss = { viewModel.closeDialog() }
          )
        }
      }
      else -> {}
    }
  }
}
