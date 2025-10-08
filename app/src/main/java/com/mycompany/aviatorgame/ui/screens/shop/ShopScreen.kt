package com.mycompany.aviatorgame.ui.screens.shop

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycompany.aviatorgame.data.local.BillingManager
import com.mycompany.aviatorgame.data.model.ShopItem
import com.mycompany.aviatorgame.ui.theme.ButtonCta
import com.mycompany.aviatorgame.ui.theme.ButtonPrimary
import com.mycompany.aviatorgame.ui.theme.ButtonTertiaryText
import com.mycompany.aviatorgame.ui.theme.CardBackground
import com.mycompany.aviatorgame.ui.theme.DarkBackground
import com.mycompany.aviatorgame.utils.formatCoins

@Composable
fun ShopScreen(
    onBackClick: () -> Unit,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    val purchaseState by viewModel.purchaseState.collectAsState()

    LaunchedEffect(purchaseState) {
        when (val state = purchaseState) {
            is BillingManager.PurchaseState.Success -> {
                viewModel.handlePurchaseSuccess(state.coins)
            }
            else -> {}
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            }

            Text(
                "Shop",
                color = Color.White,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
            )

            // Shop Items
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Get more coins to keep playing!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }

                items(viewModel.shopItems) { item ->
                    ShopItemCard(
                        item = item,
                        onPurchase = {
                            viewModel.purchaseItem(activity, item)
                        },
                        isProcessing = purchaseState is BillingManager.PurchaseState.Processing
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Purchase status
        when (val state = purchaseState) {
            is BillingManager.PurchaseState.Processing -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ButtonPrimary)
                }
            }
            is BillingManager.PurchaseState.Error -> {
                AlertDialog(
                    onDismissRequest = viewModel::resetPurchaseState,
                    containerColor = CardBackground,
                    title = {
                        Text("Purchase Failed", color = Color.White)
                    },
                    text = {
                        Text(state.message, color = Color.White.copy(alpha = 0.8f))
                    },
                    confirmButton = {
                        Button(
                            onClick = viewModel::resetPurchaseState,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("OK", color = Color.White)
                        }
                    }
                )
            }
            is BillingManager.PurchaseState.Success -> {
                AlertDialog(
                    onDismissRequest = viewModel::resetPurchaseState,
                    containerColor = CardBackground,
                    title = {
                        Text("Purchase Successful!", color = Color.White)
                    },
                    text = {
                        Column {
                            Text(
                                "🎉",
                                fontSize = 40.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "You received ${state.coins.formatCoins()} coins!",
                                fontSize = 18.sp,
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = viewModel::resetPurchaseState,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Awesome!", color = Color.White)
                        }
                    }
                )
            }
            else -> {}
        }
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    onPurchase: () -> Unit,
    isProcessing: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "💰",
                    fontSize = 32.sp
                )
                Column {
                    Text(
                        item.getTotalCoins().formatCoins(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.bonus > 0) {
                        Text(
                            "+${item.bonus}% bonus",
                            color = ButtonCta,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Button(
                onClick = onPurchase,
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    item.price,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}