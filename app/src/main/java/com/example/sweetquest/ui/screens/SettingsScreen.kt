package com.example.sweetquest.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sweetquest.data.UserProgressEntity
import com.example.sweetquest.ui.components.TopStatsBar

@Composable
fun SettingsScreen(
  userProgress: UserProgressEntity,
  onToggleSound: () -> Unit,
  onToggleMusic: () -> Unit,
  onToggleHaptics: () -> Unit,
  onResetProgress: () -> Unit,
  onBackClick: () -> Unit,
  onAddLivesClick: () -> Unit,
  onAddCoinsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showResetDialog by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            Color(0xFFE8EAF6),
            Color(0xFFEDE7F6),
            Color(0xFFF3E5F5)
          )
        )
      )
      .testTag("settings_screen")
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
          .padding(horizontal = 16.dp)
      ) {
        item {
          Text(
            text = "SETTINGS & STATS",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF3949AB),
            modifier = Modifier.padding(vertical = 10.dp)
          )
        }

        // Audio & Haptics
        item {
          Text(
            text = "AUDIO & FEEDBACK",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF5C6BC0),
            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
          )
        }

        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              SettingToggleRow(
                icon = Icons.Default.VolumeUp,
                title = "Sound FX",
                subtitle = "Candy pops, combos & booster activations",
                iconTint = Color(0xFFFF4081),
                checked = userProgress.soundEnabled,
                testTag = "sound_fx_toggle",
                onCheckedChange = { onToggleSound() }
              )
              Spacer(modifier = Modifier.height(14.dp))
              SettingToggleRow(
                icon = Icons.Default.MusicNote,
                title = "Music",
                subtitle = "Whimsical sweet candy melodies",
                iconTint = Color(0xFF7C4DFF),
                checked = userProgress.musicEnabled,
                testTag = "music_toggle",
                onCheckedChange = { onToggleMusic() }
              )
              Spacer(modifier = Modifier.height(14.dp))
              SettingToggleRow(
                icon = Icons.Default.Vibration,
                title = "Haptic Feedback",
                subtitle = "Tactile vibration for matches and swaps",
                iconTint = Color(0xFF00B0FF),
                checked = userProgress.hapticsEnabled,
                testTag = "haptics_toggle",
                onCheckedChange = { onToggleHaptics() }
              )
            }
          }
        }

        // Lifetime Statistics
        item {
          Text(
            text = "PLAYER LIFETIME STATS",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF5C6BC0),
            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
          )
        }

        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              StatRow("Highest Level Reached", "Level ${userProgress.unlockedLevel} / 30")
              StatRow("Total Stars Collected", "${userProgress.totalStars} ⭐")
              StatRow("Highest Combo Streak", "${userProgress.highestCombo}x")
              StatRow("Candies Popped", "${userProgress.totalCandiesPopped}")
              StatRow("Total Matches Made", "${userProgress.totalMatchesMade}")
            }
          }
        }

        // How to Play Guide
        item {
          Text(
            text = "HOW TO PLAY RULES",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF5C6BC0),
            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
          )
        }

        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              RuleRow("Match 3 identical pieces", "Clears the pieces and earns score.")
              RuleRow("Match 4 in a straight line", "Creates a STRIPED CANDY (clears entire row or column).")
              RuleRow("Match 5 in an L or T shape", "Creates a WRAPPED CANDY (explodes 3x3 radius twice).")
              RuleRow("Match 5 in a straight row", "Creates a RAINBOW PRISM (clears all candies of matching color).")
              RuleRow("Swap two specials together", "Triggers MEGA chain reactions (e.g. Rainbow + Striped)! ")
              RuleRow("Break Blockers & Cages", "Make matches adjacent to blockers or cages to smash them.")
              RuleRow("Deliver Ingredients", "Clear candies underneath Hazelnut acorns so they drop to the bottom.")
            }
          }
        }

        // Reset Data
        item {
          Spacer(modifier = Modifier.height(20.dp))
          OutlinedButton(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("reset_progress_button")
          ) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset Game Progress", fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(30.dp))
        }
      }
    }

    if (showResetDialog) {
      AlertDialog(
        onDismissRequest = { showResetDialog = false },
        title = { Text("Reset All Progress?", fontWeight = FontWeight.Bold) },
        text = { Text("This will reset your level progress, stars, boosters, and coins back to level 1 defaults.") },
        confirmButton = {
          Button(
            onClick = {
              showResetDialog = false
              onResetProgress()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
          ) {
            Text("Reset Everything")
          }
        },
        dismissButton = {
          TextButton(onClick = { showResetDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }
  }
}

@Composable
private fun SettingToggleRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String? = null,
  iconTint: Color = Color(0xFF5C6BC0),
  checked: Boolean,
  testTag: String = "",
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      modifier = Modifier.weight(1f).padding(end = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF212121))
        if (subtitle != null) {
          Text(subtitle, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 13.sp)
        }
      }
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier
    )
  }
}

@Composable
private fun StatRow(title: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(title, fontSize = 13.sp, color = Color(0xFF616161))
    Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
  }
}

@Composable
private fun RuleRow(title: String, desc: String) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1A237E))
    Text(desc, fontSize = 11.sp, color = Color(0xFF424242))
  }
}
