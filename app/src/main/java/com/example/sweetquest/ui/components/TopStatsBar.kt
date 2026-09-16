package com.example.sweetquest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sweetquest.data.UserProgressEntity

@Composable
fun TopStatsBar(
  userProgress: UserProgressEntity,
  onAddLivesClick: () -> Unit,
  onAddCoinsClick: () -> Unit,
  onBackClick: (() -> Unit)? = null,
  onSettingsClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Back button if present
    if (onBackClick != null) {
      IconButton(
        onClick = onBackClick,
        modifier = Modifier
          .size(38.dp)
          .shadow(3.dp, CircleShape)
          .background(Color.White, CircleShape)
          .testTag("top_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = Color(0xFF7B1FA2),
          modifier = Modifier.size(20.dp)
        )
      }
    }

    // Lives Pill
    StatPill(
      icon = {
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = "Lives",
          tint = Color(0xFFE91E63),
          modifier = Modifier.size(18.dp)
        )
      },
      text = "${userProgress.lives}/5",
      onAddClick = onAddLivesClick,
      testTag = "lives_stat_pill"
    )

    // Coins Pill
    StatPill(
      icon = {
        Icon(
          imageVector = Icons.Default.MonetizationOn,
          contentDescription = "Coins",
          tint = Color(0xFFFFB300),
          modifier = Modifier.size(18.dp)
        )
      },
      text = "${userProgress.coins}",
      onAddClick = onAddCoinsClick,
      testTag = "coins_stat_pill"
    )

    // Stars Pill
    Box(
      modifier = Modifier
        .shadow(3.dp, RoundedCornerShape(16.dp))
        .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
        .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(16.dp))
        .padding(horizontal = 10.dp, vertical = 6.dp)
        .testTag("stars_stat_pill"),
      contentAlignment = Alignment.Center
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = "Stars",
          tint = Color(0xFFFFD700),
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${userProgress.totalStars}",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = Color(0xFF424242)
        )
      }
    }

    // Settings button if present
    if (onSettingsClick != null) {
      IconButton(
        onClick = onSettingsClick,
        modifier = Modifier
          .size(38.dp)
          .shadow(3.dp, CircleShape)
          .background(Color.White, CircleShape)
          .testTag("top_settings_button")
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = Color(0xFF7B1FA2),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
private fun StatPill(
  icon: @Composable () -> Unit,
  text: String,
  onAddClick: () -> Unit,
  testTag: String
) {
  Box(
    modifier = Modifier
      .shadow(3.dp, RoundedCornerShape(16.dp))
      .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
      .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
      .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
      .testTag(testTag)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      icon()
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = text,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = Color(0xFF333333)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Box(
        modifier = Modifier
          .size(22.dp)
          .clip(CircleShape)
          .background(Color(0xFF00C853))
          .clickable { onAddClick() },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add",
          tint = Color.White,
          modifier = Modifier.size(14.dp)
        )
      }
    }
  }
}
