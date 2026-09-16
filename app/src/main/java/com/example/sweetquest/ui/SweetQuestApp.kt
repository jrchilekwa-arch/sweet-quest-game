package com.example.sweetquest.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sweetquest.model.BoosterType
import com.example.sweetquest.ui.components.LevelPreviewDialog
import com.example.sweetquest.ui.screens.DailyRewardsScreen
import com.example.sweetquest.ui.screens.HomeScreen
import com.example.sweetquest.ui.screens.LevelPlayScreen
import com.example.sweetquest.ui.screens.SettingsScreen
import com.example.sweetquest.ui.screens.ShopScreen
import com.example.sweetquest.ui.screens.SplashScreen
import com.example.sweetquest.ui.screens.WorldMapScreen

@Composable
fun SweetQuestApp(
  viewModel: SweetQuestViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val activeDialog by viewModel.activeDialog.collectAsState()
  val userProgress by viewModel.userProgress.collectAsState()
  val levelProgressMap by viewModel.levelProgressMap.collectAsState()
  val selectedLevel by viewModel.selectedLevel.collectAsState()

  // Lifecycle-aware background music management
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> viewModel.soundManager.resumeMusic()
        Lifecycle.Event.ON_PAUSE -> viewModel.soundManager.pauseMusic()
        else -> {}
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  // Handle system back navigation
  BackHandler(enabled = currentScreen != ScreenState.HOME && currentScreen != ScreenState.SPLASH) {
    if (activeDialog != ActiveDialog.NONE) {
      viewModel.closeDialog()
    } else {
      when (currentScreen) {
        ScreenState.LEVEL_PLAY -> viewModel.openPauseMenu()
        ScreenState.WORLD_MAP, ScreenState.SHOP, ScreenState.DAILY_REWARDS, ScreenState.SETTINGS -> {
          viewModel.navigateTo(ScreenState.HOME)
        }
        else -> {}
      }
    }
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
          ScreenState.SPLASH -> {
            SplashScreen(
              onStartClick = { viewModel.navigateTo(ScreenState.HOME) }
            )
          }
          ScreenState.HOME -> {
            HomeScreen(
              userProgress = userProgress,
              onPlayClick = {
                val nextLevel = userProgress.unlockedLevel
                viewModel.openLevelPreview(nextLevel)
              },
              onWorldMapClick = { viewModel.navigateTo(ScreenState.WORLD_MAP) },
              onShopClick = { viewModel.navigateTo(ScreenState.SHOP) },
              onDailyRewardsClick = { viewModel.navigateTo(ScreenState.DAILY_REWARDS) },
              onSettingsClick = { viewModel.navigateTo(ScreenState.SETTINGS) },
              onAddLivesClick = { viewModel.openRewardedAd(AdRewardType.EXTRA_LIFE) },
              onAddCoinsClick = { viewModel.navigateTo(ScreenState.SHOP) }
            )
          }
          ScreenState.WORLD_MAP -> {
            WorldMapScreen(
              userProgress = userProgress,
              levelProgressMap = levelProgressMap,
              onLevelClick = { lvl -> viewModel.openLevelPreview(lvl) },
              onBackClick = { viewModel.navigateTo(ScreenState.HOME) },
              onAddLivesClick = { viewModel.openRewardedAd(AdRewardType.EXTRA_LIFE) },
              onAddCoinsClick = { viewModel.navigateTo(ScreenState.SHOP) }
            )
          }
          ScreenState.LEVEL_PLAY -> {
            LevelPlayScreen(
              viewModel = viewModel,
              onQuitToMap = { viewModel.navigateTo(ScreenState.WORLD_MAP) }
            )
          }
          ScreenState.SHOP -> {
            ShopScreen(
              userProgress = userProgress,
              onBuyBooster = { booster -> viewModel.buyBoosterWithCoins(booster) },
              onBuyPack = { packId, cost -> viewModel.buyBoosterPack(packId, cost) },
              onBuyTheme = { theme -> viewModel.buyTheme(theme) },
              onSelectTheme = { theme -> viewModel.selectTheme(theme) },
              onWatchAdForCoins = { viewModel.openRewardedAd(AdRewardType.EXTRA_MOVES) }, // triggers rewarded ad
              onPurchaseNoAds = { viewModel.purchaseNoAds() },
              onBackClick = { viewModel.navigateTo(ScreenState.HOME) },
              onAddLivesClick = { viewModel.openRewardedAd(AdRewardType.EXTRA_LIFE) },
              onAddCoinsClick = { viewModel.openRewardedAd(AdRewardType.EXTRA_MOVES) }
            )
          }
          ScreenState.DAILY_REWARDS -> {
            DailyRewardsScreen(
              userProgress = userProgress,
              onClaimReward = { day -> viewModel.claimDailyReward(day) },
              onBackClick = { viewModel.navigateTo(ScreenState.HOME) },
              onAddLivesClick = { viewModel.openRewardedAd(AdRewardType.EXTRA_LIFE) },
              onAddCoinsClick = { viewModel.navigateTo(ScreenState.SHOP) }
            )
          }
          ScreenState.SETTINGS -> {
            SettingsScreen(
              userProgress = userProgress,
              onToggleSound = { viewModel.toggleSound() },
              onToggleMusic = { viewModel.toggleMusic() },
              onToggleHaptics = { viewModel.toggleHaptics() },
              onResetProgress = { viewModel.resetAllProgress() },
              onBackClick = { viewModel.navigateTo(ScreenState.HOME) },
              onAddLivesClick = { viewModel.openRewardedAd(AdRewardType.EXTRA_LIFE) },
              onAddCoinsClick = { viewModel.navigateTo(ScreenState.SHOP) }
            )
          }
        }
      }

      // Level Preview Dialog overlay
      if (activeDialog == ActiveDialog.LEVEL_PREVIEW) {
        LevelPreviewDialog(
          levelNumber = selectedLevel,
          levelProgress = levelProgressMap[selectedLevel],
          livesRemaining = userProgress.lives,
          onPlayClick = { viewModel.startLevel(selectedLevel) },
          onDismiss = { viewModel.closeDialog() }
        )
      }
    }
  }
}
