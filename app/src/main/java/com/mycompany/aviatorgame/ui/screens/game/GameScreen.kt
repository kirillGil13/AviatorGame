package com.mycompany.aviatorgame.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycompany.aviatorgame.ui.components.BetControl
import com.mycompany.aviatorgame.ui.components.AviatorGameCanvas
import com.mycompany.aviatorgame.utils.formatCoins
import com.mycompany.aviatorgame.utils.formatMultiplier

@OptIn(ExperimentalMaterial3Api::class)
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
                        Color(0xFF0a0e27),
                        Color(0xFF1a1f3a)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = { },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("◀ Back", color = Color.White)
                    }
                },
                actions = {
                    // Balance
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1f2844)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💰", fontSize = 20.sp)
                            Text(
                                gameState.balance.formatCoins(),
                                color = Color(0xFFffd700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Button(
                        onClick = onShopClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00d4ff)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("+ Buy", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0a0e27).copy(alpha = 0.95f)
                )
            )

            // Game Canvas - фиксированная высота
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1a1f3a),
                                Color(0xFF0a0e27)
                            )
                        )
                    )
            ) {
                AviatorGameCanvas(
                    multiplier = gameState.currentMultiplier,
                    isPlaying = gameState.isPlaying,
                    isCrashed = gameState.isCrashed
                )

                // Current Multiplier Display
                if (gameState.isPlaying || gameState.isCrashed) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-50).dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (gameState.isCrashed) {
                                Text(
                                    "FLEW AWAY!",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFff4757)
                                )
                                Text(
                                    gameState.currentMultiplier.formatMultiplier(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFff4757).copy(alpha = 0.7f)
                                )
                            } else {
                                Text(
                                    gameState.currentMultiplier.formatMultiplier(),
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        gameState.currentMultiplier < 2f -> Color.White
                                        gameState.currentMultiplier < 5f -> Color(0xFF00ff88)
                                        gameState.currentMultiplier < 10f -> Color(0xFFffd700)
                                        else -> Color(0xFFff9500)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Waiting for next round",
                                fontSize = 24.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Place your bets!",
                                fontSize = 18.sp,
                                color = Color(0xFF00ff88).copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Betting Controls Section - с прокруткой
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Betting Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bet 1
                    Box(modifier = Modifier.weight(1f)) {
                        BetControl(
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
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bet 2
                    Box(modifier = Modifier.weight(1f)) {
                        BetControl(
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
                }

                // Start Game Button
                if (!gameState.isPlaying && gameState.activeBets.isNotEmpty()) {
                    Button(
                        onClick = viewModel::startGame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00ff88)
                        )
                    ) {
                        Text(
                            "START ROUND (${gameState.activeBets.sumOf { it.amount }} coins)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                // Add spacing at bottom for better scrolling
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
