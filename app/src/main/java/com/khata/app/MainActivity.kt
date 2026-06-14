    package com.khata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.khata.app.core.navigation.KhataNavGraph
import com.khata.app.core.navigation.Screen
import com.khata.app.core.security.SecurePreferences
import com.khata.app.core.theme.KhataTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main Activity — Single Activity host for all Compose screens.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var securePreferences: SecurePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KhataTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val isOnboardingDone = remember {
                        securePreferences.getBoolean("onboarding_complete", false)
                    }
                    KhataNavGraph(
                        navController = navController,
                        startDestination = if (isOnboardingDone) Screen.Dashboard.route else Screen.Onboarding.route
                    )
                }
            }
        }
    }
}