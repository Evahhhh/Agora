package com.example.agora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.agora.ui.theme.AgoraTheme
import com.example.agora.ui.views.HomeScreen

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgoraTheme {
                HomeScreen()
            }
        }
    }
}
