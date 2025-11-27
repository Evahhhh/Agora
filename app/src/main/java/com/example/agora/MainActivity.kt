package com.example.agora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.agora.navigation.AgoraNavHost
import com.example.agora.ui.theme.AgoraTheme
import com.example.agora.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgoraTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                AgoraNavHost(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
