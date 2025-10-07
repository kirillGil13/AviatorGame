package com.mycompany.aviatorgame.ui.screens.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycompany.aviatorgame.R
import com.mycompany.aviatorgame.ui.theme.AviatorRed
import com.mycompany.aviatorgame.ui.theme.ButtonPrimary
import com.mycompany.aviatorgame.ui.theme.ButtonSecondary
import com.mycompany.aviatorgame.ui.theme.ButtonSecondaryDisabled
import com.mycompany.aviatorgame.ui.theme.ButtonSecondaryDisabledText
import com.mycompany.aviatorgame.ui.theme.ButtonSecondaryText
import com.mycompany.aviatorgame.ui.theme.CardBackground
import com.mycompany.aviatorgame.ui.theme.DarkBackground
import com.mycompany.aviatorgame.ui.theme.MainGray
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
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo and Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.aviator_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(200.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "CRASH GAME",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    letterSpacing = 6.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "YOUR BALANCE",
                        color = Color.White.copy(alpha = 0.6f),
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
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            gameState.balance.formatCoins(),
                            color = Color(0xFFffd700),
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Play Button
            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "PLAY NOW",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
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
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Shop",
                            modifier = Modifier.size(30.dp)
                        )

                        Text(
                            "SHOP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ButtonSecondaryText
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
                        .weight(1f),
                    enabled = canClaimBonus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonSecondary,
                        disabledContainerColor = ButtonSecondaryDisabled
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (canClaimBonus) Icons.Default.CardGiftcard else Icons.Default.CheckCircle,
                            contentDescription = "Shop",
                            modifier = Modifier.size(30.dp)
                        )

                        Text(
                            if (canClaimBonus) "CLAIM BONUS" else "BONUS CLAIMED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canClaimBonus) ButtonSecondaryText else ButtonSecondaryDisabledText
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
            containerColor = CardBackground,
            title = {
                Text(
                    "🎉 DAILY BONUS!",
                    color = Color(0xFFE8E8E8),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "+ $bonusAmount",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFffd700)
                    )

                    Text(
                        "COINS ADDED!",
                        color = Color(0xFFE8E8E8),
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBonusDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("AWESOME!", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}