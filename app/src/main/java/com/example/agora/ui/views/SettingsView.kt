package com.example.agora.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Intent
import com.example.agora.MainActivity
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsView(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Paramètres utilisateur")

        Spacer(Modifier.height(20.dp))

        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            context.startActivity(Intent(context, MainActivity::class.java))
            if (context is androidx.activity.ComponentActivity) context.finish()
        }) {
            Text("Se déconnecter")
        }
    }
}
