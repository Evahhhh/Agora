package com.example.agora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agora.ui.theme.AgoraTheme
import com.example.agora.ui.views.AuthScreen
import com.example.agora.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgoraTheme {
                val authViewModel: AuthViewModel = viewModel()
                AuthScreen(authViewModel)
            }
        }
    }
}
