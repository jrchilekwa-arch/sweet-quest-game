package com.example

import com.example.sweetquest.data.LevelsData
import com.example.sweetquest.engine.Match3Engine
import com.example.sweetquest.model.BoosterType
import com.example.sweetquest.model.LevelConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Match3EngineTest {

  private lateinit var level1Config: LevelConfig
  private lateinit var engine: Match3Engine

  @Before
  fun setUp() {
    level1Config = LevelsData.getLevel(1)
    engine = Match3Engine(level1Config)
  }

  @Test
  fun testBoardDimensions() {
    assertEquals(8, engine.board.size)
    for (row in 0 until 8) {
      assertEquals(8, engine.board[row].size)
    }
  }

  @Test
  fun testInitialState() {
    assertEquals(level1Config.movesLimit, engine.movesRemaining)
    assertEquals(0, engine.currentScore)
    assertEquals(0, engine.calculateStars())
    assertFalse(engine.isLevelWon)
    assertFalse(engine.isLevelLost)
  }

  @Test
  fun testNonAdjacentSwapRejected() {
    val canSwap = engine.canSwap(0, 0, 0, 2)
    assertFalse(canSwap)
    val result = engine.executeSwap(0, 0, 0, 2)
    assertEquals(null, result)
  }

  @Test
  fun testStarsCalculation() {
    assertEquals(0, engine.calculateStars())

    engine.currentScore = level1Config.star1Score
    assertEquals(1, engine.calculateStars())

    engine.currentScore = level1Config.star2Score
    assertEquals(2, engine.calculateStars())

    engine.currentScore = level1Config.star3Score
    assertEquals(3, engine.calculateStars())
  }

  @Test
  fun testHammerBoosterSmash() {
    val targetTile = engine.board[3][3]
    assertNotNull(targetTile)

    val result = engine.useBooster(BoosterType.HAMMER, 3, 3)
    assertTrue(result.clearedCount > 0)
    assertTrue(engine.currentScore > 0)
  }

  @Test
  fun testRocketBoosterCrossClear() {
    val result = engine.useBooster(BoosterType.ROCKET, 4, 4)
    assertTrue(result.clearedCount >= 10)
    assertTrue(engine.currentScore > 0)
  }

  @Test
  fun testShuffleBooster() {
    val result = engine.useBooster(BoosterType.SHUFFLE, 0, 0)
    // Board should still have 8x8 tiles
    for (r in 0 until 8) {
      for (c in 0 until 8) {
        assertNotNull(engine.board[r][c])
      }
    }
  }

  @Test
  fun testAll30LevelsConfigured() {
    for (i in 1..30) {
      val config = LevelsData.getLevel(i)
      assertEquals(i, config.levelNumber)
      assertTrue(config.movesLimit >= 15)
      assertTrue(config.star1Score > 0)
      assertTrue(config.star2Score > config.star1Score)
      assertTrue(config.star3Score > config.star2Score)
    }
  }

  @Test
  fun testCascadingPhysicsGravityAndRefill() {
    // Clear 3 tiles in row 7 (bottom row)
    engine.board[7][0] = null
    engine.board[7][1] = null
    engine.board[7][2] = null

    // Record tile that was above at row 6
    val tileAbove = engine.board[6][0]
    assertNotNull(tileAbove)

    // Apply gravity
    val grav = engine.applyGravityOnly()
    assertTrue("Movements should be recorded for falling tiles", grav.movements.isNotEmpty())

    // The tile originally at row 6, col 0 should now be at row 7, col 0
    val droppedTile = engine.board[7][0]
    assertNotNull(droppedTile)
    assertEquals(7, droppedTile?.row)
    assertEquals(0, droppedTile?.col)

    // Top slots should now have nulls before refill
    val refill = engine.refillEmptySlots()
    assertTrue("Refill should populate newly generated candies", refill.newTiles.isNotEmpty())

    // After refill, grid should be completely populated
    for (r in 0 until 8) {
      for (c in 0 until 8) {
        assertNotNull("Cell ($r, $c) must be refilled", engine.board[r][c])
      }
    }
  }

  @Test
  fun testStepClearNextMatches() {
    // Create an explicit 3-in-a-row match
    val redColor = com.example.sweetquest.model.CandyColor.RED
    engine.board[5][0] = com.example.sweetquest.model.Tile(5, 0, redColor)
    engine.board[5][1] = com.example.sweetquest.model.Tile(5, 1, redColor)
    engine.board[5][2] = com.example.sweetquest.model.Tile(5, 2, redColor)

    val step = engine.clearNextMatchesStep(combo = 1)
    assertNotNull("Should detect and clear matched candies step", step)
    assertTrue("Cleared positions must contain matched coordinates", step!!.clearedPositions.contains(Pair(5, 0)))
    assertTrue("Score should increase with cleared candies", step.scoreGained > 0)

    engine.finalizeClearStep(step)

    // Matched positions should now be empty (null) waiting for gravity
    assertEquals(null, engine.board[5][0])
    assertEquals(null, engine.board[5][1])
    assertEquals(null, engine.board[5][2])

    // Apply gravity
    val grav = engine.applyGravityOnly()
    assertTrue("Upper tiles should fall down", grav.movements.isNotEmpty())

    // Refill empty slots
    val refill = engine.refillEmptySlots()
    assertTrue("New candies should refill open slots", refill.newTiles.isNotEmpty())

    for (r in 0 until 8) {
      for (c in 0 until 8) {
        assertNotNull("Cell ($r, $c) must not be null after refill", engine.board[r][c])
      }
    }
  }
}
