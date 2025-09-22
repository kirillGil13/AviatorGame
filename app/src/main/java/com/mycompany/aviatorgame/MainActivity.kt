package com.mycompany.aviatorgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mycompany.aviatorgame.ui.screens.game.GameScreen
import com.mycompany.aviatorgame.ui.screens.main.MainScreen
import com.mycompany.aviatorgame.ui.screens.shop.ShopScreen
import com.mycompany.aviatorgame.ui.theme.AviatorGameTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AviatorGameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AviatorApp()
                }
            }
        }
    }
}

@Composable
fun AviatorApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onPlayClick = { navController.navigate("game") },
                onShopClick = { navController.navigate("shop") }
            )
        }
        composable("game") {
            GameScreen(
                onBackClick = { navController.popBackStack() },
                onShopClick = { navController.navigate("shop") }
            )
        }
        composable("shop") {
            ShopScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}