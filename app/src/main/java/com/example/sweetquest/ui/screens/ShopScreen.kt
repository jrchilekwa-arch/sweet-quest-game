package com.example.sweetquest.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.sweetquest.model.BoosterType
import com.example.sweetquest.model.GameTheme
import com.example.sweetquest.ui.components.TopStatsBar

@Composable
fun ShopScreen(
  userProgress: UserProgressEntity,
  onBuyBooster: (BoosterType) -> Unit,
  onBuyPack: (String, Int) -> Unit,
  onBuyTheme: (GameTheme) -> Unit,
  onSelectTheme: (GameTheme) -> Unit,
  onWatchAdForCoins: () -> Unit,
  onPurchaseNoAds: () -> Unit,
  onBackClick: () -> Unit,
  onAddLivesClick: () -> Unit,
  onAddCoinsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val unlockedThemeIds = userProgress.unlockedThemes.split(",")

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            Color(0xFFE0F2F1),
            Color(0xFFE8F5E9),
            Color(0xFFFFF8E1)
          )
        )
      )
      .testTag("shop_screen")
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
            text = "SWEET SHOP",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF00695C),
            modifier = Modifier.padding(vertical = 10.dp)
          )
        }

        // Free Coins via Ad
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF43A047)),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Free Daily Coins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Watch a short sponsor video", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
              }
              Button(
                onClick = onWatchAdForCoins,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("watch_ad_coins_button")
              ) {
                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF1B5E20))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+50 COINS", color = Color(0xFF1B5E20), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
              }
            }
          }
        }

        // Booster Bundles
        item {
          SectionTitle("SPECIAL BOOSTER BUNDLES")
        }

        item {
          BundleCard(
            title = "Sweet Starter Pack",
            itemsDesc = "2 Hammers • 2 Rockets • 2 Shuffles",
            costCoins = 150,
            canAfford = userProgress.coins >= 150,
            onBuy = { onBuyPack("sweet_starter", 150) },
            testTag = "buy_starter_pack"
          )
        }

        item {
          BundleCard(
            title = "Master Chocolatier Box",
            itemsDesc = "3 Rainbow Orbs • 3 Lightnings • 3 Hammers",
            costCoins = 280,
            canAfford = userProgress.coins >= 280,
            onBuy = { onBuyPack("master_chocolatier", 280) },
            testTag = "buy_master_pack"
          )
        }

        // Individual Boosters
        item {
          SectionTitle("INDIVIDUAL BOOSTERS")
        }

        BoosterType.entries.forEach { booster ->
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(3.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(booster.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF333333))
                  Text(booster.description, fontSize = 11.sp, color = Color(0xFF757575))
                }
                Button(
                  onClick = { onBuyBooster(booster) },
                  enabled = userProgress.coins >= booster.coinCost,
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                  modifier = Modifier.testTag("buy_booster_${booster.name.lowercase()}")
                ) {
                  Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("${booster.coinCost}", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        // Themes
        item {
          SectionTitle("COSMETIC BOARD THEMES")
        }

        GameTheme.entries.forEach { theme ->
          val isUnlocked = unlockedThemeIds.contains(theme.id)
          val isSelected = userProgress.activeTheme == theme.id

          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(3.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(Color(theme.primaryColorHex))
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(theme.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(if (isUnlocked) "Owned" else "${theme.priceCoins} Coins", fontSize = 11.sp, color = Color(0xFF757575))
                  }
                }

                if (isSelected) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00C853))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ACTIVE", fontWeight = FontWeight.Bold, color = Color(0xFF00C853), fontSize = 12.sp)
                  }
                } else if (isUnlocked) {
                  OutlinedButton(
                    onClick = { onSelectTheme(theme) },
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Text("USE", fontWeight = FontWeight.Bold)
                  }
                } else {
                  Button(
                    onClick = { onBuyTheme(theme) },
                    enabled = userProgress.coins >= theme.priceCoins,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                  ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${theme.priceCoins}", fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }

        // No Ads Permanent Pass
        item {
          SectionTitle("PREMIUM PASS")
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Remove All Forced Ads", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("100% ad-free experience (coins purchase)", fontSize = 11.sp, color = Color(0xFF757575))
              }
              if (userProgress.noAdsPurchased) {
                Text("UNLOCKED", fontWeight = FontWeight.Bold, color = Color(0xFF00C853), fontSize = 12.sp)
              } else {
                Button(
                  onClick = onPurchaseNoAds,
                  enabled = userProgress.coins >= 500,
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                  Text("500 Coins", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }
}

@Composable
private fun SectionTitle(title: String) {
  Text(
    text = title,
    fontSize = 12.sp,
    fontWeight = FontWeight.ExtraBold,
    color = Color(0xFF00796B),
    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
  )
}

@Composable
private fun BundleCard(
  title: String,
  itemsDesc: String,
  costCoins: Int,
  canAfford: Boolean,
  onBuy: () -> Unit,
  testTag: String
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(4.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(title, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF004D40))
        Text(itemsDesc, fontSize = 12.sp, color = Color(0xFF00796B), fontWeight = FontWeight.Medium)
      }
      Button(
        onClick = onBuy,
        enabled = canAfford,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag(testTag)
      ) {
        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("$costCoins", fontWeight = FontWeight.Bold)
      }
    }
  }
}
