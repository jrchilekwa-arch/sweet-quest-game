package com.example.sweetquest.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.sweetquest.data.LevelProgressEntity
import com.example.sweetquest.data.LevelsData
import com.example.sweetquest.model.BoosterType
import com.example.sweetquest.model.LevelConfig
import com.example.sweetquest.ui.AdRewardType

@Composable
fun LevelPreviewDialog(
  levelNumber: Int,
  levelProgress: LevelProgressEntity?,
  livesRemaining: Int,
  onPlayClick: () -> Unit,
  onDismiss: () -> Unit
) {
  val level = LevelsData.getLevel(levelNumber)

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("level_preview_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Level $levelNumber",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF7B1FA2)
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF757575))
          }
        }

        Text(
          text = level.worldName,
          fontSize = 13.sp,
          color = Color(0xFF9E9E9E),
          fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Stars earned preview
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          val starsEarned = levelProgress?.starsEarned ?: 0
          for (i in 1..3) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = if (i <= starsEarned) Color(0xFFFFD700) else Color(0xFFE0E0E0),
              modifier = Modifier.size(36.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Objectives box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF8E1), RoundedCornerShape(16.dp))
            .border(1.5.dp, Color(0xFFFFE082), RoundedCornerShape(16.dp))
            .padding(14.dp)
        ) {
          Column {
            Text(
              text = "TARGET OBJECTIVES",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFF57F17)
            )
            Spacer(modifier = Modifier.height(6.dp))
            level.objectives.forEach { obj ->
              Text(
                text = "• ${obj.description}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF424242),
                modifier = Modifier.padding(vertical = 2.dp)
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Move Limit: ${level.movesLimit} moves",
              fontSize = 12.sp,
              color = Color(0xFF616161)
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Play button
        Button(
          onClick = onPlayClick,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("play_level_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("PLAY", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              Text(" -1", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          }
        }
      }
    }
  }
}

@Composable
fun PauseMenuDialog(
  soundEnabled: Boolean,
  musicEnabled: Boolean = true,
  hapticsEnabled: Boolean,
  onResume: () -> Unit,
  onRestart: () -> Unit,
  onToggleSound: () -> Unit,
  onToggleMusic: () -> Unit = {},
  onToggleHaptics: () -> Unit,
  onQuitToMap: () -> Unit
) {
  Dialog(onDismissRequest = onResume) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("pause_dialog")
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "GAME PAUSED",
          fontSize = 22.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF7B1FA2)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Toggles
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF7B1FA2))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sound FX", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
          }
          Switch(checked = soundEnabled, onCheckedChange = { onToggleSound() })
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF7B1FA2))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Music", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
          }
          Switch(checked = musicEnabled, onCheckedChange = { onToggleMusic() })
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Vibration, contentDescription = null, tint = Color(0xFF7B1FA2))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Haptic Feedback", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
          }
          Switch(checked = hapticsEnabled, onCheckedChange = { onToggleHaptics() })
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Buttons
        Button(
          onClick = onResume,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("resume_button")
        ) {
          Text("RESUME", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
          onClick = onRestart,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("restart_button")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text("RESTART LEVEL", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
          onClick = onQuitToMap,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("quit_to_map_button")
        ) {
          Text("QUIT TO MAP", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun LevelCompleteDialog(
  levelNumber: Int,
  stars: Int,
  score: Int,
  coinsBonus: Int,
  onNextLevel: () -> Unit,
  onMapClick: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "complete_pulse")
  val starScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "star_pulse"
  )

  Dialog(onDismissRequest = onMapClick) {
    Card(
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("level_complete_dialog")
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Banner text
        Text(
          text = "LEVEL COMPLETED!",
          fontSize = 24.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFFE91E63),
          textAlign = TextAlign.Center
        )

        Text(
          text = "Sugar Sweet Victory!",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF8E24AA)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Animated 3 Stars
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (i in 1..3) {
            val isEarned = i <= stars
            val scale = if (isEarned) starScale else 0.85f
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = if (isEarned) Color(0xFFFFD700) else Color(0xFFE0E0E0),
              modifier = Modifier
                .size(46.dp)
                .scale(scale)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Score card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3E5F5), RoundedCornerShape(16.dp))
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("FINAL SCORE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B1FA2))
            Text("$score", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF4A148C))

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("+$coinsBonus Coins Earned!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onNextLevel,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("next_level_button")
        ) {
          Text("NEXT LEVEL", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
          onClick = onMapClick,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("map_return_button")
        ) {
          Text("WORLD MAP", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun LevelFailedDialog(
  score: Int,
  userCoins: Int,
  onWatchAdForMoves: () -> Unit,
  onBuyMovesWithCoins: () -> Unit,
  onRetry: () -> Unit,
  onQuitToMap: () -> Unit
) {
  Dialog(onDismissRequest = onQuitToMap) {
    Card(
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("level_failed_dialog")
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "OUT OF MOVES!",
          fontSize = 24.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFFD32F2F)
        )

        Text(
          text = "Keep playing to complete your objectives!",
          fontSize = 13.sp,
          color = Color(0xFF616161),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Rewarded offer box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
            .border(1.5.dp, Color(0xFFA5D6A7), RoundedCornerShape(16.dp))
            .padding(14.dp)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "+5 EXTRA MOVES",
              fontSize = 16.sp,
              fontWeight = FontWeight.Black,
              color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Watch ad button
            Button(
              onClick = onWatchAdForMoves,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("watch_ad_for_moves_button")
            ) {
              Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Watch Free Ad (+5 Moves)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Buy with coins
            OutlinedButton(
              onClick = onBuyMovesWithCoins,
              enabled = userCoins >= 100,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("buy_moves_coins_button")
            ) {
              Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFB300))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Use 100 Coins", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("retry_level_button")
          ) {
            Text("RETRY", fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = onQuitToMap,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("quit_failed_button")
          ) {
            Text("MAP", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun RewardedAdDialog(
  countdown: Int,
  rewardType: AdRewardType
) {
  Dialog(onDismissRequest = {}) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)),
      elevation = CardDefaults.cardElevation(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
        .testTag("rewarded_ad_dialog")
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "REWARDED SPONSOR",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD54F)
          )

          Box(
            modifier = Modifier
              .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "Reward in ${countdown}s",
              fontSize = 12.sp,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Sponsor art
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(
              Brush.verticalGradient(listOf(Color(0xFF7B1FA2), Color(0xFFE91E63))),
              RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "Sweet Quest Studio",
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
            Text(
              text = "Enjoy endless confectionery match-3 fun!",
              fontSize = 12.sp,
              color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val prizeName = when (rewardType) {
              AdRewardType.EXTRA_MOVES -> "+5 Extra Moves"
              AdRewardType.EXTRA_LIFE -> "+1 Free Life"
              AdRewardType.FREE_BOOSTER -> "+1 Super Booster"
            }
            Text(
              text = "Unlocking $prizeName...",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFFFEB3B)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
          progress = { (5 - countdown) / 5f },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = Color(0xFF00E676),
          trackColor = Color(0xFF424242)
        )
      }
    }
  }
}

@Composable
fun QuickBuyBoosterDialog(
  boosterType: BoosterType,
  userCoins: Int,
  onBuyCoins: () -> Unit,
  onWatchAd: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = boosterType.displayName,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF7B1FA2)
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Text(
          text = boosterType.description,
          fontSize = 13.sp,
          color = Color(0xFF616161),
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = onBuyCoins,
          enabled = userCoins >= boosterType.coinCost,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Buy for ${boosterType.coinCost} Coins", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = onWatchAd,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Get Free with Ad", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
