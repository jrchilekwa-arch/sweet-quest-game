package com.example.sweetquest.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sweetquest.model.BoosterType
import com.example.sweetquest.model.CandyColor
import com.example.sweetquest.model.LevelConfig
import com.example.sweetquest.model.ObjectiveType

@Composable
fun GameHeader(
  levelConfig: LevelConfig,
  movesRemaining: Int,
  score: Int,
  stars: Int,
  jelliesRemaining: Int,
  blockersRemaining: Int,
  ingredientsCollected: Int,
  candiesCollected: Map<CandyColor, Int>,
  onPauseClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val targetMax = levelConfig.star3Score.toFloat()
  val progress = (score / targetMax).coerceIn(0f, 1f)
  val animatedProgress by animateFloatAsState(targetValue = progress, label = "score_progress")

  Surface(
    color = Color.White.copy(alpha = 0.92f),
    shape = RoundedCornerShape(18.dp),
    shadowElevation = 6.dp,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      // Top bar: Level info, Moves left, Pause button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Level pill
        Box(
          modifier = Modifier
            .background(Brush.horizontalGradient(listOf(Color(0xFFE91E63), Color(0xFFFF4081))), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = "Level ${levelConfig.levelNumber}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }

        // Moves badge
        Box(
          modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(
              Brush.verticalGradient(
                if (movesRemaining <= 5) listOf(Color(0xFFFF5252), Color(0xFFD50000))
                else listOf(Color(0xFFFFD54F), Color(0xFFFFB300))
              ),
              RoundedCornerShape(16.dp)
            )
            .border(2.dp, Color.White, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "$movesRemaining",
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 20.sp
            )
            Text(
              text = "MOVES",
              color = Color.White.copy(alpha = 0.9f),
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp
            )
          }
        }

        // Pause button
        IconButton(
          onClick = onPauseClick,
          modifier = Modifier
            .size(38.dp)
            .background(Color(0xFFF3E5F5), CircleShape)
            .testTag("pause_button")
        ) {
          Icon(
            imageVector = Icons.Default.Pause,
            contentDescription = "Pause",
            tint = Color(0xFF7B1FA2),
            modifier = Modifier.size(22.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Score Progress Bar with 3 Stars
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(18.dp)
          .clip(RoundedCornerShape(9.dp))
          .background(Color(0xFFEEEEEE))
      ) {
        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(18.dp),
          color = Color(0xFFFFB300),
          trackColor = Color(0xFFEEEEEE)
        )

        // 3 Star Indicators at thresholds
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (i in 1..3) {
            val isEarned = stars >= i
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = "Star $i",
              tint = if (isEarned) Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Score: $score",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF424242)
        )
        Text(
          text = "Target: ${levelConfig.star1Score}",
          fontSize = 11.sp,
          color = Color(0xFF757575)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Objectives checklist banner
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFFFFF9C4), RoundedCornerShape(10.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Flag,
          contentDescription = null,
          tint = Color(0xFFF57F17),
          modifier = Modifier.size(16.dp)
        )

        levelConfig.objectives.forEach { obj ->
          val (isDone, label) = when (obj.type) {
            ObjectiveType.TARGET_SCORE -> Pair(score >= obj.targetCount, "${score}/${obj.targetCount}")
            ObjectiveType.CLEAR_JELLY -> Pair(jelliesRemaining <= 0, "Jellies: $jelliesRemaining")
            ObjectiveType.BREAK_BLOCKS -> Pair(blockersRemaining <= 0, "Blocks: $blockersRemaining")
            ObjectiveType.COLLECT_INGREDIENTS -> Pair(ingredientsCollected >= obj.targetCount, "Nuts: $ingredientsCollected/${obj.targetCount}")
            ObjectiveType.COLLECT_CANDIES -> {
              val current = candiesCollected[obj.targetColor] ?: 0
              Pair(current >= obj.targetCount, "${obj.targetColor?.name?.lowercase() ?: "candies"}: $current/${obj.targetCount}")
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            if (isDone) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Done",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(14.dp)
              )
            }
            Text(
              text = label,
              fontSize = 11.sp,
              fontWeight = if (isDone) FontWeight.Bold else FontWeight.Medium,
              color = if (isDone) Color(0xFF2E7D32) else Color(0xFF424242)
            )
          }
        }
      }
    }
  }
}

@Composable
fun BoosterTray(
  hammerCount: Int,
  rocketCount: Int,
  rainbowCount: Int,
  shuffleCount: Int,
  lightningCount: Int,
  activeBooster: BoosterType?,
  onBoosterClick: (BoosterType) -> Unit,
  onAddBoosterClick: (BoosterType) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 10.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    BoosterButton(
      type = BoosterType.HAMMER,
      icon = Icons.Default.Build,
      count = hammerCount,
      bgColor = Color(0xFFFF9800),
      isActive = activeBooster == BoosterType.HAMMER,
      onClick = { if (hammerCount > 0) onBoosterClick(BoosterType.HAMMER) else onAddBoosterClick(BoosterType.HAMMER) }
    )
    BoosterButton(
      type = BoosterType.ROCKET,
      icon = Icons.Default.RocketLaunch,
      count = rocketCount,
      bgColor = Color(0xFFE91E63),
      isActive = activeBooster == BoosterType.ROCKET,
      onClick = { if (rocketCount > 0) onBoosterClick(BoosterType.ROCKET) else onAddBoosterClick(BoosterType.ROCKET) }
    )
    BoosterButton(
      type = BoosterType.RAINBOW_ORB,
      icon = Icons.Default.InvertColors,
      count = rainbowCount,
      bgColor = Color(0xFF9C27B0),
      isActive = activeBooster == BoosterType.RAINBOW_ORB,
      onClick = { if (rainbowCount > 0) onBoosterClick(BoosterType.RAINBOW_ORB) else onAddBoosterClick(BoosterType.RAINBOW_ORB) }
    )
    BoosterButton(
      type = BoosterType.SHUFFLE,
      icon = Icons.Default.Casino,
      count = shuffleCount,
      bgColor = Color(0xFF00C853),
      isActive = activeBooster == BoosterType.SHUFFLE,
      onClick = { if (shuffleCount > 0) onBoosterClick(BoosterType.SHUFFLE) else onAddBoosterClick(BoosterType.SHUFFLE) }
    )
    BoosterButton(
      type = BoosterType.LIGHTNING,
      icon = Icons.Default.Bolt,
      count = lightningCount,
      bgColor = Color(0xFFFFD600),
      isActive = activeBooster == BoosterType.LIGHTNING,
      onClick = { if (lightningCount > 0) onBoosterClick(BoosterType.LIGHTNING) else onAddBoosterClick(BoosterType.LIGHTNING) }
    )
  }
}

@Composable
private fun BoosterButton(
  type: BoosterType,
  icon: ImageVector,
  count: Int,
  bgColor: Color,
  isActive: Boolean,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.testTag("booster_${type.name.lowercase()}")
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .shadow(if (isActive) 8.dp else 3.dp, CircleShape)
        .background(
          Brush.radialGradient(listOf(bgColor, bgColor.copy(alpha = 0.8f))),
          CircleShape
        )
        .border(
          width = if (isActive) 3.dp else 1.5.dp,
          color = if (isActive) Color.White else Color.White.copy(alpha = 0.5f),
          shape = CircleShape
        )
        .clickable { onClick() },
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = type.displayName,
        tint = Color.White,
        modifier = Modifier.size(24.dp)
      )

      // Count badge
      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .size(18.dp)
          .background(if (count > 0) Color(0xFF212121) else Color(0xFFD32F2F), CircleShape)
          .border(1.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (count > 0) "$count" else "+",
          color = Color.White,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Text(
      text = type.displayName.split(" ").first(),
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
      color = Color(0xFF424242),
      modifier = Modifier.padding(top = 2.dp)
    )
  }
}
