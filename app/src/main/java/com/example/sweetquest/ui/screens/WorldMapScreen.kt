package com.example.sweetquest.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sweetquest.data.LevelProgressEntity
import com.example.sweetquest.data.LevelsData
import com.example.sweetquest.data.UserProgressEntity
import com.example.sweetquest.ui.components.TopStatsBar

@Composable
fun WorldMapScreen(
  userProgress: UserProgressEntity,
  levelProgressMap: Map<Int, LevelProgressEntity>,
  onLevelClick: (Int) -> Unit,
  onBackClick: () -> Unit,
  onAddLivesClick: () -> Unit,
  onAddCoinsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val worlds = listOf(
    Pair("Candy Meadows", 1..5),
    Pair("Jelly Forest", 6..10),
    Pair("Cookie Mountains", 11..15),
    Pair("Caramel Falls", 16..20),
    Pair("Licorice Labyrinth", 21..25),
    Pair("Grand Confectionery", 26..30)
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            Color(0xFF81D4FA),
            Color(0xFFC5CAE9),
            Color(0xFFF8BBD0),
            Color(0xFFFFECB3)
          )
        )
      )
      .testTag("world_map_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      TopStatsBar(
        userProgress = userProgress,
        onAddLivesClick = onAddLivesClick,
        onAddCoinsClick = onAddCoinsClick,
        onBackClick = onBackClick
      )

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 16.dp),
        reverseLayout = true // Level 1 starts at bottom like candy games
      ) {
        worlds.forEach { (worldName, range) ->
          item {
            WorldHeader(worldName = worldName)
          }

          items(range.toList()) { levelNum ->
            val isUnlocked = levelNum <= userProgress.unlockedLevel
            val progress = levelProgressMap[levelNum]
            val isCurrent = levelNum == userProgress.unlockedLevel

            LevelNodeItem(
              levelNumber = levelNum,
              isUnlocked = isUnlocked,
              isCurrent = isCurrent,
              stars = progress?.starsEarned ?: 0,
              onClick = { if (isUnlocked) onLevelClick(levelNum) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun WorldHeader(worldName: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
      elevation = CardDefaults.cardElevation(4.dp)
    ) {
      Text(
        text = "— $worldName —",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF6A1B9A),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )
    }
  }
}

@Composable
private fun LevelNodeItem(
  levelNumber: Int,
  isUnlocked: Boolean,
  isCurrent: Boolean,
  stars: Int,
  onClick: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "current_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.14f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "current_pulse_scale"
  )

  // Alternate alignment for zigzag winding path
  val alignment = when (levelNumber % 3) {
    0 -> Alignment.CenterEnd
    1 -> Alignment.CenterStart
    else -> Alignment.Center
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    contentAlignment = alignment
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .clickable(enabled = isUnlocked) { onClick() }
        .testTag("level_node_$levelNumber")
    ) {
      Box(
        modifier = Modifier
          .size(62.dp)
          .scale(if (isCurrent) pulseScale else 1f)
          .shadow(if (isCurrent) 10.dp else 4.dp, CircleShape)
          .clip(CircleShape)
          .background(
            when {
              isCurrent -> Brush.radialGradient(listOf(Color(0xFFFF4081), Color(0xFFC2185B)))
              isUnlocked -> Brush.radialGradient(listOf(Color(0xFF00E676), Color(0xFF00C853)))
              else -> Brush.radialGradient(listOf(Color(0xFFBDBDBD), Color(0xFF757575)))
            }
          )
          .border(
            width = if (isCurrent) 3.5.dp else 2.dp,
            color = if (isCurrent) Color(0xFFFFEB3B) else Color.White,
            shape = CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        if (!isUnlocked) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
          )
        } else {
          Text(
            text = "$levelNumber",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp
          )
        }
      }

      // Stars under level node
      if (isUnlocked) {
        Row(
          modifier = Modifier.padding(top = 4.dp),
          horizontalArrangement = Arrangement.Center
        ) {
          for (s in 1..3) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = if (s <= stars) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }
  }
}
