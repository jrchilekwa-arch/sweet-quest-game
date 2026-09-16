package com.example.sweetquest.engine

import com.example.sweetquest.model.BoosterType
import com.example.sweetquest.model.CandyColor
import com.example.sweetquest.model.LevelConfig
import com.example.sweetquest.model.LevelObjective
import com.example.sweetquest.model.ObjectiveType
import com.example.sweetquest.model.ParticleEffect
import com.example.sweetquest.model.ScorePopup
import com.example.sweetquest.model.SpecialPiece
import com.example.sweetquest.model.Tile
import com.example.sweetquest.model.TileType
import kotlin.math.abs
import kotlin.random.Random

data class MatchResult(
  val clearedCount: Int,
  val scoreGained: Int,
  val comboCount: Int,
  val specialPiecesCreated: List<Tile>,
  val specialPiecesActivated: List<Tile>,
  val clearedColors: Map<CandyColor, Int>,
  val jelliesCleared: Int,
  val blockersBroken: Int,
  val ingredientsCollected: Int,
  val scorePopups: List<ScorePopup>,
  val particles: List<ParticleEffect>
)

data class StepClearResult(
  val clearedPositions: Set<Pair<Int, Int>>,
  val newlyCreatedSpecials: List<Tile>,
  val specialsActivated: List<Tile>,
  val scoreGained: Int,
  val popups: List<ScorePopup>,
  val jelliesCleared: Int,
  val blockersBroken: Int,
  val ingredientsCollected: Int,
  val clearedColors: Map<CandyColor, Int>
)

data class TileMovement(
  val fromRow: Int,
  val fromCol: Int,
  val toRow: Int,
  val toCol: Int,
  val tile: Tile
)

data class StepGravityResult(
  val movements: List<TileMovement>,
  val ingredientsDropped: Int
)

data class StepRefillResult(
  val newTiles: List<Tile>
)

class Match3Engine(val levelConfig: LevelConfig) {

  var board: Array<Array<Tile?>> = Array(8) { row ->
    Array(8) { col ->
      Tile(row = row, col = col, color = null)
    }
  }

  var movesRemaining: Int = levelConfig.movesLimit
  var currentScore: Int = 0
  var currentCombo: Int = 0

  // Objective counters
  var jelliesRemaining: Int = 0
  var blockersRemaining: Int = 0
  var ingredientsCollected: Int = 0
  val candiesCollected: MutableMap<CandyColor, Int> = mutableMapOf()

  var isLevelWon: Boolean = false
  var isLevelLost: Boolean = false

  init {
    initBoard()
  }

  fun initBoard() {
    board = Array(8) { row -> Array(8) { col -> null } }
    jelliesRemaining = 0
    blockersRemaining = 0
    ingredientsCollected = 0
    candiesCollected.clear()

    // 1. Mark holes
    for (r in 0..7) {
      for (c in 0..7) {
        val pos = Pair(r, c)
        if (levelConfig.blockedHoles.contains(pos)) {
          board[r][c] = null
          continue
        }

        var tileType = TileType.NORMAL
        var color: CandyColor? = null

        if (levelConfig.initialBlockers.contains(pos)) {
          tileType = TileType.BLOCKER
          blockersRemaining++
        } else if (levelConfig.initialCages.contains(pos)) {
          tileType = TileType.CAGE
          color = getRandomColorExcept(r, c)
        } else if (levelConfig.initialIngredients.contains(pos)) {
          tileType = TileType.INGREDIENT
        } else {
          color = getRandomColorExcept(r, c)
        }

        if (levelConfig.initialJellies.contains(pos)) {
          tileType = if (tileType == TileType.NORMAL) TileType.JELLY_1 else tileType
          jelliesRemaining++
        }

        board[r][c] = Tile(
          row = r,
          col = c,
          color = color,
          tileType = tileType,
          special = SpecialPiece.NONE
        )
      }
    }
  }

  private fun getRandomColorExcept(row: Int, col: Int): CandyColor {
    val prohibited = mutableSetOf<CandyColor>()
    // Check left 2
    if (col >= 2) {
      val c1 = board[row][col - 1]?.color
      val c2 = board[row][col - 2]?.color
      if (c1 != null && c1 == c2) {
        prohibited.add(c1)
      }
    }
    // Check up 2
    if (row >= 2) {
      val c1 = board[row - 1][col]?.color
      val c2 = board[row - 2][col]?.color
      if (c1 != null && c1 == c2) {
        prohibited.add(c1)
      }
    }
    val available = CandyColor.entries.filter { !prohibited.contains(it) }
    return if (available.isNotEmpty()) available.random() else CandyColor.entries.random()
  }

  fun canSwap(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
    if (abs(r1 - r2) + abs(c1 - c2) != 1) return false
    val t1 = board[r1][c1] ?: return false
    val t2 = board[r2][c2] ?: return false

    // Cannot swap blockers or holes
    if (t1.tileType == TileType.BLOCKER || t2.tileType == TileType.BLOCKER) return false
    if (t1.tileType == TileType.CAGE || t2.tileType == TileType.CAGE) return false

    // Special combo swaps are always valid
    if (t1.special != SpecialPiece.NONE && t2.special != SpecialPiece.NONE) return true
    if (t1.special == SpecialPiece.RAINBOW_PRISM || t2.special == SpecialPiece.RAINBOW_PRISM) return true

    // Check if swap would result in a match
    swapTiles(r1, c1, r2, c2)
    val hasMatches = findMatches().isNotEmpty()
    // swap back
    swapTiles(r1, c1, r2, c2)
    return hasMatches
  }

  fun swapTiles(r1: Int, c1: Int, r2: Int, c2: Int) {
    val t1 = board[r1][c1]
    val t2 = board[r2][c2]
    board[r1][c1] = t2?.copy(row = r1, col = c1)
    board[r2][c2] = t1?.copy(row = r2, col = c2)
  }

  fun executeSwap(r1: Int, c1: Int, r2: Int, c2: Int): MatchResult? {
    if (!canSwap(r1, c1, r2, c2)) return null

    movesRemaining--
    val t1 = board[r1][c1]!!
    val t2 = board[r2][c2]!!

    // Check special combo combinations
    if (t1.special != SpecialPiece.NONE || t2.special != SpecialPiece.NONE) {
      val comboResult = handleSpecialCombination(r1, c1, r2, c2, t1, t2)
      if (comboResult != null) {
        checkGameStatus()
        return comboResult
      }
    }

    // Normal swap
    swapTiles(r1, c1, r2, c2)
    currentCombo = 1
    val result = processMatchesAndCascades(lastSwapPos = Pair(r2, c2))
    checkGameStatus()
    return result
  }

  private fun handleSpecialCombination(
    r1: Int, c1: Int, r2: Int, c2: Int,
    t1: Tile, t2: Tile
  ): MatchResult? {
    val toClear = mutableSetOf<Pair<Int, Int>>()
    val specialActivated = mutableListOf<Tile>()
    val popups = mutableListOf<ScorePopup>()
    val particles = mutableListOf<ParticleEffect>()

    // Case 1: Rainbow + Rainbow -> Clears ALL movable tiles!
    if (t1.special == SpecialPiece.RAINBOW_PRISM && t2.special == SpecialPiece.RAINBOW_PRISM) {
      for (r in 0..7) {
        for (c in 0..7) {
          if (board[r][c]?.tileType != TileType.BLOCKER) {
            toClear.add(Pair(r, c))
          }
        }
      }
      specialActivated.add(t1)
      specialActivated.add(t2)
      popups.add(ScorePopup(System.currentTimeMillis(), "DOUBLE RAINBOW!!", r2, c2, 0xFFFF00FF))
    }
    // Case 2: Rainbow + Striped -> Convert all candies of striped color to striped and trigger them!
    else if (t1.special == SpecialPiece.RAINBOW_PRISM && (t2.special == SpecialPiece.HORIZONTAL_STRIPED || t2.special == SpecialPiece.VERTICAL_STRIPED)) {
      val targetColor = t2.color ?: CandyColor.entries.random()
      for (r in 0..7) {
        for (c in 0..7) {
          val tile = board[r][c]
          if (tile?.color == targetColor) {
            val orientation = if (Random.nextBoolean()) SpecialPiece.HORIZONTAL_STRIPED else SpecialPiece.VERTICAL_STRIPED
            board[r][c] = tile.copy(special = orientation)
            activateSpecialAt(r, c, toClear, specialActivated)
          }
        }
      }
      toClear.add(Pair(r1, c1))
      toClear.add(Pair(r2, c2))
      popups.add(ScorePopup(System.currentTimeMillis(), "RAINBOW BLAST!!", r2, c2, 0xFF00E5FF))
    }
    // Mirror of Case 2
    else if (t2.special == SpecialPiece.RAINBOW_PRISM && (t1.special == SpecialPiece.HORIZONTAL_STRIPED || t1.special == SpecialPiece.VERTICAL_STRIPED)) {
      val targetColor = t1.color ?: CandyColor.entries.random()
      for (r in 0..7) {
        for (c in 0..7) {
          val tile = board[r][c]
          if (tile?.color == targetColor) {
            val orientation = if (Random.nextBoolean()) SpecialPiece.HORIZONTAL_STRIPED else SpecialPiece.VERTICAL_STRIPED
            board[r][c] = tile.copy(special = orientation)
            activateSpecialAt(r, c, toClear, specialActivated)
          }
        }
      }
      toClear.add(Pair(r1, c1))
      toClear.add(Pair(r2, c2))
      popups.add(ScorePopup(System.currentTimeMillis(), "RAINBOW BLAST!!", r2, c2, 0xFF00E5FF))
    }
    // Case 3: Rainbow + Wrapped -> Converts all candies of wrapped color into wrapped candies and detonates!
    else if (t1.special == SpecialPiece.RAINBOW_PRISM && t2.special == SpecialPiece.EXPLOSIVE_WRAPPED) {
      val targetColor = t2.color ?: CandyColor.entries.random()
      for (r in 0..7) {
        for (c in 0..7) {
          val tile = board[r][c]
          if (tile?.color == targetColor) {
            board[r][c] = tile.copy(special = SpecialPiece.EXPLOSIVE_WRAPPED)
            activateSpecialAt(r, c, toClear, specialActivated)
          }
        }
      }
      toClear.add(Pair(r1, c1))
      toClear.add(Pair(r2, c2))
      popups.add(ScorePopup(System.currentTimeMillis(), "SWEET DETONATION!!", r2, c2, 0xFFFF5722))
    }
    // Mirror of Case 3
    else if (t2.special == SpecialPiece.RAINBOW_PRISM && t1.special == SpecialPiece.EXPLOSIVE_WRAPPED) {
      val targetColor = t1.color ?: CandyColor.entries.random()
      for (r in 0..7) {
        for (c in 0..7) {
          val tile = board[r][c]
          if (tile?.color == targetColor) {
            board[r][c] = tile.copy(special = SpecialPiece.EXPLOSIVE_WRAPPED)
            activateSpecialAt(r, c, toClear, specialActivated)
          }
        }
      }
      toClear.add(Pair(r1, c1))
      toClear.add(Pair(r2, c2))
      popups.add(ScorePopup(System.currentTimeMillis(), "SWEET DETONATION!!", r2, c2, 0xFFFF5722))
    }
    // Case 4: Rainbow + Normal Candy -> Clears all candies of that color!
    else if (t1.special == SpecialPiece.RAINBOW_PRISM && t2.color != null) {
      val targetColor = t2.color
      for (r in 0..7) {
        for (c in 0..7) {
          if (board[r][c]?.color == targetColor) {
            toClear.add(Pair(r, c))
          }
        }
      }
      toClear.add(Pair(r1, c1))
      specialActivated.add(t1)
      popups.add(ScorePopup(System.currentTimeMillis(), "COLOR CRUSH!!", r2, c2, 0xFFFFEB3B))
    }
    // Mirror of Case 4
    else if (t2.special == SpecialPiece.RAINBOW_PRISM && t1.color != null) {
      val targetColor = t1.color
      for (r in 0..7) {
        for (c in 0..7) {
          if (board[r][c]?.color == targetColor) {
            toClear.add(Pair(r, c))
          }
        }
      }
      toClear.add(Pair(r2, c2))
      specialActivated.add(t2)
      popups.add(ScorePopup(System.currentTimeMillis(), "COLOR CRUSH!!", r1, c1, 0xFFFFEB3B))
    }
    // Case 5: Striped + Striped -> Clears row and column!
    else if ((t1.special == SpecialPiece.HORIZONTAL_STRIPED || t1.special == SpecialPiece.VERTICAL_STRIPED) &&
      (t2.special == SpecialPiece.HORIZONTAL_STRIPED || t2.special == SpecialPiece.VERTICAL_STRIPED)) {
      for (c in 0..7) toClear.add(Pair(r2, c))
      for (r in 0..7) toClear.add(Pair(r, c2))
      specialActivated.add(t1)
      specialActivated.add(t2)
      popups.add(ScorePopup(System.currentTimeMillis(), "CROSS LASER!!", r2, c2, 0xFFFF4081))
    }
    // Case 6: Striped + Wrapped -> Giant 3-row x 3-column cross laser!
    else if ((t1.special == SpecialPiece.EXPLOSIVE_WRAPPED && (t2.special == SpecialPiece.HORIZONTAL_STRIPED || t2.special == SpecialPiece.VERTICAL_STRIPED)) ||
      (t2.special == SpecialPiece.EXPLOSIVE_WRAPPED && (t1.special == SpecialPiece.HORIZONTAL_STRIPED || t1.special == SpecialPiece.VERTICAL_STRIPED))) {
      for (dr in -1..1) {
        val r = r2 + dr
        if (r in 0..7) {
          for (c in 0..7) toClear.add(Pair(r, c))
        }
      }
      for (dc in -1..1) {
        val c = c2 + dc
        if (c in 0..7) {
          for (r in 0..7) toClear.add(Pair(r, c))
        }
      }
      specialActivated.add(t1)
      specialActivated.add(t2)
      popups.add(ScorePopup(System.currentTimeMillis(), "MEGA LASER!!", r2, c2, 0xFFFFD700))
    }
    // Case 7: Wrapped + Wrapped -> Giant 5x5 blast!
    else if (t1.special == SpecialPiece.EXPLOSIVE_WRAPPED && t2.special == SpecialPiece.EXPLOSIVE_WRAPPED) {
      for (dr in -2..2) {
        for (dc in -2..2) {
          val r = r2 + dr
          val c = c2 + dc
          if (r in 0..7 && c in 0..7) {
            toClear.add(Pair(r, c))
          }
        }
      }
      specialActivated.add(t1)
      specialActivated.add(t2)
      popups.add(ScorePopup(System.currentTimeMillis(), "MEGA BOMB!!", r2, c2, 0xFFFF3D00))
    } else {
      return null
    }

    // Execute clearing of toClear
    var scoreGained = 0
    var clearedCount = 0
    var jelliesCleared = 0
    var blockersBroken = 0
    var ingredientsCollected = 0
    val clearedColors = mutableMapOf<CandyColor, Int>()

    toClear.forEach { (r, c) ->
      val tile = board[r][c] ?: return@forEach
      if (tile.tileType == TileType.BLOCKER) {
        blockersBroken++
        blockersRemaining = maxOf(0, blockersRemaining - 1)
        board[r][c] = Tile(r, c, getRandomColorExcept(r, c))
      } else {
        if (tile.tileType == TileType.JELLY_1 || tile.tileType == TileType.JELLY_2) {
          jelliesCleared++
          jelliesRemaining = maxOf(0, jelliesRemaining - 1)
        }
        if (tile.tileType == TileType.INGREDIENT) {
          ingredientsCollected++
          this.ingredientsCollected++
        }
        if (tile.color != null) {
          clearedColors[tile.color] = (clearedColors[tile.color] ?: 0) + 1
          candiesCollected[tile.color] = (candiesCollected[tile.color] ?: 0) + 1
        }
        clearedCount++
        scoreGained += 60
        board[r][c] = null
      }
    }

    currentScore += scoreGained
    applyGravityAndRefill()

    // Follow-up cascades
    val cascade = processMatchesAndCascades()
    return MatchResult(
      clearedCount = clearedCount + cascade.clearedCount,
      scoreGained = scoreGained + cascade.scoreGained,
      comboCount = 2 + cascade.comboCount,
      specialPiecesCreated = emptyList(),
      specialPiecesActivated = specialActivated + cascade.specialPiecesActivated,
      clearedColors = mergeColorMaps(clearedColors, cascade.clearedColors),
      jelliesCleared = jelliesCleared + cascade.jelliesCleared,
      blockersBroken = blockersBroken + cascade.blockersBroken,
      ingredientsCollected = ingredientsCollected + cascade.ingredientsCollected,
      scorePopups = popups + cascade.scorePopups,
      particles = particles + cascade.particles
    )
  }

  private fun activateSpecialAt(r: Int, c: Int, toClear: MutableSet<Pair<Int, Int>>, specialActivated: MutableList<Tile>) {
    val tile = board[r][c] ?: return
    specialActivated.add(tile)
    when (tile.special) {
      SpecialPiece.HORIZONTAL_STRIPED -> {
        for (col in 0..7) toClear.add(Pair(r, col))
      }
      SpecialPiece.VERTICAL_STRIPED -> {
        for (row in 0..7) toClear.add(Pair(row, c))
      }
      SpecialPiece.EXPLOSIVE_WRAPPED -> {
        for (dr in -1..1) {
          for (dc in -1..1) {
            val nr = r + dr
            val nc = c + dc
            if (nr in 0..7 && nc in 0..7) toClear.add(Pair(nr, nc))
          }
        }
      }
      SpecialPiece.RAINBOW_PRISM -> {
        val targetColor = CandyColor.entries.random()
        for (row in 0..7) {
          for (col in 0..7) {
            if (board[row][col]?.color == targetColor) {
              toClear.add(Pair(row, col))
            }
          }
        }
      }
      SpecialPiece.NONE -> {}
    }
  }

  fun clearNextMatchesStep(
    lastSwapPos: Pair<Int, Int>? = null,
    combo: Int = currentCombo
  ): StepClearResult? {
    val matchGroups = findMatches()
    if (matchGroups.isEmpty()) return null

    val toClear = mutableSetOf<Pair<Int, Int>>()
    val newlyCreatedSpecials = mutableListOf<Tile>()
    val specialsActivated = mutableListOf<Tile>()
    val clearedColors = mutableMapOf<CandyColor, Int>()
    val popups = mutableListOf<ScorePopup>()
    var jelliesCleared = 0
    var blockersBroken = 0

    for (group in matchGroups) {
      val count = group.size
      val color = group.first().color ?: continue
      toClear.addAll(group.map { Pair(it.row, it.col) })

      // Check special creations
      if (count >= 5) {
        val isLine = group.all { it.row == group.first().row } || group.all { it.col == group.first().col }
        val spawnPos = if (lastSwapPos != null && group.any { it.row == lastSwapPos.first && it.col == lastSwapPos.second }) {
          lastSwapPos
        } else {
          Pair(group[count / 2].row, group[count / 2].col)
        }

        if (isLine) {
          // Rainbow Prism!
          val newSpecial = Tile(
            row = spawnPos.first,
            col = spawnPos.second,
            color = null,
            special = SpecialPiece.RAINBOW_PRISM
          )
          newlyCreatedSpecials.add(newSpecial)
          toClear.remove(spawnPos)
          popups.add(ScorePopup(System.currentTimeMillis(), "RAINBOW!", spawnPos.first, spawnPos.second, 0xFFFF00FF))
        } else {
          // T or L shape -> Wrapped explosive piece!
          val newSpecial = Tile(
            row = spawnPos.first,
            col = spawnPos.second,
            color = color,
            special = SpecialPiece.EXPLOSIVE_WRAPPED
          )
          newlyCreatedSpecials.add(newSpecial)
          toClear.remove(spawnPos)
          popups.add(ScorePopup(System.currentTimeMillis(), "WRAPPED BOMB!", spawnPos.first, spawnPos.second, 0xFFFF5722))
        }
      } else if (count == 4) {
        val isHorizontal = group.all { it.row == group.first().row }
        val spawnPos = if (lastSwapPos != null && group.any { it.row == lastSwapPos.first && it.col == lastSwapPos.second }) {
          lastSwapPos
        } else {
          Pair(group[count / 2].row, group[count / 2].col)
        }
        val orientation = if (isHorizontal) SpecialPiece.VERTICAL_STRIPED else SpecialPiece.HORIZONTAL_STRIPED
        val newSpecial = Tile(
          row = spawnPos.first,
          col = spawnPos.second,
          color = color,
          special = orientation
        )
        newlyCreatedSpecials.add(newSpecial)
        toClear.remove(spawnPos)
        popups.add(ScorePopup(System.currentTimeMillis(), "STRIPED!", spawnPos.first, spawnPos.second, 0xFF00E5FF))
      }
    }

    // Check for any special pieces within toClear to activate their powers
    val specialsInClear = toClear.mapNotNull { board[it.first][it.second] }
      .filter { it.special != SpecialPiece.NONE }
    for (sp in specialsInClear) {
      activateSpecialAt(sp.row, sp.col, toClear, specialsActivated)
    }

    // Check adjacent blockers & cages to break them
    val adjacentBlockers = mutableSetOf<Pair<Int, Int>>()
    for ((r, c) in toClear) {
      val neighbors = listOf(Pair(r - 1, c), Pair(r + 1, c), Pair(r, c - 1), Pair(r, c + 1))
      for ((nr, nc) in neighbors) {
        if (nr in 0..7 && nc in 0..7) {
          val neighbor = board[nr][nc]
          if (neighbor?.tileType == TileType.BLOCKER) {
            adjacentBlockers.add(Pair(nr, nc))
          } else if (neighbor?.tileType == TileType.CAGE) {
            // Free from cage!
            board[nr][nc] = neighbor.copy(tileType = TileType.NORMAL)
            popups.add(ScorePopup(System.currentTimeMillis(), "FREED!", nr, nc, 0xFF4CAF50))
          }
        }
      }
    }

    for ((br, bc) in adjacentBlockers) {
      blockersBroken++
      blockersRemaining = maxOf(0, blockersRemaining - 1)
      board[br][bc] = Tile(br, bc, getRandomColorExcept(br, bc))
      popups.add(ScorePopup(System.currentTimeMillis(), "CRUNCH!", br, bc, 0xFF8D6E63))
    }

    // Clear the tiles
    var stepScore = 0
    for ((r, c) in toClear) {
      val tile = board[r][c] ?: continue

      if (tile.tileType == TileType.JELLY_1 || tile.tileType == TileType.JELLY_2) {
        jelliesCleared++
        jelliesRemaining = maxOf(0, jelliesRemaining - 1)
      }

      if (tile.color != null) {
        clearedColors[tile.color] = (clearedColors[tile.color] ?: 0) + 1
        candiesCollected[tile.color] = (candiesCollected[tile.color] ?: 0) + 1
      }
      stepScore += 60 * maxOf(1, combo)
    }

    currentScore += stepScore

    // Combo praise popups
    if (combo >= 2) {
      val praise = when (combo) {
        2 -> "Sweet! x2"
        3 -> "Tasty! x3"
        4 -> "Sugar Rush! x4"
        5 -> "Divine! x5"
        else -> "Incredible! x$combo"
      }
      val pRow = toClear.firstOrNull()?.first ?: 3
      val pCol = toClear.firstOrNull()?.second ?: 3
      popups.add(ScorePopup(System.currentTimeMillis(), praise, pRow, pCol, 0xFFFFD700))
    }

    return StepClearResult(
      clearedPositions = toClear,
      newlyCreatedSpecials = newlyCreatedSpecials,
      specialsActivated = specialsActivated,
      scoreGained = stepScore,
      popups = popups,
      jelliesCleared = jelliesCleared,
      blockersBroken = blockersBroken,
      ingredientsCollected = 0,
      clearedColors = clearedColors
    )
  }

  fun finalizeClearStep(step: StepClearResult) {
    for ((r, c) in step.clearedPositions) {
      board[r][c] = null
    }
    for (special in step.newlyCreatedSpecials) {
      board[special.row][special.col] = special
    }
  }

  fun applyGravityOnly(): StepGravityResult {
    var ingredientsReachedBottom = 0
    val movements = mutableListOf<TileMovement>()

    // Check ingredients on row 7 (bottom row)
    for (c in 0..7) {
      val tile = board[7][c]
      if (tile?.tileType == TileType.INGREDIENT) {
        ingredientsReachedBottom++
        board[7][c] = null
      }
    }

    // Gravity fall column by column
    for (c in 0..7) {
      var writeRow = 7
      for (r in 7 downTo 0) {
        val tile = board[r][c]
        if (tile?.tileType == TileType.BLOCKER || levelConfig.blockedHoles.contains(Pair(r, c))) {
          writeRow = r - 1
          continue
        }

        if (tile != null) {
          if (r != writeRow) {
            movements.add(TileMovement(fromRow = r, fromCol = c, toRow = writeRow, toCol = c, tile = tile))
            board[writeRow][c] = tile.copy(row = writeRow, col = c)
            board[r][c] = null
          }
          writeRow--
        }
      }
    }

    return StepGravityResult(movements, ingredientsReachedBottom)
  }

  fun refillEmptySlots(): StepRefillResult {
    val newTiles = mutableListOf<Tile>()

    for (c in 0..7) {
      for (r in 7 downTo 0) {
        if (board[r][c] == null && !levelConfig.blockedHoles.contains(Pair(r, c))) {
          val newColor = CandyColor.entries.random()
          val newTile = Tile(
            row = r,
            col = c,
            color = newColor,
            special = SpecialPiece.NONE,
            tileType = TileType.NORMAL
          )
          board[r][c] = newTile
          newTiles.add(newTile)
        }
      }
    }

    // Recheck bottom row for any ingredient that just landed
    for (c in 0..7) {
      val tile = board[7][c]
      if (tile?.tileType == TileType.INGREDIENT) {
        board[7][c] = null
        val replacement = Tile(7, c, CandyColor.entries.random(), SpecialPiece.NONE, TileType.NORMAL)
        board[7][c] = replacement
        newTiles.add(replacement)
      }
    }

    return StepRefillResult(newTiles)
  }

  fun processMatchesAndCascades(lastSwapPos: Pair<Int, Int>? = null): MatchResult {
    var totalCleared = 0
    var totalScore = 0
    var maxCombo = currentCombo
    val specialsCreated = mutableListOf<Tile>()
    val specialsActivated = mutableListOf<Tile>()
    val clearedColors = mutableMapOf<CandyColor, Int>()
    var totalJellies = 0
    var totalBlockers = 0
    var totalIngredients = 0
    val popups = mutableListOf<ScorePopup>()
    val particles = mutableListOf<ParticleEffect>()

    var iteration = 0
    while (iteration < 15) { // Safety ceiling for chain reactions
      iteration++
      val step = clearNextMatchesStep(lastSwapPos = if (iteration == 1) lastSwapPos else null, combo = iteration)
      if (step == null) break

      finalizeClearStep(step)

      totalCleared += step.clearedPositions.size
      totalScore += step.scoreGained
      maxCombo = iteration
      specialsCreated.addAll(step.newlyCreatedSpecials)
      specialsActivated.addAll(step.specialsActivated)
      step.clearedColors.forEach { (k, v) -> clearedColors[k] = (clearedColors[k] ?: 0) + v }
      totalJellies += step.jelliesCleared
      totalBlockers += step.blockersBroken
      popups.addAll(step.popups)

      // Apply gravity and refill
      val ingredientsDropped = applyGravityAndRefill()
      totalIngredients += ingredientsDropped
      this.ingredientsCollected += ingredientsDropped

      currentCombo = iteration
    }

    checkGameStatus()

    return MatchResult(
      clearedCount = totalCleared,
      scoreGained = totalScore,
      comboCount = maxCombo,
      specialPiecesCreated = specialsCreated,
      specialPiecesActivated = specialsActivated,
      clearedColors = clearedColors,
      jelliesCleared = totalJellies,
      blockersBroken = totalBlockers,
      ingredientsCollected = totalIngredients,
      scorePopups = popups,
      particles = particles
    )
  }

  fun applyGravityAndRefill(): Int {
    val grav = applyGravityOnly()
    val refill = refillEmptySlots()
    val totalIng = grav.ingredientsDropped
    return totalIng
  }

  fun findMatches(): List<List<Tile>> {
    val visitedInGroup = mutableSetOf<Pair<Int, Int>>()
    val matches = mutableListOf<List<Tile>>()

    // Horizontal scans
    for (r in 0..7) {
      var matchLen = 1
      for (c in 0..7) {
        val curr = board[r][c]
        val next = if (c < 7) board[r][c + 1] else null

        if (curr?.isMovable == true && next?.isMovable == true && curr.color != null && curr.color == next.color) {
          matchLen++
        } else {
          if (matchLen >= 3) {
            val group = (c - matchLen + 1..c).mapNotNull { board[r][it] }
            matches.add(group)
          }
          matchLen = 1
        }
      }
    }

    // Vertical scans
    for (c in 0..7) {
      var matchLen = 1
      for (r in 0..7) {
        val curr = board[r][c]
        val next = if (r < 7) board[r + 1][c] else null

        if (curr?.isMovable == true && next?.isMovable == true && curr.color != null && curr.color == next.color) {
          matchLen++
        } else {
          if (matchLen >= 3) {
            val group = (r - matchLen + 1..r).mapNotNull { board[it][c] }
            matches.add(group)
          }
          matchLen = 1
        }
      }
    }

    return matches
  }

  // Booster logic
  fun useBooster(type: BoosterType, targetRow: Int = -1, targetCol: Int = -1): MatchResult {
    val toClear = mutableSetOf<Pair<Int, Int>>()
    val popups = mutableListOf<ScorePopup>()
    val specialsActivated = mutableListOf<Tile>()

    when (type) {
      BoosterType.HAMMER -> {
        val r = targetRow.coerceIn(0, 7)
        val c = targetCol.coerceIn(0, 7)
        toClear.add(Pair(r, c))
        popups.add(ScorePopup(System.currentTimeMillis(), "HAMMER SMASH!", r, c, 0xFFFF9800))
      }
      BoosterType.ROCKET -> {
        val r = targetRow.coerceIn(0, 7)
        val c = targetCol.coerceIn(0, 7)
        for (col in 0..7) toClear.add(Pair(r, col))
        for (row in 0..7) toClear.add(Pair(row, c))
        popups.add(ScorePopup(System.currentTimeMillis(), "ROCKET BLAST!", r, c, 0xFFE91E63))
      }
      BoosterType.RAINBOW_ORB -> {
        // Pick the most abundant color
        val colorCounts = mutableMapOf<CandyColor, Int>()
        for (r in 0..7) {
          for (c in 0..7) {
            val color = board[r][c]?.color
            if (color != null) {
              colorCounts[color] = (colorCounts[color] ?: 0) + 1
            }
          }
        }
        val topColor = colorCounts.maxByOrNull { it.value }?.key ?: CandyColor.entries.random()
        for (r in 0..7) {
          for (c in 0..7) {
            if (board[r][c]?.color == topColor) {
              toClear.add(Pair(r, c))
            }
          }
        }
        popups.add(ScorePopup(System.currentTimeMillis(), "PRISM WIPEOUT!", 3, 3, 0xFF9C27B0))
      }
      BoosterType.SHUFFLE -> {
        shuffleBoard()
        popups.add(ScorePopup(System.currentTimeMillis(), "SHUFFLE POP!", 3, 3, 0xFF00E676))
        return MatchResult(0, 50, 1, emptyList(), emptyList(), emptyMap(), 0, 0, 0, popups, emptyList())
      }
      BoosterType.LIGHTNING -> {
        // Zap 8 random movable pieces
        val movableCoords = mutableListOf<Pair<Int, Int>>()
        for (r in 0..7) {
          for (c in 0..7) {
            if (board[r][c]?.tileType != TileType.BLOCKER) {
              movableCoords.add(Pair(r, c))
            }
          }
        }
        movableCoords.shuffled().take(8).forEach { toClear.add(it) }
        popups.add(ScorePopup(System.currentTimeMillis(), "THUNDER BOLT!", 3, 3, 0xFFFFEB3B))
      }
    }

    var score = 0
    var clearedCount = 0
    var jelliesCleared = 0
    var blockersBroken = 0
    var ingredientsDropped = 0
    val colorsCleared = mutableMapOf<CandyColor, Int>()

    toClear.forEach { (r, c) ->
      val tile = board[r][c] ?: return@forEach
      if (tile.tileType == TileType.BLOCKER) {
        blockersBroken++
        blockersRemaining = maxOf(0, blockersRemaining - 1)
        board[r][c] = Tile(r, c, getRandomColorExcept(r, c))
      } else {
        if (tile.tileType == TileType.JELLY_1 || tile.tileType == TileType.JELLY_2) {
          jelliesCleared++
          jelliesRemaining = maxOf(0, jelliesRemaining - 1)
        }
        if (tile.tileType == TileType.INGREDIENT) {
          ingredientsDropped++
          this.ingredientsCollected++
        }
        if (tile.color != null) {
          colorsCleared[tile.color] = (colorsCleared[tile.color] ?: 0) + 1
          candiesCollected[tile.color] = (candiesCollected[tile.color] ?: 0) + 1
        }
        clearedCount++
        score += 80
        board[r][c] = null
      }
    }

    currentScore += score
    applyGravityAndRefill()
    currentCombo = 1
    val cascades = processMatchesAndCascades()

    return MatchResult(
      clearedCount = clearedCount + cascades.clearedCount,
      scoreGained = score + cascades.scoreGained,
      comboCount = 1 + cascades.comboCount,
      specialPiecesCreated = cascades.specialPiecesCreated,
      specialPiecesActivated = specialsActivated + cascades.specialPiecesActivated,
      clearedColors = mergeColorMaps(colorsCleared, cascades.clearedColors),
      jelliesCleared = jelliesCleared + cascades.jelliesCleared,
      blockersBroken = blockersBroken + cascades.blockersBroken,
      ingredientsCollected = ingredientsDropped + cascades.ingredientsCollected,
      scorePopups = popups + cascades.scorePopups,
      particles = cascades.particles
    )
  }

  fun shuffleBoard() {
    val movablePieces = mutableListOf<Tile>()
    for (r in 0..7) {
      for (c in 0..7) {
        val tile = board[r][c]
        if (tile != null && tile.tileType == TileType.NORMAL && tile.color != null) {
          movablePieces.add(tile)
        }
      }
    }
    movablePieces.shuffle()
    var idx = 0
    for (r in 0..7) {
      for (c in 0..7) {
        val tile = board[r][c]
        if (tile != null && tile.tileType == TileType.NORMAL && tile.color != null && idx < movablePieces.size) {
          val shuffled = movablePieces[idx++]
          board[r][c] = tile.copy(color = shuffled.color, special = shuffled.special)
        }
      }
    }
  }

  fun hasPossibleMoves(): Boolean {
    for (r in 0..7) {
      for (c in 0..7) {
        if (canSwap(r, c, r + 1, c) || canSwap(r, c, r, c + 1)) {
          return true
        }
      }
    }
    return false
  }

  fun checkGameStatus() {
    val objectivesMet = levelConfig.objectives.all { obj ->
      when (obj.type) {
        ObjectiveType.TARGET_SCORE -> currentScore >= obj.targetCount
        ObjectiveType.CLEAR_JELLY -> jelliesRemaining <= 0
        ObjectiveType.BREAK_BLOCKS -> blockersRemaining <= 0
        ObjectiveType.COLLECT_INGREDIENTS -> ingredientsCollected >= obj.targetCount
        ObjectiveType.COLLECT_CANDIES -> {
          val count = candiesCollected[obj.targetColor] ?: 0
          count >= obj.targetCount
        }
      }
    }

    if (objectivesMet) {
      isLevelWon = true
      isLevelLost = false
    } else if (movesRemaining <= 0) {
      isLevelLost = true
      isLevelWon = false
    }
  }

  fun calculateStars(): Int {
    return when {
      currentScore >= levelConfig.star3Score -> 3
      currentScore >= levelConfig.star2Score -> 2
      currentScore >= levelConfig.star1Score -> 1
      isLevelWon -> 1
      else -> 0
    }
  }

  fun addMoves(count: Int) {
    movesRemaining += count
    isLevelLost = false
  }

  private fun mergeColorMaps(m1: Map<CandyColor, Int>, m2: Map<CandyColor, Int>): Map<CandyColor, Int> {
    val result = m1.toMutableMap()
    m2.forEach { (k, v) -> result[k] = (result[k] ?: 0) + v }
    return result
  }
}
