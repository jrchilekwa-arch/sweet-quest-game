package com.example.sweetquest.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sweetquest.model.BoosterType
import com.example.sweetquest.model.ScorePopup
import com.example.sweetquest.model.Tile
import com.example.sweetquest.model.TileType
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun BoardView(
  board: Array<Array<Tile?>>,
  selectedTile: Pair<Int, Int>?,
  activeBooster: BoosterType?,
  scorePopups: List<ScorePopup>,
  clearingTiles: Set<Pair<Int, Int>> = emptySet(),
  fallingTiles: Map<Pair<Int, Int>, Int> = emptyMap(),
  spawningTiles: Set<Pair<Int, Int>> = emptySet(),
  boardBgColor: Color = Color(0xFFFFF0F5),
  engineVersion: Int = 0,
  onTileClick: (Int, Int) -> Unit,
  onSwipe: (Int, Int, Int, Int) -> Unit,
  modifier: Modifier = Modifier
) {
  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(1f)
      .padding(8.dp)
      .shadow(10.dp, RoundedCornerShape(20.dp))
      .clip(RoundedCornerShape(20.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(
            boardBgColor,
            boardBgColor.copy(alpha = 0.95f)
          )
        )
      )
      .border(4.dp, Color(0xFFFFD54F), RoundedCornerShape(20.dp))
      .padding(6.dp)
      .testTag("match3_puzzle_board")
  ) {
    val cellSize = maxWidth / 8f

    // 8x8 Grid
    Column(modifier = Modifier.fillMaxSize()) {
      for (r in 0..7) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
          for (c in 0..7) {
            key(r, c, engineVersion) {
              val tile = board[r][c]
              val isSelected = selectedTile?.first == r && selectedTile?.second == c

              val isClearing = clearingTiles.contains(Pair(r, c))
              val dropDistance = fallingTiles[Pair(r, c)] ?: 0
              val isSpawning = spawningTiles.contains(Pair(r, c))

              val clearScale by animateFloatAsState(
                targetValue = if (isClearing) 1.25f else 1f,
                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                label = "clearScale"
              )
              val clearAlpha by animateFloatAsState(
                targetValue = if (isClearing) 0.15f else 1f,
                animationSpec = tween(durationMillis = 180),
                label = "clearAlpha"
              )

              val fallAnim = remember(tile?.id ?: "$r-$c-${tile?.color}") { Animatable(0f) }
              LaunchedEffect(dropDistance) {
                if (dropDistance > 0) {
                  fallAnim.snapTo(-dropDistance.toFloat())
                  fallAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                      dampingRatio = Spring.DampingRatioMediumBouncy,
                      stiffness = Spring.StiffnessLow
                    )
                  )
                }
              }

              val spawnAnim = remember(tile?.id ?: "$r-$c-${tile?.color}") { Animatable(0f) }
              LaunchedEffect(isSpawning) {
                if (isSpawning) {
                  spawnAnim.snapTo(-1.2f)
                  spawnAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                      dampingRatio = Spring.DampingRatioLowBouncy,
                      stiffness = Spring.StiffnessMedium
                    )
                  )
                }
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .aspectRatio(1f)
                  .padding(1.5.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    when {
                      tile == null -> Color.Transparent
                      tile.tileType == TileType.JELLY_1 -> Color(0xFFFF80AB).copy(alpha = 0.45f)
                      tile.tileType == TileType.JELLY_2 -> Color(0xFFFF4081).copy(alpha = 0.65f)
                      (r + c) % 2 == 0 -> Color.White.copy(alpha = 0.35f)
                      else -> Color.White.copy(alpha = 0.18f)
                    }
                  )
                  .border(
                    width = if (isSelected) 3.dp else 0.5.dp,
                    color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp)
                  )
                  .pointerInput(r, c, engineVersion) {
                    var totalDragX = 0f
                    var totalDragY = 0f
                    var hasSwiped = false
                    detectDragGestures(
                      onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                        hasSwiped = false
                      },
                      onDragEnd = {
                        if (!hasSwiped) {
                          if (abs(totalDragX) >= 20f || abs(totalDragY) >= 20f) {
                            val targetR = if (abs(totalDragY) > abs(totalDragX)) {
                              if (totalDragY > 0) (r + 1).coerceAtMost(7) else (r - 1).coerceAtLeast(0)
                            } else r

                            val targetC = if (abs(totalDragX) >= abs(totalDragY)) {
                              if (totalDragX > 0) (c + 1).coerceAtMost(7) else (c - 1).coerceAtLeast(0)
                            } else c

                            if (targetR != r || targetC != c) {
                              onSwipe(r, c, targetR, targetC)
                            }
                          } else {
                            onTileClick(r, c)
                          }
                        }
                      },
                      onDragCancel = {
                        if (!hasSwiped && abs(totalDragX) < 15f && abs(totalDragY) < 15f) {
                          onTileClick(r, c)
                        }
                      },
                      onDrag = { _, dragAmount ->
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                        if (!hasSwiped && (abs(totalDragX) >= 24f || abs(totalDragY) >= 24f)) {
                          hasSwiped = true
                          val targetR = if (abs(totalDragY) > abs(totalDragX)) {
                            if (totalDragY > 0) (r + 1).coerceAtMost(7) else (r - 1).coerceAtLeast(0)
                          } else r

                          val targetC = if (abs(totalDragX) >= abs(totalDragY)) {
                            if (totalDragX > 0) (c + 1).coerceAtMost(7) else (c - 1).coerceAtLeast(0)
                          } else c

                          if (targetR != r || targetC != c) {
                            onSwipe(r, c, targetR, targetC)
                          }
                        }
                      }
                    )
                  }
                  .clickable {
                    onTileClick(r, c)
                  }
              ) {
                if (tile != null) {
                  CandyPiece(
                    tile = tile,
                    isSelected = isSelected,
                    modifier = Modifier
                      .fillMaxSize()
                      .offset {
                        val yPx = ((fallAnim.value + spawnAnim.value) * cellSize.toPx()).roundToInt()
                        IntOffset(0, yPx)
                      }
                      .graphicsLayer {
                        scaleX = clearScale
                        scaleY = clearScale
                        alpha = clearAlpha
                      }
                  )
                }
              }
            }
          }
        }
      }
    }

    // Floating Score & Combo Popups Overlay
    scorePopups.forEach { popup ->
      val xOffset = (popup.col * (maxWidth.value / 8f)).dp
      val yOffset = (popup.row * (maxHeight.value / 8f)).dp

      Box(
        modifier = Modifier
          .offset { IntOffset(xOffset.roundToPx(), yOffset.roundToPx()) }
          .padding(4.dp)
      ) {
        Text(
          text = popup.text,
          color = Color(popup.color),
          fontSize = if (popup.text.contains("!")) 16.sp else 13.sp,
          fontWeight = FontWeight.ExtraBold,
          modifier = Modifier
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        )
      }
    }
  }
}
