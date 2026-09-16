package com.example.sweetquest.model

enum class ObjectiveType {
  TARGET_SCORE,
  CLEAR_JELLY,
  BREAK_BLOCKS,
  COLLECT_INGREDIENTS,
  COLLECT_CANDIES
}

data class LevelObjective(
  val type: ObjectiveType,
  val targetCount: Int,
  val targetColor: CandyColor? = null,
  val description: String
)

data class LevelConfig(
  val levelNumber: Int,
  val worldName: String,
  val movesLimit: Int,
  val star1Score: Int,
  val star2Score: Int,
  val star3Score: Int,
  val objectives: List<LevelObjective>,
  val initialJellies: Set<Pair<Int, Int>> = emptySet(),
  val initialBlockers: Set<Pair<Int, Int>> = emptySet(),
  val initialCages: Set<Pair<Int, Int>> = emptySet(),
  val initialIngredients: Set<Pair<Int, Int>> = emptySet(),
  val blockedHoles: Set<Pair<Int, Int>> = emptySet()
)
