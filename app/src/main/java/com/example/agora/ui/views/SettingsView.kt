package com.example.agora.ui.views

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agora.MainActivity
import com.example.agora.EditEventActivity
import com.example.agora.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@Composable
fun SettingsView(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.showMessage(null) // réinitialiser après affichage
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserData(auth)
        loading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                "Bonjour ${viewModel.userName} 👋",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(16.dp))

            if (loading) {
                CircularProgressIndicator()
            } else {
                Text("Villes de rattachement", style = MaterialTheme.typography.titleMedium)
                viewModel.allCities.forEach { (name, ref) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.toggleCitySelection(ref)
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name)
                        Checkbox(
                            checked = viewModel.userCities.contains(ref),
                            onCheckedChange = {
                                viewModel.toggleCitySelection(ref)
                            }
                        )
                    }
                }

                Button(
                    onClick = { viewModel.updateCities(auth) },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Mettre à jour mes villes")
                }

                Spacer(Modifier.height(20.dp))

                Text("Mes événements", style = MaterialTheme.typography.titleMedium)
                if (viewModel.userEvents.isEmpty()) {
                    Text("Aucun événement créé")
                } else {
                    LazyColumn {
                        items(viewModel.userEvents, key = { it.id }) { event ->
                            val now = Date()
                            val isPast = event.date?.before(now) ?: false
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPast) Color.LightGray else MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            event.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPast) Color.DarkGray else Color.Black
                                            )
                                        )
                                        if (event.isHighlighted) {
                                            Text(
                                                "⭐",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFFD700),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Lieu : ${event.place}",
                                        color = if (isPast) Color.Gray else Color.Black
                                    )
                                    Text(
                                        "Date : ${event.date?.let { sdf.format(it) } ?: "Inconnue"}",
                                        color = if (isPast) Color.Gray else Color.Black
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    if (event.isHighlighted) {
                                        Text(
                                            "Événement mis en avant ⭐",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier
                                                .background(Color(0xFFFFA000), shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (!event.isHighlighted && !isPast) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    viewModel.highlightEvent(auth, event.id)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077FF)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Mettre en avant (5€)", color = Color.White)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            val intent = Intent(context, EditEventActivity::class.java)
                                            intent.putExtra("eventId", event.id)
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Modifier", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                context.startActivity(Intent(context, MainActivity::class.java))
                if (context is androidx.activity.ComponentActivity) context.finish()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Se déconnecter", color = Color.White)
        }
    }
}
