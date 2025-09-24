package com.mycompany.aviatorgame.ui.screens.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycompany.aviatorgame.utils.formatCoins
import com.mycompany.aviatorgame.utils.isToday
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

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

    // Анимации
    val infiniteTransition = rememberInfiniteTransition(label = "main")
    val planeAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "plane"
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = 200f
        ),
        label = "scale"
    )

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
        // Анимированный фон
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAnimatedBackground(planeAnimation)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                // Logo and Title
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(scaleAnimation)
                ) {
                    Text(
                        "✈️",
                        fontSize = 72.sp,
                        modifier = Modifier.scale(
                            1f + sin(planeAnimation * 6.28f) * 0.1f
                        )
                    )

                    Text(
                        "AVIATOR",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 4.sp
                    )

                    Text(
                        "CASH GAME",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00d4ff),
                        letterSpacing = 8.sp
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Balance Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1f2844)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1f2844),
                                        Color(0xFF2a3454)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "YOUR BALANCE",
                                color = Color(0xFF6a7490),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 2.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "💰",
                                    fontSize = 32.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    gameState.balance.formatCoins(),
                                    color = Color(0xFFffd700),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 36.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Play Button
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00ff88)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "PLAY NOW",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🚀", fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Secondary buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Shop Button
                    Button(
                        onClick = onShopClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00d4ff)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💎", fontSize = 20.sp)
                            Text(
                                "SHOP",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

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
                            .weight(1f)
                            .height(56.dp),
                        enabled = canClaimBonus,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canClaimBonus) Color(0xFFffd700) else Color(0xFF2a3454),
                            disabledContainerColor = Color(0xFF2a3454)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (canClaimBonus) "🎁" else "✓",
                                fontSize = 20.sp
                            )
                            Text(
                                if (canClaimBonus) "BONUS" else "CLAIMED",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canClaimBonus) Color.Black else Color(0xFF4a5268)
                            )
                            if (gameState.consecutiveDays > 0) {
                                Text(
                                    "Day ${gameState.consecutiveDays}",
                                    fontSize = 10.sp,
                                    color = if (canClaimBonus) Color.Black.copy(alpha = 0.7f) else Color(0xFF4a5268)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Bonus Dialog
    if (showBonusDialog && bonusAmount != null) {
        AlertDialog(
            onDismissRequest = { showBonusDialog = false },
            containerColor = Color(0xFF1f2844),
            title = {
                Text(
                    "🎉 DAILY BONUS!",
                    color = Color(0xFFffd700),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "+$bonusAmount",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00ff88)
                    )

                    Text(
                        "COINS ADDED!",
                        color = Color.White,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp
                    )

                    if (gameState.consecutiveDays < 7) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2a3454)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Day ${gameState.consecutiveDays} of 7 • Come back tomorrow!",
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFF6a7490),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBonusDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00d4ff)
                    )
                ) {
                    Text("AWESOME!", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

fun DrawScope.drawAnimatedBackground(animation: Float) {
    // Grid pattern
    val gridSize = 50f
    val lineColor = Color(0xFF1a2547).copy(alpha = 0.2f)

    for (x in 0..20) {
        drawLine(
            color = lineColor,
            start = Offset(x * gridSize, 0f),
            end = Offset(x * gridSize, size.height),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )
    }

    for (y in 0..20) {
        drawLine(
            color = lineColor,
            start = Offset(0f, y * gridSize),
            end = Offset(size.width, y * gridSize),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )
    }

    // Animated plane path
    val path = Path()
    val startX = -100f + animation * (size.width + 200f)
    val startY = size.height * 0.7f

    path.moveTo(startX, startY)

    for (i in 0..20) {
        val x = startX + i * 20f
        val y = startY - sin(i * 0.3f + animation * 6.28f) * 50f
        path.lineTo(x, y)
    }

    drawPath(
        path = path,
        color = Color(0xFF00d4ff).copy(alpha = 0.3f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 2f
        )
    )
}