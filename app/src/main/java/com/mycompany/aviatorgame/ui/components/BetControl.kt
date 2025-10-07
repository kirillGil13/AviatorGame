package com.mycompany.aviatorgame.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycompany.aviatorgame.data.model.Bet
import com.mycompany.aviatorgame.ui.theme.ButtonCta
import com.mycompany.aviatorgame.ui.theme.ButtonCtaDisabled
import com.mycompany.aviatorgame.ui.theme.ButtonCtaDisabledText
import com.mycompany.aviatorgame.ui.theme.ButtonCtaText
import com.mycompany.aviatorgame.ui.theme.ButtonPrimary
import com.mycompany.aviatorgame.ui.theme.ButtonPrimaryDisabled
import com.mycompany.aviatorgame.ui.theme.ButtonPrimaryDisabledText
import com.mycompany.aviatorgame.ui.theme.ButtonSecondary
import com.mycompany.aviatorgame.ui.theme.ButtonSecondaryDisabled
import com.mycompany.aviatorgame.ui.theme.ButtonSecondaryDisabledText
import com.mycompany.aviatorgame.ui.theme.ButtonSecondaryText
import com.mycompany.aviatorgame.ui.theme.CardBackground
import com.mycompany.aviatorgame.utils.formatMultiplier

// Компактный TextField с полным контролем padding
@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val borderColor = if (enabled) Color(0xFF3a3f4e) else Color(0xFF3a3f4e).copy(alpha = 0.5f)
    val textColor = if (enabled) Color.White else Color.Gray

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = TextStyle(
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(Color(0xFF00d47e)),
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
    ) { innerTextField ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    color = Color(0xFF4a5268),
                    fontSize = 12.sp
                )
            }
            innerTextField()
        }
    }
}

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
    var selectedTab by remember { mutableIntStateOf(0) }

    val cardColor by animateColorAsState(
        targetValue = when {
            activeBet?.cashedOut == true -> Color(0xFF00d47e).copy(alpha = 0.15f)
            else -> CardBackground
        },
        label = "card_color"
    )

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
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header with tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "BET $betNumber",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )

                // Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1a1f2e)),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        TabButton(
                            text = "Bet",
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            enabled = !isPlaying && activeBet == null
                        )
                        TabButton(
                            text = "Auto",
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            enabled = !isPlaying && activeBet == null
                        )
                    }
                }
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> BetTab(
                    amount = amount,
                    onAmountChange = onAmountChange,
                    isPlaying = isPlaying,
                    activeBet = activeBet,
                    currentMultiplier = currentMultiplier,
                    onPlaceBet = onPlaceBet,
                    onCancelBet = onCancelBet,
                    onCashOut = onCashOut
                )
                1 -> AutoTab(
                    autoCashOut = autoCashOut,
                    onAutoCashOutChange = onAutoCashOutChange,
                    isPlaying = isPlaying,
                    activeBet = activeBet,
                    currentMultiplier = currentMultiplier,
                    onPlaceBet = onPlaceBet,
                    onCancelBet = onCancelBet,
                    onCashOut = onCashOut
                )
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .height(28.dp)
            .widthIn(min = 50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF3a3f4e) else Color.Transparent,
            disabledContainerColor = if (selected) Color(0xFF3a3f4e).copy(alpha = 0.5f) else Color.Transparent
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun BetTab(
    amount: Int,
    onAmountChange: (Int) -> Unit,
    isPlaying: Boolean,
    activeBet: Bet?,
    currentMultiplier: Float,
    onPlaceBet: () -> Unit,
    onCancelBet: () -> Unit,
    onCashOut: () -> Unit
) {
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left side - Input and quick bet buttons
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Input
            CompactTextField(
                value = amount.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { onAmountChange(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isPlaying && activeBet == null,
                keyboardType = KeyboardType.Number
            )

            // Quick bet buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickBetButton(
                        text = "100",
                        onClick = { onAmountChange(100) },
                        enabled = !isPlaying && activeBet == null,
                        modifier = Modifier.weight(1f)
                    )
                    QuickBetButton(
                        text = "200",
                        onClick = { onAmountChange(200) },
                        enabled = !isPlaying && activeBet == null,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickBetButton(
                        text = "500",
                        onClick = { onAmountChange(500) },
                        enabled = !isPlaying && activeBet == null,
                        modifier = Modifier.weight(1f)
                    )
                    QuickBetButton(
                        text = "1000",
                        onClick = { onAmountChange(1000) },
                        enabled = !isPlaying && activeBet == null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Right side - Bet button
        BetActionButton(
            activeBet = activeBet,
            isPlaying = isPlaying,
            currentMultiplier = currentMultiplier,
            amount = amount,
            onPlaceBet = onPlaceBet,
            onCancelBet = onCancelBet,
            onCashOut = onCashOut,
        )
    }
}

@Composable
fun AutoTab(
    autoCashOut: Float?,
    onAutoCashOutChange: (Float?) -> Unit,
    isPlaying: Boolean,
    activeBet: Bet?,
    currentMultiplier: Float,
    onPlaceBet: () -> Unit,
    onCancelBet: () -> Unit,
    onCashOut: () -> Unit
) {
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left side - Input and quick auto buttons
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Input
            CompactTextField(
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
                placeholder = "Auto (e.g. 2.0)",
                keyboardType = KeyboardType.Decimal
            )

            // Quick auto buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickBetButton(
                        text = "1.5",
                        onClick = { onAutoCashOutChange(1.5f) },
                        enabled = !isPlaying && activeBet == null,
                        modifier = Modifier.weight(1f)
                    )
                    QuickBetButton(
                        text = "2.0",
                        onClick = { onAutoCashOutChange(2.0f) },
                        enabled = !isPlaying && activeBet == null,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickBetButton(
                        text = "3.0",
                        onClick = { onAutoCashOutChange(3.0f) },
                        enabled = !isPlaying && activeBet == null,
                        modifier = Modifier.weight(1f)
                    )
                    QuickBetButton(
                        text = "5.0",
                        onClick = { onAutoCashOutChange(5.0f) },
                        enabled = !isPlaying && activeBet == null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        // Right side - Bet button
        BetActionButton(
            activeBet = activeBet,
            isPlaying = isPlaying,
            currentMultiplier = currentMultiplier,
            amount = 0, // Not used in this context
            onPlaceBet = onPlaceBet,
            onCancelBet = onCancelBet,
            onCashOut = onCashOut,
        )

    }
}

@Composable
fun QuickBetButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ButtonSecondary,
            disabledContainerColor = ButtonSecondaryDisabled
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) ButtonSecondaryText else ButtonSecondaryDisabledText
        )
    }
}

@Composable
fun BetActionButton(
    activeBet: Bet?,
    isPlaying: Boolean,
    currentMultiplier: Float,
    amount: Int,
    onPlaceBet: () -> Unit,
    onCancelBet: () -> Unit,
    onCashOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        activeBet == null -> {
            val enabled = !isPlaying && amount >= 10
            Button(
                onClick = onPlaceBet,
                modifier = modifier
                    .defaultMinSize(minHeight = 108.dp, minWidth = 180.dp),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonCta,
                    disabledContainerColor = ButtonCtaDisabled
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text(
                    "BET",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) ButtonCtaText else ButtonCtaDisabledText
                )
            }
        }
        !isPlaying -> {
            Button(
                onClick = onCancelBet,
                modifier = modifier
                    .defaultMinSize(minHeight = 108.dp, minWidth = 180.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text(
                    "CANCEL",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
        activeBet.cashedOut -> {
            Box(
                modifier = modifier
                    .background(
                        Color(0xFF00d47e).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFF00d47e),
                        shape = RoundedCornerShape(8.dp)
                    ).defaultMinSize(minHeight = 108.dp, minWidth = 180.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("✓", fontSize = 14.sp, color = Color(0xFF00d47e))
                    Text(
                        "+${activeBet.getWinAmount()}",
                        color = Color(0xFF00d47e),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
        else -> {
            val potentialWin = (activeBet.amount * currentMultiplier).toInt()

            Button(
                onClick = onCashOut,
                modifier = modifier
                    .defaultMinSize(minHeight = 108.dp, minWidth = 180.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFff6b00)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "CASH",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 9.sp
                    )
                    Text(
                        "$potentialWin",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}