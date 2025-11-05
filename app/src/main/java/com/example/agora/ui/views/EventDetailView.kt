package com.example.agora.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.agora.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailView(
    eventId: String,
    eventViewModel: EventViewModel,
    navController: NavController
) {
    val event = eventViewModel.events.find { it.id == eventId }

    if (event == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Événement introuvable")
        }
        return
    }

    var photos by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(eventId) {
        photos = eventViewModel.loadEventPhotos(eventId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(event.name) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour"
                    )
                }
            }
        )

        if (photos.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos) { photo ->
                    AsyncImage(
                        model = photo,
                        contentDescription = event.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1.5f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(modifier = Modifier.padding(16.dp)) {
            if (event.isPromoted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(event.name, style = MaterialTheme.typography.titleLarge)
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Événement mis en avant",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(32.dp) // étoile plus grande
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Événement mis en avant par le créateur ⭐",
                    color = Color(0xFFFFD700),
                    fontSize = 14.sp
                )
            } else {
                Text(event.name, style = MaterialTheme.typography.titleLarge)
            }

            Text("${event.place} • ${event.cityName}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text(event.date, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Text(event.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
