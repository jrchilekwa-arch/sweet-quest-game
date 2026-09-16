package com.example.sweetquest.haptics

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.sweetquest.model.BoosterType
import com.example.sweetquest.model.SpecialPiece

/**
 * VibrationManager provides tactile immersion across critical game actions
 * such as candy matching, combo cascades, booster power-up usage,
 * and level completion celebrations.
 *
 * It utilizes Android 12+ [VibratorManager] and [CombinedVibration] with
 * composition primitives where supported, with seamless fallbacks for older devices.
 */
class VibrationManager(private val context: Context) {

  var isEnabled: Boolean = true

  // Android 12 (API 31+) VibratorManager
  private val vibratorManager: VibratorManager? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    } else {
      null
    }
  }

  // System Vibrator (either default from VibratorManager or legacy system service)
  private val vibrator: Vibrator? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  /**
   * Dispatches a vibration effect through VibratorManager (CombinedVibration) on API 31+,
   * falling back to Vibrator on API 26-30, or legacy vibrate on older devices.
   */
  private fun playEffect(effect: VibrationEffect, fallbackDurationMs: Long = 40L) {
    if (!isEnabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && vibratorManager != null) {
        val combined = CombinedVibration.createParallel(effect)
        vibratorManager?.vibrate(combined)
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator != null) {
        vibrator?.vibrate(effect)
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(fallbackDurationMs)
      }
    } catch (_: Exception) {
      // Gracefully ignore if vibrator hardware is busy or unsupported
    }
  }

  /**
   * Tactile feedback for tile selection.
   */
  fun onTileSelected() {
    if (!isEnabled) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && vibrator?.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK) == true) {
      val effect = VibrationEffect.startComposition()
        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.4f)
        .compose()
      playEffect(effect, 15L)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      playEffect(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK), 15L)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      playEffect(VibrationEffect.createOneShot(15, 60), 15L)
    } else {
      playEffect(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE), 15L)
    }
  }

  /**
   * Tactile feedback for swapping two tiles.
   */
  fun onTileSwapped() {
    if (!isEnabled) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && vibrator?.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK) == true) {
      val effect = VibrationEffect.startComposition()
        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.6f)
        .compose()
      playEffect(effect, 20L)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      playEffect(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK), 20L)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      playEffect(VibrationEffect.createOneShot(20, 100), 20L)
    } else {
      playEffect(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE), 20L)
    }
  }

  /**
   * Tactile feedback for matching candies.
   * Escalates intensity based on combo level (Single, Double, Cascade, Sugar Rush).
   */
  fun onCandyMatched(combo: Int, clearedCount: Int = 3) {
    if (!isEnabled) return

    when {
      // Mega combo (Sugar Rush / Divine)
      combo >= 4 -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
          vibrator?.areAllPrimitivesSupported(
            VibrationEffect.Composition.PRIMITIVE_QUICK_RISE,
            VibrationEffect.Composition.PRIMITIVE_THUD
          ) == true
        ) {
          val effect = VibrationEffect.startComposition()
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.7f)
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f, 40)
            .compose()
          playEffect(effect, 100L)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          // Triple pulse rumble
          val timings = longArrayOf(0, 35, 30, 45, 30, 70)
          val amplitudes = intArrayOf(0, 140, 0, 200, 0, 255)
          playEffect(VibrationEffect.createWaveform(timings, amplitudes, -1), 120L)
        } else {
          playEffect(VibrationEffect.createOneShot(90, VibrationEffect.DEFAULT_AMPLITUDE), 90L)
        }
      }

      // Medium combo (Tasty x2 / x3)
      combo in 2..3 -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
          vibrator?.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK) == true
        ) {
          val effect = VibrationEffect.startComposition()
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.7f)
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.9f, 50)
            .compose()
          playEffect(effect, 60L)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          playEffect(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK), 50L)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val timings = longArrayOf(0, 25, 30, 35)
          val amplitudes = intArrayOf(0, 130, 0, 190)
          playEffect(VibrationEffect.createWaveform(timings, amplitudes, -1), 60L)
        } else {
          playEffect(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE), 50L)
        }
      }

      // Base 3-piece match
      else -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
          vibrator?.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK) == true
        ) {
          val effect = VibrationEffect.startComposition()
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f)
            .compose()
          playEffect(effect, 25L)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          playEffect(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK), 25L)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          playEffect(VibrationEffect.createOneShot(25, 120), 25L)
        } else {
          playEffect(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE), 25L)
        }
      }
    }
  }

  /**
   * Tactile feedback when a special power piece (Striped, Wrapped, Rainbow) is formed.
   */
  fun onSpecialPieceCreated(special: SpecialPiece) {
    if (!isEnabled) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
      vibrator?.areAllPrimitivesSupported(
        VibrationEffect.Composition.PRIMITIVE_TICK,
        VibrationEffect.Composition.PRIMITIVE_QUICK_RISE
      ) == true
    ) {
      val effect = VibrationEffect.startComposition()
        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f)
        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.8f, 25)
        .compose()
      playEffect(effect, 50L)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val timings = longArrayOf(0, 20, 25, 45)
      val amplitudes = intArrayOf(0, 90, 0, 210)
      playEffect(VibrationEffect.createWaveform(timings, amplitudes, -1), 50L)
    } else {
      playEffect(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE), 45L)
    }
  }

  /**
   * Tactile feedback when a special piece detonates (Laser, Bomb blast, Color crush).
   */
  fun onSpecialPieceDetonated(special: SpecialPiece) {
    if (!isEnabled) return
    when (special) {
      SpecialPiece.EXPLOSIVE_WRAPPED -> {
        // Double concussive blast
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val timings = longArrayOf(0, 45, 40, 80)
          val amplitudes = intArrayOf(0, 180, 0, 255)
          playEffect(VibrationEffect.createWaveform(timings, amplitudes, -1), 120L)
        } else {
          playEffect(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE), 100L)
        }
      }

      SpecialPiece.RAINBOW_PRISM -> {
        // Shimmering wave
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val timings = longArrayOf(0, 25, 20, 35, 20, 50)
          val amplitudes = intArrayOf(0, 120, 0, 180, 0, 240)
          playEffect(VibrationEffect.createWaveform(timings, amplitudes, -1), 110L)
        } else {
          playEffect(VibrationEffect.createOneShot(90, VibrationEffect.DEFAULT_AMPLITUDE), 90L)
        }
      }

      else -> {
        // Striped laser zap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          playEffect(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK), 45L)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          playEffect(VibrationEffect.createOneShot(45, 200), 45L)
        } else {
          playEffect(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE), 45L)
        }
      }
    }
  }

  /**
   * Tactile feedback for using in-game power-up boosters:
   * - HAMMER: Deep impact thud
   * - ROCKET: Cross-beam double shock
   * - RAINBOW_ORB: Chromatic cascade wave
   * - SHUFFLE: Rolling rhythmic pulse
   * - LIGHTNING: Rapid staccato electrical burst
   */
  fun onPowerUpUsed(type: BoosterType) {
    if (!isEnabled) return
    when (type) {
      BoosterType.HAMMER -> {
        // Deep impact hammer smash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
          vibrator?.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD) == true
        ) {
          val effect = VibrationEffect.startComposition()
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f)
            .compose()
          playEffect(effect, 80L)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          playEffect(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK), 70L)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          playEffect(VibrationEffect.createOneShot(70, 255), 70L)
        } else {
          playEffect(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE), 70L)
        }
      }

      BoosterType.ROCKET -> {
        // Rocket launch followed by cross blast impact
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val timings = longArrayOf(0, 30, 25, 60)
          val amplitudes = intArrayOf(0, 160, 0, 255)
          playEffect(VibrationEffect.createWaveform(timings, amplitudes, -1), 90L)
        } else {
          playEffect(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE), 80L)
        }
      }

      BoosterType.RAINBOW_ORB -> {
        // Prism resonance: 3 rising ripples
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val timings = longArrayOf(0, 30, 20, 45, 20, 75)
          val amplitudes = intArrayOf(0, 110, 0, 180, 0, 255)
          playEffect(VibrationEffect.createWaveform(timings, amplitudes, -1), 130L)
        } else {
          playEffect(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE), 100L)
        }
      }

      BoosterType.SHUFFLE -> {
        // Rolling board shuffle rhythm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val timings = longArrayOf(0, 25, 20, 25, 20, 35)
          val amplitudes = intArrayOf(0, 120, 0, 140, 0, 180)
          playEffect(VibrationEffect.createWaveform(timings, amplitudes, -1), 90L)
        } else {
          playEffect(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE), 80L)
        }
      }

      BoosterType.LIGHTNING -> {
        // Staccato crackle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val timings = longArrayOf(0, 20, 15, 25, 15, 30, 15, 55)
          val amplitudes = intArrayOf(0, 180, 0, 220, 0, 240, 0, 255)
          playEffect(VibrationEffect.createWaveform(timings, amplitudes, -1), 130L)
        } else {
          playEffect(VibrationEffect.createOneShot(110, VibrationEffect.DEFAULT_AMPLITUDE), 110L)
        }
      }
    }
  }

  /**
   * Tactile feedback for Level Completion celebration.
   * Plays a triumphant rhythmic cadence (short-short-short-LONG swell)
   * with escalating bursts matching stars earned (1, 2, or 3).
   */
  fun onLevelComplete(stars: Int) {
    if (!isEnabled) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
      vibrator?.areAllPrimitivesSupported(
        VibrationEffect.Composition.PRIMITIVE_CLICK,
        VibrationEffect.Composition.PRIMITIVE_QUICK_RISE,
        VibrationEffect.Composition.PRIMITIVE_THUD
      ) == true
    ) {
      val composition = VibrationEffect.startComposition()
        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.7f)
        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f, 60)
        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.9f, 60)
        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 1.0f, 80)
        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f, 60)

      // Add star accents
      for (i in 1..stars.coerceIn(1, 3)) {
        composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f, 100)
      }

      playEffect(composition.compose(), 300L)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Fanfare waveform: ta-ta-ta-TAAA!
      val timings = longArrayOf(0, 40, 50, 40, 50, 40, 70, 120, 80, 50)
      val amplitudes = intArrayOf(0, 140, 0, 160, 0, 180, 0, 255, 0, 200)
      playEffect(VibrationEffect.createWaveform(timings, amplitudes, -1), 350L)
    } else {
      playEffect(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE), 250L)
    }
  }

  /**
   * Tactile feedback for Level Failed / Out of Moves.
   */
  fun onLevelFailed() {
    if (!isEnabled) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val timings = longArrayOf(0, 70, 60, 110)
      val amplitudes = intArrayOf(0, 160, 0, 220)
      playEffect(VibrationEffect.createWaveform(timings, amplitudes, -1), 200L)
    } else {
      playEffect(VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE), 180L)
    }
  }

  /**
   * Tactile feedback for general UI button clicks.
   */
  fun onClick() {
    if (!isEnabled) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      playEffect(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK), 18L)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      playEffect(VibrationEffect.createOneShot(18, 80), 18L)
    } else {
      playEffect(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE), 18L)
    }
  }
}
