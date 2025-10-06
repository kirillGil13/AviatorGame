package com.mycompany.aviatorgame.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycompany.aviatorgame.data.model.Bet
import com.mycompany.aviatorgame.ui.theme.CardBackground
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
            activeBet?.cashedOut == true -> Color(0xFF00d47e).copy(alpha = 0.15f)
            else -> CardBackground
        },
        label = "card_color"
    )

    // TODO
    val borderColor by animateColorAsState(
        targetValue = when {
            activeBet != null && isPlaying -> Color(0xFFBA6400)
            else -> CardBackground
        },
        label = "border_color"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        border = BorderStroke(
            1.dp,
            borderColor,
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Text(
                "BET $betNumber",
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )

            // Bet amount input with controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amount.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { onAmountChange(it) }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = !isPlaying && activeBet == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00d47e),
                        unfocusedBorderColor = Color(0xFF3a3f4e),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.Gray,
                        disabledBorderColor = Color(0xFF3a3f4e).copy(alpha = 0.5f),
                        cursorColor = Color(0xFF00d47e)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),

                )

                // Halve/Double buttons
                IconButton(
                    onClick = onHalve,
                    enabled = !isPlaying && activeBet == null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3a3f4e))
                ) {
                    Text("½", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onDouble,
                    enabled = !isPlaying && activeBet == null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3a3f4e))
                ) {
                    Text("2x", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Auto cash out
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = autoCashOut?.toString() ?: "",
                    onValueChange = { value ->
                        if (value.isEmpty()) {
                            onAutoCashOutChange(null)
                        } else {
                            value.toFloatOrNull()?.let { onAutoCashOutChange(it) }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isPlaying && activeBet == null,
                    placeholder = {
                        Text(
                            "Auto (e.g. 2.0)",
                            color = Color(0xFF4a5268),
                            fontSize = 12.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00d47e),
                        unfocusedBorderColor = Color(0xFF3a3f4e),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.Gray,
                        disabledBorderColor = Color(0xFF3a3f4e).copy(alpha = 0.5f),
                        cursorColor = Color(0xFF00d47e)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 13.sp,
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
                            .height(44.dp),
                        enabled = !isPlaying && amount >= 10,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isPlaying && amount >= 10)
                                Color(0xFF00d47e) else Color(0xFF3a3f4e),
                            disabledContainerColor = Color(0xFF3a3f4e)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "BET",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                !isPlaying -> {
                    Button(
                        onClick = onCancelBet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3a3f4e)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "CANCEL",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFff4757),
                            fontSize = 14.sp
                        )
                    }
                }
                activeBet.cashedOut -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(
                                Color(0xFF00d47e).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFF00d47e),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("✓", fontSize = 16.sp, color = Color(0xFF00d47e))
                            Text(
                                "+${activeBet.getWinAmount()}",
                                color = Color(0xFF00d47e),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
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
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFff6b00)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "CASH OUT",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                            Text(
                                "$potentialWin",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}