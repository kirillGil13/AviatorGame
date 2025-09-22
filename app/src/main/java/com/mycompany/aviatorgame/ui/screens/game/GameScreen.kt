package com.mycompany.aviatorgame.ui.screens.game

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
import com.mycompany.aviatorgame.ui.components.BetControl
import com.mycompany.aviatorgame.ui.components.PlaneAnimation
import com.mycompany.aviatorgame.utils.formatCoins
import com.mycompany.aviatorgame.utils.formatMultiplier

@Composable
fun GameScreen(
    onBackClick: () -> Unit,
    onShopClick: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    val bet1Amount by viewModel.bet1Amount.collectAsState()
    val bet2Amount by viewModel.bet2Amount.collectAsState()
    val bet1AutoCashOut by viewModel.bet1AutoCashOut.collectAsState()
    val bet2AutoCashOut by viewModel.bet2AutoCashOut.collectAsState()

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
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackClick) {
                Text("◀ Back", color = Color.White)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Balance
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2a2a3e)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "💰",
                            fontSize = 20.sp
                        )
                        Text(
                            gameState.balance.formatCoins(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Button(
                    onClick = onShopClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("+ Buy Coins")
                }
            }
        }

        // Game Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Multiplier Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                if (gameState.isPlaying || gameState.isCrashed) {
                    PlaneAnimation(
                        multiplier = gameState.currentMultiplier,
                        isCrashed = gameState.isCrashed
                    )

                    Text(
                        text = if (gameState.isCrashed) {
                            "CRASHED at ${gameState.currentMultiplier.formatMultiplier()}"
                        } else {
                            gameState.currentMultiplier.formatMultiplier()
                        },
                        fontSize = if (gameState.isCrashed) 36.sp else 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gameState.isCrashed) Color.Red else Color.White
                    )
                } else {
                    Text(
                        "Place your bets!",
                        fontSize = 32.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Betting Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bet 1
                BetControl(
                    modifier = Modifier.weight(1f),
                    betNumber = 1,
                    amount = bet1Amount,
                    autoCashOut = bet1AutoCashOut,
                    activeBet = gameState.activeBets.find { it.id == 1 },
                    isPlaying = gameState.isPlaying,
                    currentMultiplier = gameState.currentMultiplier,
                    onAmountChange = viewModel::updateBet1Amount,
                    onAutoCashOutChange = viewModel::updateBet1AutoCashOut,
                    onPlaceBet = viewModel::placeBet1,
                    onCancelBet = { viewModel.cancelBet(1) },
                    onCashOut = { viewModel.cashOut(1) },
                    onDouble = viewModel::doubleBet1,
                    onHalve = viewModel::halveBet1
                )

                // Bet 2
                BetControl(
                    modifier = Modifier.weight(1f),
                    betNumber = 2,
                    amount = bet2Amount,
                    autoCashOut = bet2AutoCashOut,
                    activeBet = gameState.activeBets.find { it.id == 2 },
                    isPlaying = gameState.isPlaying,
                    currentMultiplier = gameState.currentMultiplier,
                    onAmountChange = viewModel::updateBet2Amount,
                    onAutoCashOutChange = viewModel::updateBet2AutoCashOut,
                    onPlaceBet = viewModel::placeBet2,
                    onCancelBet = { viewModel.cancelBet(2) },
                    onCashOut = { viewModel.cashOut(2) },
                    onDouble = viewModel::doubleBet2,
                    onHalve = viewModel::halveBet2
                )
            }

            // Start Game Button
            if (!gameState.isPlaying && gameState.activeBets.isNotEmpty()) {
                Button(
                    onClick = viewModel::startGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        "START GAME",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
