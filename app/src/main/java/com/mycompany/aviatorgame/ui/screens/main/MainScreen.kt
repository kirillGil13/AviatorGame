package com.mycompany.aviatorgame.ui.screens.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.mycompany.aviatorgame.ui.theme.ButtonTertiaryText
import com.mycompany.aviatorgame.ui.theme.CardBackground
import com.mycompany.aviatorgame.ui.theme.DarkBackground
import com.mycompany.aviatorgame.utils.formatCoins
import com.mycompany.aviatorgame.utils.isToday
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onPlayClick: () -> Unit,
    onShopClick: () -> Unit,
    onSettingsClick: () -> Unit,
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
        TopAppBar(
            title = {
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
                // Settings Button
                TextButton(
                    onClick = onSettingsClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ButtonTertiaryText,
                    ),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkBackground
            )
        )
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

            // Secondary buttons row - 3 кнопки
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
                            contentDescription = "Bonus",
                            modifier = Modifier.size(30.dp)
                        )

                        Text(
                            if (canClaimBonus) "BONUS" else "CLAIMED",
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