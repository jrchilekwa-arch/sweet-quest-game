package com.example.sweetquest.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.sweetquest.data.UserProgressEntity
import com.example.sweetquest.ui.components.TopStatsBar

@Composable
fun HomeScreen(
  userProgress: UserProgressEntity,
  onPlayClick: () -> Unit,
  onWorldMapClick: () -> Unit,
  onShopClick: () -> Unit,
  onDailyRewardsClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onAddLivesClick: () -> Unit,
  onAddCoinsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "play_pulse")
  val buttonScale by infiniteTransition.animateFloat(
    initialValue = 0.97f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(650, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "play_scale"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFFE1BEE7),
            Color(0xFFF8BBD0),
            Color(0xFFFFF0F5)
          )
        )
      )
      .testTag("home_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top stats header
      TopStatsBar(
        userProgress = userProgress,
        onAddLivesClick = onAddLivesClick,
        onAddCoinsClick = onAddCoinsClick,
        onSettingsClick = onSettingsClick
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Hero Banner Image
      Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp)
          .padding(horizontal = 16.dp)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          Image(
            painter = painterResource(id = R.drawable.img_sweet_quest_banner),
            contentDescription = "Sweet Quest Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                )
              )
          )
          Column(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(14.dp)
          ) {
            Text(
              text = "Sweet Quest",
              color = Color.White,
              fontWeight = FontWeight.Black,
              fontSize = 22.sp
            )
            Text(
              text = "Current: Level ${userProgress.unlockedLevel} / 30",
              color = Color(0xFFFFEB3B),
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Mascot & Speech bubble
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Color(0xFFFF4081), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_sweet_mascot),
            contentDescription = "Mascot Pip",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
          modifier = Modifier
            .weight(1f)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFF48FB1), RoundedCornerShape(16.dp))
            .padding(12.dp)
        ) {
          Text(
            text = "Pip: \"Swap candies, trigger rainbows, and clear all 30 levels! Ready?\"",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF4A148C)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Big Play Button
      Button(
        onClick = onPlayClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier
          .fillMaxWidth(0.85f)
          .height(64.dp)
          .scale(buttonScale)
          .shadow(12.dp, RoundedCornerShape(26.dp))
          .testTag("home_play_button")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "PLAY LEVEL ${userProgress.unlockedLevel}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 1.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Main Navigation Cards Grid
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        NavCard(
          title = "World Map",
          subtitle = "30 Levels",
          icon = Icons.Default.Map,
          gradient = listOf(Color(0xFFAB47BC), Color(0xFF7B1FA2)),
          onClick = onWorldMapClick,
          modifier = Modifier.weight(1f),
          testTag = "home_world_map_button"
        )

        NavCard(
          title = "Daily Gift",
          subtitle = "Free Boosters",
          icon = Icons.Default.CardGiftcard,
          gradient = listOf(Color(0xFFFFB74D), Color(0xFFF57C00)),
          onClick = onDailyRewardsClick,
          modifier = Modifier.weight(1f),
          testTag = "home_daily_gift_button"
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        NavCard(
          title = "Sweet Shop",
          subtitle = "Themes & Packs",
          icon = Icons.Default.ShoppingBag,
          gradient = listOf(Color(0xFF26A69A), Color(0xFF00796B)),
          onClick = onShopClick,
          modifier = Modifier.weight(1f),
          testTag = "home_shop_button"
        )

        NavCard(
          title = "Settings",
          subtitle = "Audio & Stats",
          icon = Icons.Default.Settings,
          gradient = listOf(Color(0xFF5C6BC0), Color(0xFF3949AB)),
          onClick = onSettingsClick,
          modifier = Modifier.weight(1f),
          testTag = "home_settings_button"
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun NavCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  gradient: List<Color>,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    elevation = CardDefaults.cardElevation(5.dp),
    modifier = modifier
      .height(84.dp)
      .clickable { onClick() }
      .testTag(testTag)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Brush.horizontalGradient(gradient))
        .padding(12.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .background(Color.White.copy(alpha = 0.25f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text(text = subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
        }
      }
    }
  }
}
