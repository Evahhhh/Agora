package com.example.agora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.agora.viewmodel.EventUI

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventCard(event: EventUI, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .heightIn(min = 280.dp)
            .clickable { navController.navigate("event_detail/${event.id}") },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box {
            AsyncImage(
                model = event.imageUrl ?: "https://wallpaperbat.com/img/869012-aesthetic-green-background-minimalist-image-free-photo-png-stickers-wallpaper-background.jpg",
                contentDescription = event.name,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )

            if (event.isPromoted) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Événement mis en avant",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(event.name, style = MaterialTheme.typography.titleLarge.copy(color = Color.White))
                Text("${event.place} • ${event.cityName}", style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray))
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (event.types.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    event.types.forEach { type -> AssistChip(onClick = {}, label = { Text(type) }) }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(event.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}
