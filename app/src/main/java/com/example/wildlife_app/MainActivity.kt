
package com.example.wildlife_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.wildlife_app.navigation.NavGraph
import com.example.wildlife_app.ui.theme.WildlifeappTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WildlifeappTheme {
                NavGraph()
            }
        }
    }
}
