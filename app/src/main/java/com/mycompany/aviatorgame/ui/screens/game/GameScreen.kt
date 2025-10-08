package com.mycompany.aviatorgame.ui.screens.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.mycompany.aviatorgame.ui.theme.ButtonCta
import com.mycompany.aviatorgame.ui.theme.ButtonCtaDisabled
import com.mycompany.aviatorgame.ui.theme.ButtonCtaDisabledText
import com.mycompany.aviatorgame.ui.theme.ButtonCtaText
import com.mycompany.aviatorgame.ui.theme.ButtonSecondary
import com.mycompany.aviatorgame.ui.theme.ButtonSecondaryText
import com.mycompany.aviatorgame.ui.theme.ButtonTertiaryText
import com.mycompany.aviatorgame.ui.theme.DarkBackground
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

    // Автоматическое завершение игры при уходе с экрана
    DisposableEffect(Unit) {
        onDispose {
            // ВСЕГДА сбрасываем игру при уходе с экрана
            viewModel.forceGameEnd()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                },
                navigationIcon = {
                    TextButton(
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = ButtonTertiaryText,
                        ),
                        onClick = onBackClick
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                                contentDescription = "Back",
                            )

                            Text("Back", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                actions = {
                    // Balance
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF242938)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("💰", fontSize = 16.sp)
                            Text(
                                gameState.balance.formatCoins(),
                                color = Color(0xFFffd700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Button(
                        onClick = onShopClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonSecondary
                        ),
                        modifier = Modifier.padding(end = 8.dp).height(35.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SHOP", color = ButtonSecondaryText, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )

            // Game Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DarkBackground)
            ) {
                AviatorGameCanvas(
                    multiplier = gameState.currentMultiplier,
                    isPlaying = gameState.isPlaying,
                    isCrashed = gameState.isCrashed,
                    shouldPlayCrashAnimation = gameState.shouldPlayCrashAnimation,
                    onCrashAnimationComplete = {
                        viewModel.markCrashAnimationPlayed()
                    }
                )

                // Current Multiplier Display
                if (gameState.isPlaying || gameState.isCrashed) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 40.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (gameState.isCrashed) {
                                Text(
                                    "FLEW AWAY!",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFff4757)
                                )
                                Text(
                                    gameState.currentMultiplier.formatMultiplier(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFff4757).copy(alpha = 0.7f)
                                )
                            } else {
                                Text(
                                    gameState.currentMultiplier.formatMultiplier(),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Betting Controls Section - БЕЗ СКРОЛЛА, ВСЕ В ПОЛЕ ЗРЕНИЯ
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bet 1
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

                // Bet 2
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

                // Start Game Button - ВСЕГДА ВИДНА
                Button(
                    onClick = viewModel::startGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !gameState.isPlaying && gameState.activeBets.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonCta,
                        disabledContainerColor = ButtonCtaDisabled
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (gameState.activeBets.isNotEmpty()) {
                            "START ROUND (${gameState.activeBets.sumOf { it.amount }} coins)"
                        } else {
                            "PLACE BET TO START"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!gameState.isPlaying && gameState.activeBets.isNotEmpty()) {
                            ButtonCtaText
                        } else {
                            ButtonCtaDisabledText
                        }
                    )
                }
            }
        }
    }
}