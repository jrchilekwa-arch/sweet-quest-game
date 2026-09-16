package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.sweetquest.haptics.VibrationManager
import com.example.sweetquest.model.BoosterType
import com.example.sweetquest.model.SpecialPiece
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VibrationManagerTest {

  private lateinit var vibrationManager: VibrationManager

  @Before
  fun setUp() {
    vibrationManager = VibrationManager(ApplicationProvider.getApplicationContext())
  }

  @Test
  fun testInitialStateAndToggle() {
    assertTrue(vibrationManager.isEnabled)
    vibrationManager.isEnabled = false
    assertFalse(vibrationManager.isEnabled)
    vibrationManager.isEnabled = true
    assertTrue(vibrationManager.isEnabled)
  }

  @Test
  fun testCandyMatchedHaptics() {
    // Single match
    vibrationManager.onCandyMatched(combo = 1, clearedCount = 3)
    // Double combo
    vibrationManager.onCandyMatched(combo = 2, clearedCount = 4)
    // Mega combo / cascade
    vibrationManager.onCandyMatched(combo = 5, clearedCount = 8)
  }

  @Test
  fun testSpecialPiecesHaptics() {
    SpecialPiece.entries.forEach { piece ->
      vibrationManager.onSpecialPieceCreated(piece)
      vibrationManager.onSpecialPieceDetonated(piece)
    }
  }

  @Test
  fun testPowerUpBoosterHaptics() {
    BoosterType.entries.forEach { booster ->
      vibrationManager.onPowerUpUsed(booster)
    }
  }

  @Test
  fun testLevelCompletionHaptics() {
    // 1, 2, and 3 star victory fanfares
    vibrationManager.onLevelComplete(stars = 1)
    vibrationManager.onLevelComplete(stars = 2)
    vibrationManager.onLevelComplete(stars = 3)

    // Level failed
    vibrationManager.onLevelFailed()
  }

  @Test
  fun testDisabledHapticsDoesNotThrow() {
    vibrationManager.isEnabled = false
    vibrationManager.onCandyMatched(combo = 3)
    vibrationManager.onPowerUpUsed(BoosterType.HAMMER)
    vibrationManager.onLevelComplete(stars = 3)
  }
}
