package com.mycompany.aviatorgame.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2a2a3e)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Bet $betNumber",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            // Bet amount input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.Gray
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = onHalve,
                    enabled = !isPlaying && activeBet == null
                ) {
                    Text("½", color = Color.White, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onDouble,
                    enabled = !isPlaying && activeBet == null
                ) {
                    Text("2x", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Auto cash out
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto:", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
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
                    placeholder = { Text("Auto cash out", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.Gray
                    ),
                    singleLine = true
                )
            }

            // Action button
            when {
                activeBet == null -> {
                    Button(
                        onClick = onPlaceBet,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isPlaying,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Place Bet")
                    }
                }
                !isPlaying -> {
                    Button(
                        onClick = onCancelBet,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFf44336)
                        )
                    ) {
                        Text("Cancel Bet")
                    }
                }
                activeBet.cashedOut -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text(
                            "Won: ${activeBet.getWinAmount()} at ${activeBet.cashOutMultiplier?.formatMultiplier()}",
                            modifier = Modifier.padding(12.dp),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                else -> {
                    Button(
                        onClick = onCashOut,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        )
                    ) {
                        Column {
                            Text("Cash Out", fontWeight = FontWeight.Bold)
                            Text(
                                "${(activeBet.amount * currentMultiplier).toInt()}",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}