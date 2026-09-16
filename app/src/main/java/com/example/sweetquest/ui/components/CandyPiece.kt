package com.example.sweetquest.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.sweetquest.model.CandyColor
import com.example.sweetquest.model.SpecialPiece
import com.example.sweetquest.model.Tile
import com.example.sweetquest.model.TileType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CandyPiece(
  tile: Tile,
  isSelected: Boolean = false,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  val rainbowRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(2000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rainbow"
  )

  val currentScale = if (isSelected) pulseScale else 1f

  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(2.dp)
      .scale(currentScale)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val center = Offset(w / 2f, h / 2f)

      when (tile.tileType) {
        TileType.BLOCKER -> {
          drawCookieBlocker(w, h)
          return@Canvas
        }
        TileType.INGREDIENT -> {
          drawSugarNutIngredient(w, h)
          return@Canvas
        }
        else -> {}
      }

      // Draw Candy or Special
      if (tile.special == SpecialPiece.RAINBOW_PRISM) {
        drawRainbowPrism(w, h, center, rainbowRotation)
      } else if (tile.color != null) {
        drawCandyBody(tile.color, w, h, center)
        // Draw special overlay if any
        when (tile.special) {
          SpecialPiece.HORIZONTAL_STRIPED -> drawHorizontalStripes(w, h)
          SpecialPiece.VERTICAL_STRIPED -> drawVerticalStripes(w, h)
          SpecialPiece.EXPLOSIVE_WRAPPED -> drawWrappedBow(w, h)
          else -> {}
        }
      }

      // Draw Licorice Cage if present
      if (tile.tileType == TileType.CAGE) {
        drawLicoriceCage(w, h)
      }

      // Selection glow ring
      if (isSelected) {
        drawCircle(
          color = Color.White.copy(alpha = 0.85f),
          radius = w * 0.46f,
          center = center,
          style = Stroke(width = 4.dp.toPx())
        )
      }
    }
  }
}

private fun DrawScope.drawCandyBody(color: CandyColor, w: Float, h: Float, center: Offset) {
  when (color) {
    CandyColor.RED -> {
      // Strawberry Ruby Gem: Rounded Diamond/Heart
      val path = Path().apply {
        moveTo(w * 0.5f, h * 0.12f)
        cubicTo(w * 0.88f, h * 0.12f, w * 0.95f, h * 0.5f, w * 0.5f, h * 0.88f)
        cubicTo(w * 0.05f, h * 0.5f, w * 0.12f, h * 0.12f, w * 0.5f, h * 0.12f)
        close()
      }
      drawPath(
        path = path,
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFFFF5252), Color(0xFFD50000), Color(0xFF8B0000)),
          center = Offset(w * 0.35f, h * 0.35f),
          radius = w * 0.55f
        )
      )
      // Specular highlight
      drawCircle(
        color = Color.White.copy(alpha = 0.7f),
        radius = w * 0.10f,
        center = Offset(w * 0.38f, h * 0.28f)
      )
    }
    CandyColor.YELLOW -> {
      // Lemon Drop: Glowing rounded drop / hexagon
      val r = w * 0.38f
      drawRoundRect(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFFFFF59D), Color(0xFFFFD600), Color(0xFFFF9100)),
          center = Offset(w * 0.35f, h * 0.35f),
          radius = r * 1.3f
        ),
        topLeft = Offset(center.x - r, center.y - r),
        size = Size(r * 2f, r * 2f),
        cornerRadius = CornerRadius(r * 0.5f, r * 0.5f)
      )
      // Highlight dot
      drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = w * 0.09f,
        center = Offset(w * 0.36f, h * 0.32f)
      )
    }
    CandyColor.GREEN -> {
      // Mint Emerald: Rounded square lozenge
      val r = w * 0.36f
      drawRoundRect(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFFB9F6CA), Color(0xFF00E676), Color(0xFF007E33)),
          center = Offset(w * 0.35f, h * 0.35f),
          radius = r * 1.3f
        ),
        topLeft = Offset(center.x - r, center.y - r),
        size = Size(r * 2f, r * 2f),
        cornerRadius = CornerRadius(r * 0.35f, r * 0.35f)
      )
      drawCircle(
        color = Color.White.copy(alpha = 0.7f),
        radius = w * 0.08f,
        center = Offset(w * 0.34f, h * 0.32f)
      )
    }
    CandyColor.ORANGE -> {
      // Caramel Swirl: Circle with inner swirl
      val r = w * 0.38f
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFFFFE082), Color(0xFFFF9800), Color(0xFFE65100)),
          center = Offset(w * 0.35f, h * 0.35f),
          radius = r * 1.2f
        ),
        radius = r,
        center = center
      )
      // Swirl accent
      drawArc(
        color = Color.White.copy(alpha = 0.65f),
        startAngle = 45f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - r * 0.55f, center.y - r * 0.55f),
        size = Size(r * 1.1f, r * 1.1f),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
      )
    }
    CandyColor.PURPLE -> {
      // Grape Amethyst: Oval drop
      val rX = w * 0.38f
      val rY = h * 0.34f
      drawOval(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFFE1BEE7), Color(0xFFAA00FF), Color(0xFF4A148C)),
          center = Offset(w * 0.35f, h * 0.35f),
          radius = rX * 1.3f
        ),
        topLeft = Offset(center.x - rX, center.y - rY),
        size = Size(rX * 2f, rY * 2f)
      )
      drawCircle(
        color = Color.White.copy(alpha = 0.75f),
        radius = w * 0.08f,
        center = Offset(w * 0.36f, h * 0.32f)
      )
    }
    CandyColor.BLUE -> {
      // Blueberry Bliss: Glossy sphere
      val r = w * 0.38f
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFF80D8FF), Color(0xFF2979FF), Color(0xFF0D47A1)),
          center = Offset(w * 0.35f, h * 0.35f),
          radius = r * 1.3f
        ),
        radius = r,
        center = center
      )
      drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = w * 0.10f,
        center = Offset(w * 0.34f, h * 0.30f)
      )
    }
  }
}

private fun DrawScope.drawHorizontalStripes(w: Float, h: Float) {
  val strokeW = 4.dp.toPx()
  val y1 = h * 0.36f
  val y2 = h * 0.64f
  drawLine(
    color = Color.White.copy(alpha = 0.9f),
    start = Offset(w * 0.15f, y1),
    end = Offset(w * 0.85f, y1),
    strokeWidth = strokeW,
    cap = StrokeCap.Round
  )
  drawLine(
    color = Color.White.copy(alpha = 0.9f),
    start = Offset(w * 0.15f, y2),
    end = Offset(w * 0.85f, y2),
    strokeWidth = strokeW,
    cap = StrokeCap.Round
  )
}

private fun DrawScope.drawVerticalStripes(w: Float, h: Float) {
  val strokeW = 4.dp.toPx()
  val x1 = w * 0.36f
  val x2 = w * 0.64f
  drawLine(
    color = Color.White.copy(alpha = 0.9f),
    start = Offset(x1, h * 0.15f),
    end = Offset(x1, h * 0.85f),
    strokeWidth = strokeW,
    cap = StrokeCap.Round
  )
  drawLine(
    color = Color.White.copy(alpha = 0.9f),
    start = Offset(x2, h * 0.15f),
    end = Offset(x2, h * 0.85f),
    strokeWidth = strokeW,
    cap = StrokeCap.Round
  )
}

private fun DrawScope.drawWrappedBow(w: Float, h: Float) {
  // Bow wings on left and right
  val pathL = Path().apply {
    moveTo(w * 0.2f, h * 0.5f)
    lineTo(w * 0.04f, h * 0.32f)
    lineTo(w * 0.04f, h * 0.68f)
    close()
  }
  val pathR = Path().apply {
    moveTo(w * 0.8f, h * 0.5f)
    lineTo(w * 0.96f, h * 0.32f)
    lineTo(w * 0.96f, h * 0.68f)
    close()
  }
  val bowColor = Color.White.copy(alpha = 0.85f)
  drawPath(pathL, bowColor)
  drawPath(pathR, bowColor)
  // Center wrapper ring
  drawCircle(
    color = Color(0xFFFFEB3B).copy(alpha = 0.85f),
    radius = w * 0.12f,
    center = Offset(w * 0.5f, h * 0.5f),
    style = Stroke(width = 2.5.dp.toPx())
  )
}

private fun DrawScope.drawRainbowPrism(w: Float, h: Float, center: Offset, angle: Float) {
  // Prismatic multi-color star / orb
  val r = w * 0.40f
  val rainbowColors = listOf(
    Color(0xFFFF1744),
    Color(0xFFFF9100),
    Color(0xFFFFEA00),
    Color(0xFF00E676),
    Color(0xFF00E5FF),
    Color(0xFFD500F9),
    Color(0xFFFF1744)
  )
  drawCircle(
    brush = Brush.sweepGradient(rainbowColors, center = center),
    radius = r,
    center = center
  )
  // Glowing core
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(Color.White, Color.White.copy(alpha = 0.4f), Color.Transparent),
      center = center,
      radius = r * 0.7f
    ),
    radius = r * 0.7f,
    center = center
  )
  // Star twinkle spikes
  val spikes = 8
  val innerR = r * 0.45f
  val outerR = r * 0.85f
  val starPath = Path()
  for (i in 0 until spikes * 2) {
    val currentR = if (i % 2 == 0) outerR else innerR
    val a = (i * PI / spikes) + (angle * PI / 180f)
    val x = center.x + (currentR * cos(a)).toFloat()
    val y = center.y + (currentR * sin(a)).toFloat()
    if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
  }
  starPath.close()
  drawPath(starPath, Color.White.copy(alpha = 0.85f))
}

private fun DrawScope.drawCookieBlocker(w: Float, h: Float) {
  val r = w * 0.42f
  val center = Offset(w / 2f, h / 2f)
  // Brown biscuit
  drawRoundRect(
    brush = Brush.radialGradient(
      colors = listOf(Color(0xFF8D6E63), Color(0xFF5D4037), Color(0xFF3E2723)),
      center = Offset(w * 0.4f, h * 0.4f),
      radius = r * 1.2f
    ),
    topLeft = Offset(center.x - r, center.y - r),
    size = Size(r * 2f, r * 2f),
    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
  )
  // Waffle grid lines
  val strokeW = 2.dp.toPx()
  drawLine(Color(0xFF3E2723), Offset(w * 0.33f, h * 0.2f), Offset(w * 0.33f, h * 0.8f), strokeW)
  drawLine(Color(0xFF3E2723), Offset(w * 0.66f, h * 0.2f), Offset(w * 0.66f, h * 0.8f), strokeW)
  drawLine(Color(0xFF3E2723), Offset(w * 0.2f, h * 0.33f), Offset(w * 0.8f, h * 0.33f), strokeW)
  drawLine(Color(0xFF3E2723), Offset(w * 0.2f, h * 0.66f), Offset(w * 0.8f, h * 0.66f), strokeW)
}

private fun DrawScope.drawSugarNutIngredient(w: Float, h: Float) {
  val center = Offset(w / 2f, h / 2f)
  val r = w * 0.38f
  // Golden Chestnut / Sugar Nut
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFF6D4C41)),
      center = Offset(w * 0.35f, h * 0.35f),
      radius = r * 1.2f
    ),
    radius = r,
    center = center
  )
  // Little green leaf stem on top
  drawOval(
    color = Color(0xFF4CAF50),
    topLeft = Offset(center.x - w * 0.12f, h * 0.08f),
    size = Size(w * 0.24f, h * 0.16f)
  )
  // Highlight shine
  drawCircle(
    color = Color.White.copy(alpha = 0.75f),
    radius = w * 0.08f,
    center = Offset(w * 0.38f, h * 0.35f)
  )
}

private fun DrawScope.drawLicoriceCage(w: Float, h: Float) {
  val strokeW = 3.dp.toPx()
  val cageColor = Color(0xFF212121).copy(alpha = 0.9f)
  drawLine(cageColor, Offset(w * 0.2f, h * 0.2f), Offset(w * 0.8f, h * 0.8f), strokeW)
  drawLine(cageColor, Offset(w * 0.8f, h * 0.2f), Offset(w * 0.2f, h * 0.8f), strokeW)
  drawCircle(
    color = Color(0xFFFFD700),
    radius = 3.dp.toPx(),
    center = Offset(w * 0.5f, h * 0.5f)
  )
}
