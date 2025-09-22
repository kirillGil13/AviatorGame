package com.mycompany.aviatorgame.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycompany.aviatorgame.utils.formatCoins
import com.mycompany.aviatorgame.utils.isToday
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onPlayClick: () -> Unit,
    onShopClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    val scope = rememberCoroutineScope()
    var bonusAmount by remember { mutableStateOf<Int?>(null) }
    var showBonusDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF0f0f1e)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                "✈️ AVIATOR",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Balance
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2a2a3e)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("💰", fontSize = 32.sp)
                    Text(
                        gameState.balance.formatCoins(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Play Button
            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    "PLAY",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shop Button
            Button(
                onClick = onShopClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text(
                    "SHOP",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Bonus Button
            val canClaimBonus = !gameState.lastDailyBonusDate.isToday()
            Button(
                onClick = {
                    scope.launch {
                        bonusAmount = viewModel.claimDailyBonus()
                        if (bonusAmount != null) {
                            showBonusDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = canClaimBonus,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canClaimBonus) Color(0xFFFF9800) else Color.Gray
                )
            ) {
                Column {
                    Text(
                        if (canClaimBonus) "CLAIM DAILY BONUS" else "BONUS CLAIMED",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (gameState.consecutiveDays > 0) {
                        Text(
                            "Day ${gameState.consecutiveDays}",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Bonus Dialog
    if (showBonusDialog && bonusAmount != null) {
        AlertDialog(
            onDismissRequest = { showBonusDialog = false },
            containerColor = Color(0xFF2a2a3e),
            title = {
                Text(
                    "Daily Bonus!",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "🎉",
                        fontSize = 48.sp
                    )
                    Text(
                        "You received $bonusAmount coins!",
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    if (gameState.consecutiveDays < 7) {
                        Text(
                            "Come back tomorrow for more!",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBonusDialog = false }) {
                    Text("Awesome!", color = Color(0xFF4CAF50))
                }
            }
        )
    }
}