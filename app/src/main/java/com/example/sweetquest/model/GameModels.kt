package com.example.sweetquest.model

enum class CandyColor {
  RED,       // Strawberry Ruby
  YELLOW,    // Lemon Drop
  GREEN,     // Mint Emerald
  ORANGE,    // Caramel Swirl
  PURPLE,    // Grape Jewel
  BLUE       // Blueberry Bliss
}

enum class SpecialPiece {
  NONE,
  HORIZONTAL_STRIPED, // Clears row
  VERTICAL_STRIPED,   // Clears column
  EXPLOSIVE_WRAPPED,  // 3x3 blast
  RAINBOW_PRISM       // Clears all candies of matched type
}

enum class TileType {
  NORMAL,
  BLOCKER,     // Cookie / Chocolate (needs adjacent match)
  JELLY_1,     // Single jelly layer
  JELLY_2,     // Double jelly layer
  CAGE,        // Licorice cage holding piece
  INGREDIENT   // Droppable ingredient (Chestnut/Cherry)
}

data class Tile(
  val row: Int,
  val col: Int,
  val color: CandyColor? = null,
  val special: SpecialPiece = SpecialPiece.NONE,
  val tileType: TileType = TileType.NORMAL,
  val id: Long = (row * 8 + col).toLong(),
  val isMatched: Boolean = false,
  val isHighlighted: Boolean = false
) {
  val isMovable: Boolean
    get() = tileType != TileType.BLOCKER && tileType != TileType.CAGE && color != null
}

enum class BoosterType(
  val displayName: String,
  val description: String,
  val coinCost: Int
) {
  HAMMER("Sweet Hammer", "Smash any single candy or obstacle", 100),
  ROCKET("Sugar Rocket", "Clears an entire row and column", 150),
  RAINBOW_ORB("Prism Orb", "Eliminates all candies of chosen color", 200),
  SHUFFLE("Jumble Pop", "Shuffles the board with sweet cascades", 75),
  LIGHTNING("Thunder Drop", "Zaps 8 random candies with sugar lightning", 180)
}

enum class GameTheme(
  val id: String,
  val displayName: String,
  val primaryColorHex: Long,
  val accentColorHex: Long,
  val boardBgHex: Long,
  val priceCoins: Int
) {
  CLASSIC("classic", "Sweet Meadow", 0xFFE91E63, 0xFFFFB300, 0xFFFFF0F5, 0),
  NEON_SUGAR("neon", "Neon Candyland", 0xFF9C27B0, 0xFF00E5FF, 0xFF1A0933, 300),
  PASTEL_DREAM("pastel", "Pastel Dream", 0xFF7986CB, 0xFFF48FB1, 0xFFF3E5F5, 500),
  CHOCO_KINGDOM("choco", "Choco Kingdom", 0xFF5D4037, 0xFFFFD54F, 0xFFEFEBE9, 800)
}

data class ScorePopup(
  val id: Long,
  val text: String,
  val row: Int,
  val col: Int,
  val color: Long = 0xFFFFEB3B
)

data class ParticleEffect(
  val id: Long,
  val x: Float,
  val y: Float,
  val color: Long,
  val size: Float,
  val velocityX: Float,
  val velocityY: Float
)
