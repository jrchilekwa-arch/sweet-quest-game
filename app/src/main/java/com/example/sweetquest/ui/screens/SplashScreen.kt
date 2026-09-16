package com.example.sweetquest.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onStartClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var progress by remember { mutableFloatStateOf(0f) }
  var isReady by remember { mutableFloatStateOf(0f) }

  val infiniteTransition = rememberInfiniteTransition(label = "mascot_bounce")
  val mascotScale by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(700, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "bounce"
  )

  LaunchedEffect(Unit) {
    for (i in 1..100) {
      delay(15)
      progress = i / 100f
    }
    isReady = 1f
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF4A148C),
            Color(0xFF7B1FA2),
            Color(0xFFC2185B),
            Color(0xFFFF4081)
          )
        )
      )
      .clickable {
        if (isReady >= 1f) onStartClick()
      }
      .testTag("splash_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Mascot or Banner Image
      Box(
        modifier = Modifier
          .size(160.dp)
          .scale(mascotScale)
          .shadow(16.dp, CircleShape)
          .clip(CircleShape)
          .background(Color.White)
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_sweet_mascot),
          contentDescription = "Sweet Quest Mascot",
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape),
          contentScale = ContentScale.Crop
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Game Title
      Text(
        text = "SWEET QUEST",
        fontSize = 38.sp,
        fontWeight = FontWeight.Black,
        color = Color(0xFFFFEB3B),
        textAlign = TextAlign.Center,
        letterSpacing = 2.sp,
        modifier = Modifier.shadow(8.dp)
      )

      Text(
        text = "Match-3 Puzzle Adventure",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = 0.95f),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(48.dp))

      if (isReady < 1f) {
        // Loading bar
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth(0.7f)
        ) {
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(12.dp)
              .clip(RoundedCornerShape(6.dp)),
            color = Color(0xFFFFD54F),
            trackColor = Color.White.copy(alpha = 0.3f)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Baking Candies ${(progress * 100).toInt()}%...",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      } else {
        // Tap to start button
        Button(
          onClick = onStartClick,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B)),
          shape = RoundedCornerShape(22.dp),
          modifier = Modifier
            .fillMaxWidth(0.75f)
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .testTag("splash_start_button")
        ) {
          Text(
            text = "TAP TO PLAY",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF4A148C),
            letterSpacing = 1.sp
          )
        }
      }
    }
  }
}
