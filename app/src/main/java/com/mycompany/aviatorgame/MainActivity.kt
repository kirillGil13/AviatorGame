package com.mycompany.aviatorgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mycompany.aviatorgame.data.local.SoundManager
import com.mycompany.aviatorgame.ui.screens.game.GameScreen
import com.mycompany.aviatorgame.ui.screens.main.MainScreen
import com.mycompany.aviatorgame.ui.screens.settings.SettingsScreen
import com.mycompany.aviatorgame.ui.screens.shop.ShopScreen
import com.mycompany.aviatorgame.ui.theme.AviatorGameTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация звуков
        soundManager.initBackgroundMusic(R.raw.background_music)
        soundManager.initAirplaneSound(R.raw.airplane_sound)

        setContent {
            AviatorGameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AviatorApp(soundManager)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        soundManager.playBackgroundMusic()
    }

    override fun onPause() {
        super.onPause()
        soundManager.pauseBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.releaseAll()
    }
}

@Composable
fun AviatorApp(soundManager: SoundManager) {
    val navController = rememberNavController()

    DisposableEffect(Unit) {
        soundManager.playBackgroundMusic()
        onDispose { }
    }

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onPlayClick = { navController.navigate("game") },
                onShopClick = { navController.navigate("shop") },
                onSettingsClick = { navController.navigate("settings") }
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
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}