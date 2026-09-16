package com.example.sweetquest.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.sweetquest.haptics.VibrationManager
import com.example.sweetquest.model.BoosterType
import com.example.sweetquest.model.SpecialPiece
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Comprehensive Sound Management System for Sweet Quest.
 * Features:
 * - High-quality, zero-dependency procedural audio synthesis using AudioTrack PCM.
 * - Distinct, playful sound effects for candy matches, cascades, power-up activations, and level outcomes.
 * - Seamless looping background music (BGM) playing a whimsical candy theme.
 * - Instant toggle support for Music and FX in Settings and Pause menus.
 */
class SoundManager(
  private val context: Context,
  val vibrationManager: VibrationManager = VibrationManager(context)
) {

  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  var soundEnabled: Boolean = true

  var hapticsEnabled: Boolean
    get() = vibrationManager.isEnabled
    set(value) {
      vibrationManager.isEnabled = value
    }

  private var _musicEnabled: Boolean = true
  var musicEnabled: Boolean
    get() = _musicEnabled
    set(value) {
      _musicEnabled = value
      if (value) {
        resumeMusic()
      } else {
        pauseMusic()
      }
    }

  // Background music AudioTrack
  private var musicTrack: AudioTrack? = null
  private var isMusicInitialized = false
  private var musicPrepJob: Job? = null

  init {
    // Asynchronously prepare the background music loop so it's instantly ready
    musicPrepJob = coroutineScope.launch {
      initMusicTrack()
      if (_musicEnabled) {
        startMusic()
      }
    }
  }

  // ==========================================
  // CANDY MATCH & COMBO SOUND EFFECTS
  // ==========================================

  /**
   * Distinct, playful sound effect for candy matches.
   * Plays a crisp candy pop followed by bright, sweet pentatonic bell chimes that
   * ascend in pitch and sparkle with increasing cascade combos.
   */
  fun playMatch(combo: Int, clearedCount: Int = 3) {
    vibrationManager.onCandyMatched(combo, clearedCount)
    if (!soundEnabled) return

    // Pentatonic scale in C Major (C5, D5, E5, G5, A5, C6, D6, E6, G6)
    val scale = listOf(523.25, 587.33, 659.25, 783.99, 880.0, 1046.50, 1174.66, 1318.51, 1567.98)
    val baseFreq = scale[((combo - 1).coerceAtLeast(0)) % scale.size]

    coroutineScope.launch {
      // 1. Initial juicy candy pop click (fast downward pitch blip)
      playJuicyPopSync(durationMs = 28)

      // 2. Sweet resonant bell chime
      playToneSync(
        frequency = baseFreq,
        durationMs = if (combo > 3) 180 else 140,
        type = ToneType.SWEET_BELL,
        volume = 0.85f
      )

      // 3. Additional melodic sparkle for cascades (combo >= 2)
      if (combo >= 2) {
        delay(40)
        val sparkleFreq = baseFreq * 1.5 // fifth harmonic sparkle
        playToneSync(
          frequency = sparkleFreq,
          durationMs = 90,
          type = ToneType.SPARKLE,
          volume = 0.65f
        )
      }

      // 4. Celebratory flourish for large 4+ candy matches
      if (clearedCount >= 4) {
        delay(50)
        playToneSync(
          frequency = baseFreq * 2.0, // octave ring
          durationMs = 120,
          type = ToneType.SWEET_BELL,
          volume = 0.70f
        )
      }
    }
  }

  /**
   * Sound effect when swapping adjacent candies.
   */
  fun playSwap() {
    vibrationManager.onTileSwapped()
    if (!soundEnabled) return
    playSweep(startFreq = 380.0, endFreq = 680.0, durationMs = 75, volume = 0.60f)
  }

  /**
   * Sound effect for standard UI clicks and selections.
   */
  fun playClick() {
    vibrationManager.onClick()
    if (!soundEnabled) return
    playTone(frequency = 659.25, durationMs = 35, type = ToneType.SINE, volume = 0.50f)
  }

  // ==========================================
  // SPECIAL PIECE CREATIONS
  // ==========================================

  /**
   * Distinct playful sound when creating special candies from 4-matches, 5-matches, or L/T shapes.
   */
  fun playSpecialCreate(special: SpecialPiece = SpecialPiece.NONE) {
    vibrationManager.onSpecialPieceCreated(special)
    if (!soundEnabled) return

    coroutineScope.launch {
      when (special) {
        SpecialPiece.HORIZONTAL_STRIPED, SpecialPiece.VERTICAL_STRIPED -> {
          // Playful ascending whistle zap: whoop-ding!
          playSweepSync(startFreq = 420.0, endFreq = 950.0, durationMs = 90, volume = 0.75f)
          playToneSync(frequency = 1046.50, durationMs = 120, type = ToneType.SWEET_BELL, volume = 0.80f)
        }
        SpecialPiece.EXPLOSIVE_WRAPPED -> {
          // Double bubble-pop and sparkling low ring: bloop-ding!
          playSweepSync(startFreq = 260.0, endFreq = 580.0, durationMs = 60, volume = 0.70f)
          playSweepSync(startFreq = 340.0, endFreq = 780.0, durationMs = 70, volume = 0.75f)
          playToneSync(frequency = 880.0, durationMs = 140, type = ToneType.SWEET_BELL, volume = 0.80f)
        }
        SpecialPiece.RAINBOW_PRISM -> {
          // Magical ascending crystal glockenspiel glissando
          val rainbowNotes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
          for (note in rainbowNotes) {
            playToneSync(frequency = note, durationMs = 50, type = ToneType.SPARKLE, volume = 0.75f)
            delay(35)
          }
        }
        SpecialPiece.NONE -> {
          playToneSync(frequency = 784.0, durationMs = 80, type = ToneType.SWEET_BELL, volume = 0.70f)
        }
      }
    }
  }

  // ==========================================
  // POWER-UP & BOOSTER ACTIVATIONS
  // ==========================================

  /**
   * Distinct, playful sound effects for booster activation:
   * - HAMMER: Cartoon heavy mallet whack / bonk with woody candy crunch
   * - ROCKET: Upward fizzy rocket whoosh and pop
   * - RAINBOW_ORB: Magical sparkling celestial harp cascade
   * - SHUFFLE: Whimsical flurry whirl / rapid swirling trill
   * - LIGHTNING: Electric candy zap with high-energy crackle
   */
  fun playBooster(type: BoosterType) {
    vibrationManager.onPowerUpUsed(type)
    if (!soundEnabled) return

    coroutineScope.launch {
      when (type) {
        BoosterType.HAMMER -> {
          // Whimsical cartoon bonk: deep bass punch + downward cartoon pitch drop + candy crunch
          playCartoonBonkSync()
        }
        BoosterType.ROCKET -> {
          // Upward fizzy rocket whistle whoosh ending with a crisp pop
          playRocketWhooshSync()
        }
        BoosterType.RAINBOW_ORB -> {
          // Shimmering rainbow harp glissando
          playRainbowArpeggioSync()
        }
        BoosterType.SHUFFLE -> {
          // Whimsical flurry whirlwind whoosh
          playWhirlwindFlurrySync()
        }
        BoosterType.LIGHTNING -> {
          // Electric crackling candy zap
          playElectricZapSync()
        }
      }
    }
  }

  /**
   * Sound effect for candy explosions and special detonations.
   */
  fun playExplosion(special: SpecialPiece = SpecialPiece.EXPLOSIVE_WRAPPED) {
    vibrationManager.onSpecialPieceDetonated(special)
    if (!soundEnabled) return

    coroutineScope.launch {
      when (special) {
        SpecialPiece.EXPLOSIVE_WRAPPED -> {
          // Warm candy bomb: punchy bass thump + sparkling crackle burst
          playCandyExplosionSync()
        }
        SpecialPiece.HORIZONTAL_STRIPED, SpecialPiece.VERTICAL_STRIPED -> {
          playLaser()
        }
        SpecialPiece.RAINBOW_PRISM -> {
          playRainbow()
        }
        SpecialPiece.NONE -> {
          playNoiseBurst(durationMs = 180, volume = 0.70f)
        }
      }
    }
  }

  fun playLaser() {
    vibrationManager.onSpecialPieceDetonated(SpecialPiece.HORIZONTAL_STRIPED)
    if (!soundEnabled) return
    playSweep(startFreq = 1400.0, endFreq = 260.0, durationMs = 160, volume = 0.85f)
  }

  fun playRainbow() {
    vibrationManager.onSpecialPieceDetonated(SpecialPiece.RAINBOW_PRISM)
    if (!soundEnabled) return
    coroutineScope.launch {
      playRainbowArpeggioSync()
    }
  }

  // ==========================================
  // LEVEL COMPLETIONS & FAILURES
  // ==========================================

  /**
   * Joyous, celebratory victory fanfare for completing a level.
   * Plays a triumphant, sweet chord progression in C Major followed by star award chimes.
   */
  fun playWin(stars: Int = 3) {
    vibrationManager.onLevelComplete(stars)
    if (!soundEnabled) return

    coroutineScope.launch {
      // Triumphant opening melody: C5 -> E5 -> G5 -> high C6!
      val victoryMelody = listOf(
        Pair(523.25, 90),  // C5
        Pair(659.25, 90),  // E5
        Pair(783.99, 100), // G5
        Pair(1046.5, 260), // C6!
        Pair(880.0, 90),   // A5
        Pair(1046.5, 360)  // C6 grand ring
      )

      for ((freq, dur) in victoryMelody) {
        playToneSync(frequency = freq, durationMs = dur, type = ToneType.SWEET_BELL, volume = 0.90f)
        delay((dur * 0.85).toLong())
      }

      delay(120)

      // Award individual star chimes
      for (star in 1..stars.coerceIn(1, 3)) {
        playStarAward(star)
        delay(160)
      }
    }
  }

  /**
   * Distinct celebratory chime for each star revealed on the victory screen.
   */
  fun playStarAward(starIndex: Int) {
    if (!soundEnabled) return
    coroutineScope.launch {
      when (starIndex) {
        1 -> {
          // Star 1: Bright bell (G5)
          playToneSync(frequency = 783.99, durationMs = 180, type = ToneType.SWEET_BELL, volume = 0.85f)
        }
        2 -> {
          // Star 2: Higher bright bell (C6)
          playToneSync(frequency = 1046.50, durationMs = 200, type = ToneType.SWEET_BELL, volume = 0.90f)
        }
        3 -> {
          // Star 3: Radiant double chime (E6 + high G6 shimmer)
          playToneSync(frequency = 1318.51, durationMs = 260, type = ToneType.SWEET_BELL, volume = 0.95f)
          playToneSync(frequency = 1567.98, durationMs = 260, type = ToneType.SPARKLE, volume = 0.80f)
        }
      }
    }
  }

  /**
   * Comical, lighthearted cartoon descending trombone ("womp-womp-womp-waaah")
   * for level failure, keeping gameplay cheerful and encouraging.
   */
  fun playLose() {
    vibrationManager.onLevelFailed()
    if (!soundEnabled) return

    coroutineScope.launch {
      val sadMotif = listOf(
        Pair(440.0, 160),  // A4
        Pair(415.3, 160),  // G#4
        Pair(392.0, 160),  // G4
        Pair(329.63, 340)  // E4 with vibrato
      )

      for ((freq, dur) in sadMotif) {
        playCartoonTromboneSync(frequency = freq, durationMs = dur)
        delay((dur * 0.88).toLong())
      }
    }
  }

  enum class HapticStyle { LIGHT, MEDIUM, HEAVY }

  fun performHaptic(style: HapticStyle) {
    when (style) {
      HapticStyle.LIGHT -> vibrationManager.onClick()
      HapticStyle.MEDIUM -> vibrationManager.onCandyMatched(2)
      HapticStyle.HEAVY -> vibrationManager.onCandyMatched(4)
    }
  }

  // ==========================================
  // BACKGROUND MUSIC (BGM) SYSTEM
  // ==========================================

  private fun initMusicTrack() {
    if (isMusicInitialized) return
    try {
      val sampleRate = 22050
      val buffer = generateCandyMelodyBuffer(sampleRate)

      val track = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(buffer.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      track.write(buffer, 0, buffer.size)
      track.setLoopPoints(0, buffer.size, -1) // Loop perpetually in hardware
      track.setVolume(0.28f) // Soft, pleasant background level

      musicTrack = track
      isMusicInitialized = true
    } catch (_: Exception) {}
  }

  fun startMusic() {
    if (!_musicEnabled) return
    try {
      musicTrack?.let { track ->
        if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
          track.play()
        }
      }
    } catch (_: Exception) {}
  }

  fun pauseMusic() {
    try {
      musicTrack?.let { track ->
        if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
          track.pause()
        }
      }
    } catch (_: Exception) {}
  }

  fun resumeMusic() {
    if (_musicEnabled) {
      startMusic()
    }
  }

  fun stopMusic() {
    try {
      musicTrack?.let { track ->
        if (track.playState != AudioTrack.PLAYSTATE_STOPPED) {
          track.stop()
        }
      }
    } catch (_: Exception) {}
  }

  fun release() {
    try {
      stopMusic()
      musicTrack?.release()
      musicTrack = null
      isMusicInitialized = false
    } catch (_: Exception) {}
  }

  /**
   * Synthesizes an 8-second seamless looping upbeat music box / vibraphone melody in C Major.
   * Runs with zero CPU usage during playback via hardware looped AudioTrack.
   */
  private fun generateCandyMelodyBuffer(sampleRate: Int): ShortArray {
    val totalSeconds = 8.0
    val totalSamples = (sampleRate * totalSeconds).toInt()
    val buffer = ShortArray(totalSamples)

    // Upbeat candy melody: 16 beats at 120 BPM (0.5s per beat, 0.25s per eighth note)
    data class MusicNote(val timeSec: Double, val durationSec: Double, val freq: Double, val isBass: Boolean = false)

    val notes = listOf(
      // Bar 1
      MusicNote(0.00, 0.22, 523.25), // C5
      MusicNote(0.25, 0.22, 659.25), // E5
      MusicNote(0.50, 0.22, 783.99), // G5
      MusicNote(0.75, 0.22, 880.00), // A5
      MusicNote(0.00, 0.45, 130.81, isBass = true), // C3 bass

      // Bar 2
      MusicNote(1.00, 0.38, 783.99), // G5
      MusicNote(1.50, 0.22, 659.25), // E5
      MusicNote(1.75, 0.22, 587.33), // D5
      MusicNote(1.00, 0.45, 196.00, isBass = true), // G3 bass

      // Bar 3
      MusicNote(2.00, 0.22, 523.25), // C5
      MusicNote(2.25, 0.22, 587.33), // D5
      MusicNote(2.50, 0.22, 659.25), // E5
      MusicNote(2.75, 0.22, 783.99), // G5
      MusicNote(2.00, 0.45, 174.61, isBass = true), // F3 bass

      // Bar 4
      MusicNote(3.00, 0.38, 659.25), // E5
      MusicNote(3.50, 0.38, 587.33), // D5
      MusicNote(3.00, 0.45, 196.00, isBass = true), // G3 bass

      // Bar 5
      MusicNote(4.00, 0.22, 659.25), // E5
      MusicNote(4.25, 0.22, 783.99), // G5
      MusicNote(4.50, 0.22, 880.00), // A5
      MusicNote(4.75, 0.22, 1046.50),// C6
      MusicNote(4.00, 0.45, 130.81, isBass = true), // C3 bass

      // Bar 6
      MusicNote(5.00, 0.38, 880.00), // A5
      MusicNote(5.50, 0.22, 783.99), // G5
      MusicNote(5.75, 0.22, 659.25), // E5
      MusicNote(5.00, 0.45, 174.61, isBass = true), // F3 bass

      // Bar 7
      MusicNote(6.00, 0.22, 698.46), // F5
      MusicNote(6.25, 0.22, 659.25), // E5
      MusicNote(6.50, 0.22, 587.33), // D5
      MusicNote(6.75, 0.22, 523.25), // C5
      MusicNote(6.00, 0.45, 196.00, isBass = true), // G3 bass

      // Bar 8 (Resolving sweetly to loop)
      MusicNote(7.00, 0.38, 587.33), // D5
      MusicNote(7.50, 0.42, 523.25), // C5
      MusicNote(7.00, 0.45, 130.81, isBass = true)  // C3 bass
    )

    // Render melody into buffer
    val floatBuffer = FloatArray(totalSamples)

    for (note in notes) {
      val startSample = (note.timeSec * sampleRate).toInt()
      val noteLengthSamples = (note.durationSec * sampleRate).toInt()

      for (i in 0 until noteLengthSamples) {
        val sampleIdx = (startSample + i) % totalSamples
        val t = i.toDouble() / sampleRate
        val env = exp(if (note.isBass) -4.0 * t else -5.0 * t)

        val sample = if (note.isBass) {
          // Warm bass tone
          val wave = sin(2.0 * PI * note.freq * t) + 0.3 * sin(4.0 * PI * note.freq * t)
          (wave * env * 0.22).toFloat()
        } else {
          // Sweet vibraphone/celesta tone (fundamental + 2nd harmonic sparkle)
          val wave = sin(2.0 * PI * note.freq * t) * 0.7 +
                     sin(4.0 * PI * note.freq * t) * 0.25 +
                     sin(6.0 * PI * note.freq * t) * 0.05
          (wave * env * 0.35).toFloat()
        }

        floatBuffer[sampleIdx] += sample
      }
    }

    // Convert float to 16-bit PCM short with smooth headroom
    for (i in 0 until totalSamples) {
      val clamped = floatBuffer[i].coerceIn(-0.95f, 0.95f)
      buffer[i] = (clamped * 28000).toInt().toShort()
    }

    return buffer
  }

  // ==========================================
  // PROCEDURAL AUDIO SYNTHESIZERS
  // ==========================================

  private enum class ToneType { SINE, SWEET_BELL, SPARKLE }

  private fun playTone(frequency: Double, durationMs: Int, type: ToneType = ToneType.SINE, volume: Float = 0.8f) {
    coroutineScope.launch {
      playToneSync(frequency, durationMs, type, volume)
    }
  }

  private fun playToneSync(frequency: Double, durationMs: Int, type: ToneType, volume: Float = 0.8f) {
    val sampleRate = 22050
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val time = i.toDouble() / sampleRate
      val raw = when (type) {
        ToneType.SINE -> sin(2.0 * PI * frequency * time)
        ToneType.SWEET_BELL -> {
          // Rich celesta / bell harmonic layering (fundamental + octave + sweet 3rd harmonic)
          sin(2.0 * PI * frequency * time) * 0.65 +
          sin(4.0 * PI * frequency * time) * 0.25 +
          sin(6.0 * PI * frequency * time) * 0.10
        }
        ToneType.SPARKLE -> {
          // Shimmering high sparkle tone
          sin(2.0 * PI * frequency * time) * 0.55 +
          sin(3.0 * PI * frequency * time) * 0.35 +
          sin(8.0 * PI * frequency * time) * 0.10
        }
      }

      val envelope: Double = when (type) {
        ToneType.SINE -> {
          val attack = (i.toDouble() / (sampleRate * 0.01)).coerceIn(0.0, 1.0)
          val release = ((numSamples - i).toDouble() / (sampleRate * 0.01)).coerceIn(0.0, 1.0)
          attack * release
        }
        ToneType.SWEET_BELL -> exp(-4.5 * time)
        ToneType.SPARKLE -> exp(-6.5 * time)
      }
      buffer[i] = (raw * envelope * 27000.0 * volume).toInt().coerceIn(-32767, 32767).toShort()
    }
    playRawBuffer(buffer, sampleRate)
  }

  private fun playSweep(startFreq: Double, endFreq: Double, durationMs: Int, volume: Float = 0.8f) {
    coroutineScope.launch {
      playSweepSync(startFreq, endFreq, durationMs, volume)
    }
  }

  private fun playSweepSync(startFreq: Double, endFreq: Double, durationMs: Int, volume: Float = 0.8f) {
    val sampleRate = 22050
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val fraction = i.toDouble() / numSamples
      val currentFreq = startFreq + (endFreq - startFreq) * fraction
      val time = i.toDouble() / sampleRate
      val raw = sin(2.0 * PI * currentFreq * time)
      val envelope = sin(PI * fraction)
      buffer[i] = (raw * envelope * 27000 * volume).toInt().coerceIn(-32767, 32767).toShort()
    }
    playRawBuffer(buffer, sampleRate)
  }

  /**
   * Juicy candy pop click: crisp, punchy initial pop.
   */
  private fun playJuicyPopSync(durationMs: Int = 28) {
    val sampleRate = 22050
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val fraction = i.toDouble() / numSamples
      val currentFreq = 780.0 - (560.0 * fraction) // pitch drop
      val time = i.toDouble() / sampleRate
      val raw = sin(2.0 * PI * currentFreq * time)
      val envelope = exp(-18.0 * time)
      buffer[i] = (raw * envelope * 26000).toInt().coerceIn(-32767, 32767).toShort()
    }
    playRawBuffer(buffer, sampleRate)
  }

  /**
   * Cartoon mallet bonk: heavy punch + pitch bend downward.
   */
  private fun playCartoonBonkSync() {
    val sampleRate = 22050
    val durationMs = 240
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val time = i.toDouble() / sampleRate
      val fraction = i.toDouble() / numSamples
      // Pitch sweeps down from 380Hz to 85Hz
      val currentFreq = 380.0 * exp(-7.0 * fraction) + 85.0
      val wave = sin(2.0 * PI * currentFreq * time) * 0.7 +
                 sin(4.0 * PI * currentFreq * time) * 0.3
      val envelope = exp(-6.0 * time)
      buffer[i] = (wave * envelope * 30000).toInt().coerceIn(-32767, 32767).toShort()
    }
    playRawBuffer(buffer, sampleRate)
  }

  /**
   * Rocket whoosh: accelerating frequency sweep with subtle turbulence and pop.
   */
  private fun playRocketWhooshSync() {
    val sampleRate = 22050
    val durationMs = 260
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val time = i.toDouble() / sampleRate
      val fraction = i.toDouble() / numSamples
      val freq = 280.0 + 1200.0 * (fraction * fraction) // exponential rising pitch
      val noise = (Math.random() * 2.0 - 1.0) * 0.25
      val wave = sin(2.0 * PI * freq * time) * 0.75 + noise
      val envelope = sin(PI * fraction)
      buffer[i] = (wave * envelope * 28000).toInt().coerceIn(-32767, 32767).toShort()
    }
    playRawBuffer(buffer, sampleRate)
  }

  /**
   * Rainbow celestial harp arpeggio: shimmering 7-note cascade.
   */
  private suspend fun playRainbowArpeggioSync() {
    val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98, 2093.00)
    for (note in notes) {
      playToneSync(frequency = note, durationMs = 60, type = ToneType.SWEET_BELL, volume = 0.85f)
      delay(32)
    }
  }

  /**
   * Whimsical whirlwind flurry: rapid oscillating frequency trill.
   */
  private fun playWhirlwindFlurrySync() {
    val sampleRate = 22050
    val durationMs = 220
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val time = i.toDouble() / sampleRate
      val fraction = i.toDouble() / numSamples
      val flutter = sin(2.0 * PI * 28.0 * time) * 180.0 // 28 Hz vibrato
      val freq = 550.0 + flutter + (fraction * 350.0)
      val wave = sin(2.0 * PI * freq * time)
      val envelope = sin(PI * fraction)
      buffer[i] = (wave * envelope * 27000).toInt().coerceIn(-32767, 32767).toShort()
    }
    playRawBuffer(buffer, sampleRate)
  }

  /**
   * Electric lightning zap: frequency-modulated laser sweep with crackle.
   */
  private fun playElectricZapSync() {
    val sampleRate = 22050
    val durationMs = 180
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val time = i.toDouble() / sampleRate
      val fraction = i.toDouble() / numSamples
      val mod = sin(2.0 * PI * 140.0 * time) * 300.0 // FM modulation
      val carrier = 1400.0 - (1050.0 * fraction) + mod
      val noise = (Math.random() * 2.0 - 1.0) * 0.20
      val wave = sin(2.0 * PI * carrier * time) * 0.8 + noise
      val envelope = exp(-7.0 * time)
      buffer[i] = (wave * envelope * 29000).toInt().coerceIn(-32767, 32767).toShort()
    }
    playRawBuffer(buffer, sampleRate)
  }

  /**
   * Candy explosion: deep warm punch + bursting sugar crackles.
   */
  private fun playCandyExplosionSync() {
    val sampleRate = 22050
    val durationMs = 280
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(numSamples)
    var filteredNoise = 0.0
    for (i in 0 until numSamples) {
      val time = i.toDouble() / sampleRate
      val white = (Math.random() * 2.0 - 1.0)
      filteredNoise = filteredNoise * 0.82 + white * 0.18 // Low-pass punch
      val subBass = sin(2.0 * PI * (90.0 * exp(-8.0 * time)) * time) * 0.5
      val wave = filteredNoise * 0.65 + subBass
      val envelope = exp(-7.5 * time)
      buffer[i] = (wave * envelope * 31000).toInt().coerceIn(-32767, 32767).toShort()
    }
    playRawBuffer(buffer, sampleRate)
  }

  /**
   * Cartoon descending trombone for level fail.
   */
  private fun playCartoonTromboneSync(frequency: Double, durationMs: Int) {
    val sampleRate = 22050
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val time = i.toDouble() / sampleRate
      val fraction = i.toDouble() / numSamples
      // Gentle cartoon vibrato
      val vibrato = sin(2.0 * PI * 5.5 * time) * 6.0
      val freq = frequency + vibrato - (fraction * 12.0)
      val wave = sin(2.0 * PI * freq * time) * 0.70 +
                 sin(4.0 * PI * freq * time) * 0.22 +
                 sin(6.0 * PI * freq * time) * 0.08
      val envelope = sin(PI * fraction)
      buffer[i] = (wave * envelope * 26000).toInt().coerceIn(-32767, 32767).toShort()
    }
    playRawBuffer(buffer, sampleRate)
  }

  private fun playNoiseBurst(durationMs: Int, volume: Float = 0.7f) {
    coroutineScope.launch {
      val sampleRate = 22050
      val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
      val buffer = ShortArray(numSamples)
      var lastRandom = 0.0
      for (i in 0 until numSamples) {
        val time = i.toDouble() / sampleRate
        val white = (Math.random() * 2.0 - 1.0)
        lastRandom = lastRandom * 0.85 + white * 0.15
        val envelope = exp(-8.0 * time)
        buffer[i] = (lastRandom * envelope * 28000 * volume).toInt().coerceIn(-32767, 32767).toShort()
      }
      playRawBuffer(buffer, sampleRate)
    }
  }

  private fun playRawBuffer(buffer: ShortArray, sampleRate: Int) {
    try {
      val track = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(buffer.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      track.write(buffer, 0, buffer.size)
      track.play()

      coroutineScope.launch {
        delay((buffer.size.toFloat() / sampleRate * 1000).toLong() + 60)
        try {
          track.stop()
          track.release()
        } catch (_: Exception) {}
      }
    } catch (_: Exception) {}
  }
}
