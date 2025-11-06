package com.example.agora.ui.views

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import com.example.agora.viewmodel.EditEventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventView(eventId: String, viewModel: EditEventViewModel = viewModel()) {
    val context = LocalContext.current
    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val place by viewModel.place.collectAsState()
    val date by viewModel.date.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadEvent(eventId)
    }

    message?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        viewModel.clearMessage()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Modifier l'événement") },
            navigationIcon = {
                IconButton(onClick = {
                    if (context is ComponentActivity) context.finish()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.setName(it) },
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            OutlinedTextField(
                value = place,
                onValueChange = { viewModel.setPlace(it) },
                label = { Text("Lieu") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            OutlinedTextField(
                value = date,
                onValueChange = { viewModel.setDate(it) },
                label = { Text("Date (jj/MM/yyyy HH:mm)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.updateEvent(eventId)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077FF)),
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                if (loading)
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else
                    Text("Enregistrer les modifications")
            }
        }
    }
}
