
package com.example.wildlife_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.wildlife_app.navigation.NavGraph
import com.example.wildlife_app.ui.splash.SplashScreen
import com.example.wildlife_app.ui.theme.WildlifeappTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WildlifeappTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }

                Crossfade(targetState = showSplash, label = "splash") { splashVisible ->
                    if (splashVisible) {
                        SplashScreen(onFinished = { showSplash = false })
                    } else {
                        NavGraph()
                    }
                }
            }
        }
    }
}
