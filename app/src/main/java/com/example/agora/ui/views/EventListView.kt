package com.example.agora.ui.views

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.agora.viewmodel.EventUI
import com.example.agora.viewmodel.EventViewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star

@Composable
fun EventListView(
    eventViewModel: EventViewModel,
    navController: NavController,
    auth: FirebaseAuth,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        eventViewModel.loadEvents(onSuccess = {}, onError = {})
        if (eventViewModel.events.isEmpty()) {
            eventViewModel.loadEvents(
                onSuccess = { isLoading = false },
                onError = {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            )
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FilterDropdowns(eventViewModel)
                Spacer(Modifier.height(12.dp))
            }

            items(eventViewModel.filteredEvents) { event ->
                EventCard(event = event, navController = navController)
            }
        }
    }
}

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
                        .size(48.dp) // <- taille augmentée
                        .align(Alignment.TopEnd)
                        .padding(12.dp) // léger padding pour décaler de la bordure
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdowns(eventViewModel: EventViewModel) {
    val types = remember { mutableStateListOf<String>() }
    val cities = remember { mutableStateListOf<String>() }

    var typeExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(eventViewModel.events) {
        types.clear()
        eventViewModel.events.flatMap { it.types }.distinct().let { types.addAll(it) }

        cities.clear()
        eventViewModel.events.map { it.cityName }.distinct().let { cities.addAll(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded }
        ) {
            TextField(
                value = eventViewModel.selectedType ?: "Tous les types",
                onValueChange = {},
                readOnly = true,
                label = { Text("Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Tous les types") },
                    onClick = {
                        eventViewModel.setFilter(null, eventViewModel.selectedCities)
                        typeExpanded = false
                    }
                )
                types.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            eventViewModel.setFilter(type, eventViewModel.selectedCities)
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = cityExpanded,
            onExpandedChange = { cityExpanded = !cityExpanded }
        ) {
            TextField(
                value = if (eventViewModel.selectedCities.isEmpty()) "Toutes les villes"
                else eventViewModel.selectedCities.joinToString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Ville") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = cityExpanded,
                onDismissRequest = { cityExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Toutes les villes") },
                    onClick = {
                        eventViewModel.setFilter(eventViewModel.selectedType, emptyList())
                        cityExpanded = false
                    }
                )
                cities.forEach { city ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = city in eventViewModel.selectedCities,
                                    onCheckedChange = { checked ->
                                        val newCities = if (checked) eventViewModel.selectedCities + city
                                        else eventViewModel.selectedCities - city
                                        eventViewModel.setFilter(eventViewModel.selectedType, newCities)
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(city)
                            }
                        },
                        onClick = {}
                    )
                }
            }
        }
    }
}
