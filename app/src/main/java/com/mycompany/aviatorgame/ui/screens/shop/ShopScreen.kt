package com.mycompany.aviatorgame.ui.screens.shop

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycompany.aviatorgame.data.local.BillingManager
import com.mycompany.aviatorgame.data.model.ShopItem
import com.mycompany.aviatorgame.utils.formatCoins

@Composable
fun ShopScreen(
    onBackClick: () -> Unit,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    val purchaseState by viewModel.purchaseState.collectAsState()
    val currentPurchaseState = purchaseState

    // Handle purchase state
    LaunchedEffect(purchaseState) {
        when (currentPurchaseState) {
            is BillingManager.PurchaseState.Success -> {
                viewModel.handlePurchaseSuccess(currentPurchaseState.coins)
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF0f0f1e)
                    )
                )
            )
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
                TextButton(onClick = onBackClick) {
                    Text("◀ Back", color = Color.White)
                }

                Text(
                    "SHOP",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            // Shop Items
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Get more coins to keep playing!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
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
        when (currentPurchaseState) {
            is BillingManager.PurchaseState.Processing -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is BillingManager.PurchaseState.Error -> {
                AlertDialog(
                    onDismissRequest = viewModel::resetPurchaseState,
                    containerColor = Color(0xFF2a2a3e),
                    title = {
                        Text("Purchase Failed", color = Color.White)
                    },
                    text = {
                        Text(currentPurchaseState.message, color = Color.White)
                    },
                    confirmButton = {
                        TextButton(onClick = viewModel::resetPurchaseState) {
                            Text("OK", color = Color(0xFF4CAF50))
                        }
                    }
                )
            }
            is BillingManager.PurchaseState.Success -> {
                AlertDialog(
                    onDismissRequest = viewModel::resetPurchaseState,
                    containerColor = Color(0xFF2a2a3e),
                    title = {
                        Text("Purchase Successful!", color = Color.White)
                    },
                    text = {
                        Column {
                            Text(
                                "🎉",
                                fontSize = 48.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "You received ${currentPurchaseState.coins.formatCoins()} coins!",
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = viewModel::resetPurchaseState) {
                            Text("Awesome!", color = Color(0xFF4CAF50))
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
            containerColor = Color(0xFF2a2a3e)
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
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onPurchase,
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    item.price,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}