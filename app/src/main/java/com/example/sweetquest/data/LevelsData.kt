package com.example.sweetquest.data

import com.example.sweetquest.model.CandyColor
import com.example.sweetquest.model.LevelConfig
import com.example.sweetquest.model.LevelObjective
import com.example.sweetquest.model.ObjectiveType

object LevelsData {
  val levels: List<LevelConfig> = listOf(
    // Zone 1: Candy Meadows (Levels 1-5)
    LevelConfig(
      levelNumber = 1,
      worldName = "Candy Meadows",
      movesLimit = 25,
      star1Score = 1500,
      star2Score = 3000,
      star3Score = 4500,
      objectives = listOf(
        LevelObjective(ObjectiveType.TARGET_SCORE, 1500, null, "Reach 1,500 points")
      )
    ),
    LevelConfig(
      levelNumber = 2,
      worldName = "Candy Meadows",
      movesLimit = 22,
      star1Score = 2000,
      star2Score = 4000,
      star3Score = 6000,
      objectives = listOf(
        LevelObjective(ObjectiveType.COLLECT_CANDIES, 18, CandyColor.RED, "Collect 18 Red Berries")
      )
    ),
    LevelConfig(
      levelNumber = 3,
      worldName = "Candy Meadows",
      movesLimit = 20,
      star1Score = 2500,
      star2Score = 5000,
      star3Score = 7500,
      objectives = listOf(
        LevelObjective(ObjectiveType.COLLECT_CANDIES, 15, CandyColor.GREEN, "Collect 15 Mint Stars"),
        LevelObjective(ObjectiveType.COLLECT_CANDIES, 15, CandyColor.YELLOW, "Collect 15 Lemon Drops")
      )
    ),
    LevelConfig(
      levelNumber = 4,
      worldName = "Candy Meadows",
      movesLimit = 24,
      star1Score = 3000,
      star2Score = 6000,
      star3Score = 9000,
      objectives = listOf(
        LevelObjective(ObjectiveType.TARGET_SCORE, 3000, null, "Reach 3,000 points"),
        LevelObjective(ObjectiveType.COLLECT_CANDIES, 20, CandyColor.BLUE, "Collect 20 Blueberries")
      ),
      blockedHoles = setOf(Pair(0, 0), Pair(0, 7), Pair(7, 0), Pair(7, 7))
    ),
    LevelConfig(
      levelNumber = 5,
      worldName = "Candy Meadows",
      movesLimit = 20,
      star1Score = 3500,
      star2Score = 7000,
      star3Score = 10000,
      objectives = listOf(
        LevelObjective(ObjectiveType.COLLECT_CANDIES, 20, CandyColor.PURPLE, "Collect 20 Grape Jewels"),
        LevelObjective(ObjectiveType.COLLECT_CANDIES, 20, CandyColor.ORANGE, "Collect 20 Caramel Swirls")
      )
    ),

    // Zone 2: Jelly Forest (Levels 6-10)
    LevelConfig(
      levelNumber = 6,
      worldName = "Jelly Forest",
      movesLimit = 22,
      star1Score = 2500,
      star2Score = 5000,
      star3Score = 8000,
      objectives = listOf(
        LevelObjective(ObjectiveType.CLEAR_JELLY, 12, null, "Clear 12 Jelly Tiles")
      ),
      initialJellies = setOf(
        Pair(2, 2), Pair(2, 3), Pair(2, 4), Pair(2, 5),
        Pair(3, 2), Pair(3, 3), Pair(3, 4), Pair(3, 5),
        Pair(4, 2), Pair(4, 3), Pair(4, 4), Pair(4, 5)
      )
    ),
    LevelConfig(
      levelNumber = 7,
      worldName = "Jelly Forest",
      movesLimit = 24,
      star1Score = 3000,
      star2Score = 6000,
      star3Score = 9500,
      objectives = listOf(
        LevelObjective(ObjectiveType.CLEAR_JELLY, 16, null, "Clear 16 Jelly Tiles")
      ),
      initialJellies = setOf(
        Pair(1, 1), Pair(1, 6), Pair(2, 2), Pair(2, 5),
        Pair(3, 3), Pair(3, 4), Pair(4, 3), Pair(4, 4),
        Pair(5, 2), Pair(5, 5), Pair(6, 1), Pair(6, 6),
        Pair(3, 1), Pair(3, 6), Pair(4, 1), Pair(4, 6)
      )
    ),
    LevelConfig(
      levelNumber = 8,
      worldName = "Jelly Forest",
      movesLimit = 25,
      star1Score = 3500,
      star2Score = 7000,
      star3Score = 11000,
      objectives = listOf(
        LevelObjective(ObjectiveType.CLEAR_JELLY, 20, null, "Clear 20 Jelly Tiles"),
        LevelObjective(ObjectiveType.COLLECT_CANDIES, 15, CandyColor.RED, "Collect 15 Red Berries")
      ),
      initialJellies = (2..5).flatMap { r -> (1..6).map { c -> Pair(r, c) } }.take(20).toSet()
    ),
    LevelConfig(
      levelNumber = 9,
      worldName = "Jelly Forest",
      movesLimit = 26,
      star1Score = 4000,
      star2Score = 8000,
      star3Score = 12000,
      objectives = listOf(
        LevelObjective(ObjectiveType.CLEAR_JELLY, 18, null, "Clear 18 Jelly Tiles")
      ),
      initialJellies = setOf(
        Pair(0, 3), Pair(0, 4), Pair(1, 2), Pair(1, 5),
        Pair(2, 1), Pair(2, 6), Pair(5, 1), Pair(5, 6),
        Pair(6, 2), Pair(6, 5), Pair(7, 3), Pair(7, 4),
        Pair(3, 3), Pair(3, 4), Pair(4, 3), Pair(4, 4),
        Pair(2, 2), Pair(5, 5)
      ),
      blockedHoles = setOf(Pair(3, 0), Pair(4, 0), Pair(3, 7), Pair(4, 7))
    ),
    LevelConfig(
      levelNumber = 10,
      worldName = "Jelly Forest",
      movesLimit = 28,
      star1Score = 5000,
      star2Score = 9000,
      star3Score = 14000,
      objectives = listOf(
        LevelObjective(ObjectiveType.CLEAR_JELLY, 24, null, "Clear 24 Jelly Tiles"),
        LevelObjective(ObjectiveType.TARGET_SCORE, 5000, null, "Reach 5,000 points")
      ),
      initialJellies = (1..6).flatMap { r -> (2..5).map { c -> Pair(r, c) } }.toSet()
    ),

    // Zone 3: Cookie Mountains (Levels 11-15)
    LevelConfig(
      levelNumber = 11,
      worldName = "Cookie Mountains",
      movesLimit = 22,
      star1Score = 3000,
      star2Score = 6000,
      star3Score = 9000,
      objectives = listOf(
        LevelObjective(ObjectiveType.BREAK_BLOCKS, 8, null, "Break 8 Cookie Blocks")
      ),
      initialBlockers = setOf(
        Pair(3, 2), Pair(3, 3), Pair(3, 4), Pair(3, 5),
        Pair(4, 2), Pair(4, 3), Pair(4, 4), Pair(4, 5)
      )
    ),
    LevelConfig(
      levelNumber = 12,
      worldName = "Cookie Mountains",
      movesLimit = 25,
      star1Score = 3500,
      star2Score = 7500,
      star3Score = 11000,
      objectives = listOf(
        LevelObjective(ObjectiveType.BREAK_BLOCKS, 12, null, "Break 12 Cookie Blocks"),
        LevelObjective(ObjectiveType.COLLECT_CANDIES, 20, CandyColor.YELLOW, "Collect 20 Lemon Drops")
      ),
      initialBlockers = setOf(
        Pair(2, 1), Pair(2, 6), Pair(3, 2), Pair(3, 5),
        Pair(4, 2), Pair(4, 5), Pair(5, 1), Pair(5, 6),
        Pair(3, 3), Pair(3, 4), Pair(4, 3), Pair(4, 4)
      )
    ),
    LevelConfig(
      levelNumber = 13,
      worldName = "Cookie Mountains",
      movesLimit = 24,
      star1Score = 4000,
      star2Score = 8000,
      star3Score = 12000,
      objectives = listOf(
        LevelObjective(ObjectiveType.BREAK_BLOCKS, 14, null, "Break 14 Cookie Blocks"),
        LevelObjective(ObjectiveType.CLEAR_JELLY, 10, null, "Clear 10 Jelly Tiles")
      ),
      initialBlockers = setOf(
        Pair(1, 3), Pair(1, 4), Pair(2, 3), Pair(2, 4),
        Pair(5, 3), Pair(5, 4), Pair(6, 3), Pair(6, 4),
        Pair(3, 1), Pair(4, 1), Pair(3, 6), Pair(4, 6),
        Pair(3, 3), Pair(4, 4)
      ),
      initialJellies = setOf(
        Pair(3, 2), Pair(3, 5), Pair(4, 2), Pair(4, 5),
        Pair(2, 2), Pair(2, 5), Pair(5, 2), Pair(5, 5),
        Pair(1, 2), Pair(1, 5)
      )
    ),
    LevelConfig(
      levelNumber = 14,
      worldName = "Cookie Mountains",
      movesLimit = 26,
      star1Score = 4500,
      star2Score = 9000,
      star3Score = 13500,
      objectives = listOf(
        LevelObjective(ObjectiveType.BREAK_BLOCKS, 16, null, "Break 16 Cookie Blocks")
      ),
      initialBlockers = (2..5).flatMap { r -> (2..5).map { c -> Pair(r, c) } }.toSet()
    ),
    LevelConfig(
      levelNumber = 15,
      worldName = "Cookie Mountains",
      movesLimit = 28,
      star1Score = 5500,
      star2Score = 11000,
      star3Score = 16000,
      objectives = listOf(
        LevelObjective(ObjectiveType.BREAK_BLOCKS, 14, null, "Break 14 Cookie Blocks"),
        LevelObjective(ObjectiveType.COLLECT_CANDIES, 25, CandyColor.GREEN, "Collect 25 Mint Stars")
      ),
      initialBlockers = setOf(
        Pair(0, 2), Pair(0, 5), Pair(2, 0), Pair(2, 7),
        Pair(5, 0), Pair(5, 7), Pair(7, 2), Pair(7, 5),
        Pair(3, 2), Pair(3, 5), Pair(4, 2), Pair(4, 5),
        Pair(3, 3), Pair(4, 4)
      ),
      blockedHoles = setOf(Pair(1, 1), Pair(1, 6), Pair(6, 1), Pair(6, 6))
    ),

    // Zone 4: Caramel Falls (Levels 16-20)
    LevelConfig(
      levelNumber = 16,
      worldName = "Caramel Falls",
      movesLimit = 24,
      star1Score = 3500,
      star2Score = 7000,
      star3Score = 10500,
      objectives = listOf(
        LevelObjective(ObjectiveType.COLLECT_INGREDIENTS, 2, null, "Drop 2 Sugar Nuts to bottom")
      ),
      initialIngredients = setOf(Pair(0, 2), Pair(0, 5))
    ),
    LevelConfig(
      levelNumber = 17,
      worldName = "Caramel Falls",
      movesLimit = 25,
      star1Score = 4000,
      star2Score = 8000,
      star3Score = 12000,
      objectives = listOf(
        LevelObjective(ObjectiveType.COLLECT_INGREDIENTS, 3, null, "Drop 3 Sugar Nuts to bottom")
      ),
      initialIngredients = setOf(Pair(0, 1), Pair(0, 4), Pair(0, 6)),
      initialBlockers = setOf(Pair(4, 1), Pair(4, 4), Pair(4, 6))
    ),
    LevelConfig(
      levelNumber = 18,
      worldName = "Caramel Falls",
      movesLimit = 26,
      star1Score = 4500,
      star2Score = 9000,
      star3Score = 13000,
      objectives = listOf(
        LevelObjective(ObjectiveType.COLLECT_INGREDIENTS, 2, null, "Drop 2 Sugar Nuts to bottom"),
        LevelObjective(ObjectiveType.CLEAR_JELLY, 12, null, "Clear 12 Jelly Tiles")
      ),
      initialIngredients = setOf(Pair(0, 3), Pair(0, 4)),
      initialJellies = (5..6).flatMap { r -> (1..6).map { c -> Pair(r, c) } }.toSet()
    ),
    LevelConfig(
      levelNumber = 19,
      worldName = "Caramel Falls",
      movesLimit = 27,
      star1Score = 5000,
      star2Score = 10000,
      star3Score = 15000,
      objectives = listOf(
        LevelObjective(ObjectiveType.COLLECT_INGREDIENTS, 3, null, "Drop 3 Sugar Nuts to bottom"),
        LevelObjective(ObjectiveType.BREAK_BLOCKS, 10, null, "Break 10 Cookie Blocks")
      ),
      initialIngredients = setOf(Pair(0, 2), Pair(0, 3), Pair(0, 5)),
      initialBlockers = setOf(
        Pair(3, 1), Pair(3, 2), Pair(3, 3), Pair(3, 4), Pair(3, 5),
        Pair(5, 2), Pair(5, 3), Pair(5, 4), Pair(5, 5), Pair(5, 6)
      )
    ),
    LevelConfig(
      levelNumber = 20,
      worldName = "Caramel Falls",
      movesLimit = 28,
      star1Score = 6000,
      star2Score = 12000,
      star3Score = 17500,
      objectives = listOf(
        LevelObjective(ObjectiveType.COLLECT_INGREDIENTS, 4, null, "Drop 4 Sugar Nuts to bottom")
      ),
      initialIngredients = setOf(Pair(0, 1), Pair(0, 3), Pair(0, 5), Pair(0, 7)),
      initialBlockers = setOf(
        Pair(2, 1), Pair(2, 3), Pair(2, 5), Pair(2, 7),
        Pair(5, 1), Pair(5, 3), Pair(5, 5), Pair(5, 7)
      )
    ),

    // Zone 5: Licorice Labyrinth (Levels 21-25)
    LevelConfig(
      levelNumber = 21,
      worldName = "Licorice Labyrinth",
      movesLimit = 22,
      star1Score = 4000,
      star2Score = 8000,
      star3Score = 12000,
      objectives = listOf(
        LevelObjective(ObjectiveType.CLEAR_JELLY, 14, null, "Clear 14 Jelly Tiles")
      ),
      initialCages = setOf(
        Pair(2, 2), Pair(2, 5), Pair(3, 3), Pair(3, 4),
        Pair(4, 3), Pair(4, 4), Pair(5, 2), Pair(5, 5)
      ),
      initialJellies = setOf(
        Pair(2, 2), Pair(2, 5), Pair(3, 3), Pair(3, 4),
        Pair(4, 3), Pair(4, 4), Pair(5, 2), Pair(5, 5),
        Pair(2, 3), Pair(2, 4), Pair(5, 3), Pair(5, 4),
        Pair(3, 2), Pair(4, 5)
      )
    ),
    LevelConfig(
      levelNumber = 22,
      worldName = "Licorice Labyrinth",
      movesLimit = 24,
      star1Score = 4500,
      star2Score = 9000,
      star3Score = 13500,
      objectives = listOf(
        LevelObjective(ObjectiveType.COLLECT_CANDIES, 25, CandyColor.PURPLE, "Collect 25 Grape Jewels"),
        LevelObjective(ObjectiveType.BREAK_BLOCKS, 8, null, "Break 8 Cookie Blocks")
      ),
      initialCages = setOf(Pair(1, 2), Pair(1, 5), Pair(6, 2), Pair(6, 5)),
      initialBlockers = setOf(
        Pair(3, 2), Pair(3, 5), Pair(4, 2), Pair(4, 5),
        Pair(2, 3), Pair(2, 4), Pair(5, 3), Pair(5, 4)
      )
    ),
    LevelConfig(
      levelNumber = 23,
      worldName = "Licorice Labyrinth",
      movesLimit = 25,
      star1Score = 5000,
      star2Score = 10000,
      star3Score = 15000,
      objectives = listOf(
        LevelObjective(ObjectiveType.CLEAR_JELLY, 16, null, "Clear 16 Jelly Tiles"),
        LevelObjective(ObjectiveType.COLLECT_INGREDIENTS, 2, null, "Drop 2 Sugar Nuts to bottom")
      ),
      initialIngredients = setOf(Pair(0, 2), Pair(0, 5)),
      initialCages = setOf(Pair(3, 2), Pair(3, 5), Pair(4, 2), Pair(4, 5)),
      initialJellies = (6..7).flatMap { r -> (1..6).map { c -> Pair(r, c) } }.take(16).toSet()
    ),
    LevelConfig(
      levelNumber = 24,
      worldName = "Licorice Labyrinth",
      movesLimit = 26,
      star1Score = 5500,
      star2Score = 11000,
      star3Score = 16500,
      objectives = listOf(
        LevelObjective(ObjectiveType.BREAK_BLOCKS, 12, null, "Break 12 Cookie Blocks"),
        LevelObjective(ObjectiveType.CLEAR_JELLY, 12, null, "Clear 12 Jelly Tiles")
      ),
      initialBlockers = setOf(
        Pair(2, 1), Pair(2, 6), Pair(3, 1), Pair(3, 6),
        Pair(4, 1), Pair(4, 6), Pair(5, 1), Pair(5, 6),
        Pair(1, 3), Pair(1, 4), Pair(6, 3), Pair(6, 4)
      ),
      initialJellies = setOf(
        Pair(2, 2), Pair(2, 5), Pair(3, 3), Pair(3, 4),
        Pair(4, 3), Pair(4, 4), Pair(5, 2), Pair(5, 5),
        Pair(3, 2), Pair(4, 5), Pair(3, 5), Pair(4, 2)
      )
    ),
    LevelConfig(
      levelNumber = 25,
      worldName = "Licorice Labyrinth",
      movesLimit = 28,
      star1Score = 6500,
      star2Score = 13000,
      star3Score = 19000,
      objectives = listOf(
        LevelObjective(ObjectiveType.TARGET_SCORE, 6500, null, "Reach 6,500 points"),
        LevelObjective(ObjectiveType.CLEAR_JELLY, 18, null, "Clear 18 Jelly Tiles")
      ),
      initialCages = setOf(
        Pair(1, 1), Pair(1, 6), Pair(2, 2), Pair(2, 5),
        Pair(5, 2), Pair(5, 5), Pair(6, 1), Pair(6, 6)
      ),
      initialJellies = (2..5).flatMap { r -> (2..5).map { c -> Pair(r, c) } }
        .plus(listOf(Pair(1, 3), Pair(6, 4))).toSet()
    ),

    // Zone 6: Grand Confectionery (Levels 26-30 - Master Levels)
    LevelConfig(
      levelNumber = 26,
      worldName = "Grand Confectionery",
      movesLimit = 26,
      star1Score = 6000,
      star2Score = 12000,
      star3Score = 18000,
      objectives = listOf(
        LevelObjective(ObjectiveType.COLLECT_INGREDIENTS, 3, null, "Drop 3 Sugar Nuts to bottom"),
        LevelObjective(ObjectiveType.BREAK_BLOCKS, 10, null, "Break 10 Cookie Blocks")
      ),
      initialIngredients = setOf(Pair(0, 1), Pair(0, 4), Pair(0, 7)),
      initialBlockers = setOf(
        Pair(3, 1), Pair(3, 4), Pair(3, 7),
        Pair(5, 1), Pair(5, 4), Pair(5, 7),
        Pair(4, 2), Pair(4, 3), Pair(4, 5), Pair(4, 6)
      )
    ),
    LevelConfig(
      levelNumber = 27,
      worldName = "Grand Confectionery",
      movesLimit = 27,
      star1Score = 7000,
      star2Score = 14000,
      star3Score = 21000,
      objectives = listOf(
        LevelObjective(ObjectiveType.CLEAR_JELLY, 20, null, "Clear 20 Jelly Tiles"),
        LevelObjective(ObjectiveType.COLLECT_CANDIES, 25, CandyColor.BLUE, "Collect 25 Blueberries")
      ),
      initialJellies = (1..6).flatMap { r -> (1..6).map { c -> Pair(r, c) } }.take(20).toSet(),
      initialCages = setOf(Pair(2, 3), Pair(2, 4), Pair(5, 3), Pair(5, 4))
    ),
    LevelConfig(
      levelNumber = 28,
      worldName = "Grand Confectionery",
      movesLimit = 28,
      star1Score = 7500,
      star2Score = 15000,
      star3Score = 22500,
      objectives = listOf(
        LevelObjective(ObjectiveType.BREAK_BLOCKS, 16, null, "Break 16 Cookie Blocks"),
        LevelObjective(ObjectiveType.CLEAR_JELLY, 14, null, "Clear 14 Jelly Tiles")
      ),
      initialBlockers = setOf(
        Pair(1, 2), Pair(1, 3), Pair(1, 4), Pair(1, 5),
        Pair(6, 2), Pair(6, 3), Pair(6, 4), Pair(6, 5),
        Pair(3, 0), Pair(3, 1), Pair(4, 0), Pair(4, 1),
        Pair(3, 6), Pair(3, 7), Pair(4, 6), Pair(4, 7)
      ),
      initialJellies = setOf(
        Pair(2, 2), Pair(2, 3), Pair(2, 4), Pair(2, 5),
        Pair(3, 3), Pair(3, 4), Pair(4, 3), Pair(4, 4),
        Pair(5, 2), Pair(5, 3), Pair(5, 4), Pair(5, 5),
        Pair(3, 2), Pair(4, 5)
      )
    ),
    LevelConfig(
      levelNumber = 29,
      worldName = "Grand Confectionery",
      movesLimit = 30,
      star1Score = 8000,
      star2Score = 16000,
      star3Score = 24000,
      objectives = listOf(
        LevelObjective(ObjectiveType.COLLECT_INGREDIENTS, 4, null, "Drop 4 Sugar Nuts to bottom"),
        LevelObjective(ObjectiveType.CLEAR_JELLY, 16, null, "Clear 16 Jelly Tiles")
      ),
      initialIngredients = setOf(Pair(0, 1), Pair(0, 3), Pair(0, 5), Pair(0, 7)),
      initialJellies = (5..6).flatMap { r -> (1..6).map { c -> Pair(r, c) } }.take(16).toSet(),
      initialBlockers = setOf(
        Pair(3, 1), Pair(3, 3), Pair(3, 5), Pair(3, 7),
        Pair(4, 2), Pair(4, 4), Pair(4, 6)
      )
    ),
    LevelConfig(
      levelNumber = 30,
      worldName = "Grand Confectionery - Final Palace",
      movesLimit = 32,
      star1Score = 10000,
      star2Score = 20000,
      star3Score = 30000,
      objectives = listOf(
        LevelObjective(ObjectiveType.TARGET_SCORE, 10000, null, "Reach 10,000 points"),
        LevelObjective(ObjectiveType.BREAK_BLOCKS, 12, null, "Break 12 Cookie Blocks"),
        LevelObjective(ObjectiveType.CLEAR_JELLY, 16, null, "Clear 16 Royal Jelly Tiles")
      ),
      initialBlockers = setOf(
        Pair(2, 2), Pair(2, 5), Pair(3, 1), Pair(3, 6),
        Pair(4, 1), Pair(4, 6), Pair(5, 2), Pair(5, 5),
        Pair(3, 3), Pair(3, 4), Pair(4, 3), Pair(4, 4)
      ),
      initialJellies = (1..6).flatMap { r -> (2..5).map { c -> Pair(r, c) } }.take(16).toSet(),
      initialCages = setOf(Pair(0, 3), Pair(0, 4), Pair(7, 3), Pair(7, 4))
    )
  )

  fun getLevel(number: Int): LevelConfig {
    return levels.find { it.levelNumber == number } ?: levels.first()
  }
}
