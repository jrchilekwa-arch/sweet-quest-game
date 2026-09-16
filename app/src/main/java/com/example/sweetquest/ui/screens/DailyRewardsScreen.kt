package com.example.sweetquest.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sweetquest.data.UserProgressEntity
import com.example.sweetquest.ui.components.TopStatsBar

@Composable
fun DailyRewardsScreen(
  userProgress: UserProgressEntity,
  onClaimReward: (Int) -> Unit,
  onBackClick: () -> Unit,
  onAddLivesClick: () -> Unit,
  onAddCoinsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val now = System.currentTimeMillis()
  val lastClaim = userProgress.lastDailyClaimDate
  val hoursSinceClaim = (now - lastClaim) / (1000 * 60 * 60)
  val isClaimable = hoursSinceClaim >= 20 || lastClaim == 0L

  val nextDayToClaim = if (isClaimable) {
    val next = userProgress.currentDailyStreak + 1
    if (next > 7) 1 else next
  } else {
    userProgress.currentDailyStreak
  }

  val rewardSchedule = listOf(
    Pair("50 Coins", ""),
    Pair("75 Coins", "+ Hammer"),
    Pair("100 Coins", ""),
    Pair("125 Coins", "+ Rocket"),
    Pair("150 Coins", ""),
    Pair("200 Coins", "+ Rainbow Orb"),
    Pair("350 Coins", "+ Lightning Burst!")
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            Color(0xFFFFF3E0),
            Color(0xFFFFE0B2),
            Color(0xFFFFCC80)
          )
        )
      )
      .testTag("daily_rewards_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      TopStatsBar(
        userProgress = userProgress,
        onAddLivesClick = onAddLivesClick,
        onAddCoinsClick = onAddCoinsClick,
        onBackClick = onBackClick
      )

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "DAILY REWARDS",
          fontSize = 24.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFFE65100)
        )

        Text(
          text = "Log in every day for free coins and boosters!",
          fontSize = 13.sp,
          color = Color(0xFF795548),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 7-day grid
        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          for (day in 1..6) {
            val (coins, bonus) = rewardSchedule[day - 1]
            val isClaimed = !isClaimable && day <= userProgress.currentDailyStreak
            val isCurrent = isClaimable && day == nextDayToClaim

            item {
              RewardCard(
                dayNumber = day,
                coinsText = coins,
                bonusText = bonus,
                isClaimed = isClaimed,
                isCurrent = isCurrent
              )
            }
          }

          // Day 7 Big Jackpot Card spanning across
          val (day7Coins, day7Bonus) = rewardSchedule[6]
          val isDay7Claimed = !isClaimable && userProgress.currentDailyStreak == 7
          val isDay7Current = isClaimable && nextDayToClaim == 7

          item(span = { GridItemSpan(3) }) {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isDay7Current) Color(0xFFFF9800) else Color.White
              ),
              elevation = CardDefaults.cardElevation(6.dp),
              modifier = Modifier
                .fillMaxWidth()
                .border(
                  width = if (isDay7Current) 3.dp else 1.dp,
                  color = if (isDay7Current) Color(0xFFFFD54F) else Color(0xFFFFCC80),
                  shape = RoundedCornerShape(16.dp)
                )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "DAY 7 GRAND JACKPOT",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (isDay7Current) Color.White else Color(0xFFE65100)
                  )
                  Text(
                    text = "$day7Coins $day7Bonus",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isDay7Current) Color(0xFFFFEB3B) else Color(0xFF2E7D32)
                  )
                }
                Icon(
                  imageVector = Icons.Default.CardGiftcard,
                  contentDescription = null,
                  tint = if (isDay7Current) Color(0xFFFFEB3B) else Color(0xFFFF9800),
                  modifier = Modifier.size(36.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Claim Button
        Button(
          onClick = { onClaimReward(nextDayToClaim) },
          enabled = isClaimable,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(56.dp)
            .testTag("claim_daily_reward_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isClaimable) "CLAIM DAY $nextDayToClaim REWARD" else "COME BACK TOMORROW!",
              fontSize = 16.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
          }
        }

        if (!isClaimable) {
          Text(
            text = "Next gift unlocks in ${24 - hoursSinceClaim} hours",
            fontSize = 12.sp,
            color = Color(0xFF757575),
            modifier = Modifier.padding(top = 8.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun RewardCard(
  dayNumber: Int,
  coinsText: String,
  bonusText: String,
  isClaimed: Boolean,
  isCurrent: Boolean
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse_reward")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.98f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "reward_scale"
  )

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = when {
        isClaimed -> Color(0xFFE0E0E0)
        isCurrent -> Color(0xFFFFF3E0)
        else -> Color.White
      }
    ),
    elevation = CardDefaults.cardElevation(if (isCurrent) 6.dp else 2.dp),
    modifier = Modifier
      .height(105.dp)
      .scale(if (isCurrent) pulseScale else 1f)
      .border(
        width = if (isCurrent) 2.5.dp else 1.dp,
        color = if (isCurrent) Color(0xFFFF9800) else Color(0xFFFFE0B2),
        shape = RoundedCornerShape(14.dp)
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Day $dayNumber",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF795548)
        )
        if (isClaimed) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Claimed",
            tint = Color(0xFF00C853),
            modifier = Modifier.size(14.dp)
          )
        }
      }

      Icon(
        imageVector = Icons.Default.MonetizationOn,
        contentDescription = null,
        tint = Color(0xFFFFB300),
        modifier = Modifier.size(24.dp)
      )

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = coinsText,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF333333)
        )
        if (bonusText.isNotEmpty()) {
          Text(
            text = bonusText,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE65100)
          )
        }
      }
    }
  }
}
