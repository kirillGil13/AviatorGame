package com.mycompany.aviatorgame.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycompany.aviatorgame.data.model.Bet
import com.mycompany.aviatorgame.utils.formatMultiplier

@Composable
fun BetControl(
    modifier: Modifier = Modifier,
    betNumber: Int,
    amount: Int,
    autoCashOut: Float?,
    activeBet: Bet?,
    isPlaying: Boolean,
    currentMultiplier: Float,
    onAmountChange: (Int) -> Unit,
    onAutoCashOutChange: (Float?) -> Unit,
    onPlaceBet: () -> Unit,
    onCancelBet: () -> Unit,
    onCashOut: () -> Unit,
    onDouble: () -> Unit,
    onHalve: () -> Unit
) {
    val cardColor by animateColorAsState(
        targetValue = when {
            activeBet?.cashedOut == true -> Color(0xFF00ff88).copy(alpha = 0.1f)
            activeBet != null && isPlaying -> Color(0xFF00d4ff).copy(alpha = 0.1f)
            else -> Color(0xFF1f2844)
        },
        label = "card_color"
    )

    val borderColor = when {
        activeBet?.cashedOut == true -> Color(0xFF00ff88)
        activeBet != null && isPlaying -> Color(0xFF00d4ff)
        else -> Color(0xFF2a3454)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(borderColor, borderColor.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "BET $betNumber",
                    color = Color(0xFF6a7490),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )

                if (activeBet != null && isPlaying && !activeBet.cashedOut) {
                    val pulseAnimation by rememberInfiniteTransition(label = "pulse").animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                Color(0xFF00ff88).copy(alpha = pulseAnimation),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "ACTIVE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bet amount input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Amount",
                    color = Color(0xFF8890a6),
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amount.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { onAmountChange(it) }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isPlaying && activeBet == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00d4ff),
                            unfocusedBorderColor = Color(0xFF2a3454),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.Gray,
                            disabledBorderColor = Color(0xFF2a3454).copy(alpha = 0.5f),
                            cursorColor = Color(0xFF00d4ff)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onHalve,
                            enabled = !isPlaying && activeBet == null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2a3454))
                                .size(48.dp)
                        ) {
                            Text("½", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = onDouble,
                            enabled = !isPlaying && activeBet == null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2a3454))
                                .size(48.dp)
                        ) {
                            Text("2x", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Auto cash out
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Auto Cash Out",
                    color = Color(0xFF8890a6),
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = autoCashOut?.toString() ?: "",
                    onValueChange = { value ->
                        if (value.isEmpty()) {
                            onAutoCashOutChange(null)
                        } else {
                            value.toFloatOrNull()?.let { onAutoCashOutChange(it) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPlaying && activeBet == null,
                    placeholder = {
                        Text(
                            "Optional (e.g. 2.00)",
                            color = Color(0xFF4a5268),
                            fontSize = 14.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00d4ff),
                        unfocusedBorderColor = Color(0xFF2a3454),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.Gray,
                        disabledBorderColor = Color(0xFF2a3454).copy(alpha = 0.5f),
                        cursorColor = Color(0xFF00d4ff)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Action button
            when {
                activeBet == null -> {
                    Button(
                        onClick = onPlaceBet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isPlaying && amount >= 10,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00d4ff),
                            disabledContainerColor = Color(0xFF2a3454)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "PLACE BET",
                            fontWeight = FontWeight.Bold,
                            color = if (!isPlaying && amount >= 10) Color.Black else Color(0xFF4a5268)
                        )
                    }
                }
                !isPlaying -> {
                    Button(
                        onClick = onCancelBet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2a3454)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "CANCEL BET",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFff4757)
                        )
                    }
                }
                activeBet.cashedOut -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00ff88).copy(alpha = 0.2f),
                                        Color(0xFF00ff88).copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = Color(0xFF00ff88),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "WON ${activeBet.getWinAmount()} COINS",
                                color = Color(0xFF00ff88),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                "at ${activeBet.cashOutMultiplier?.formatMultiplier()}",
                                color = Color(0xFF00ff88).copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                else -> {
                    val potentialWin = (activeBet.amount * currentMultiplier).toInt()

                    Button(
                        onClick = onCashOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFffd700)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "CASH OUT",
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                "$potentialWin",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}