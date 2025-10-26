package com.example.agora.ui.views

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.agora.viewmodel.EventViewModel

@Composable
fun HomeScreen() {
    var selectedItem by remember { mutableStateOf(1) } // par défaut sur Événements
    val items = listOf("Ajouter", "Événements", "Mon Compte")
    val icons = listOf(Icons.Default.Add, Icons.AutoMirrored.Filled.List, Icons.Default.AccountCircle)
    val eventViewModel: EventViewModel = viewModel()

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedItem) {
            0 -> AddEventView(
                viewModel = viewModel(),
                modifier = Modifier.padding(innerPadding)
            )
            2 -> SettingsView(Modifier.padding(innerPadding))
            1 -> {
                // NavHost pour la section Événements
                NavHost(
                    navController = navController,
                    startDestination = "event_list",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("event_list") {
                        EventListView(
                            eventViewModel = eventViewModel,
                            navController = navController
                        )
                    }
                    composable("event_detail/{eventId}") { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                        EventDetailView(eventId = eventId, eventViewModel = eventViewModel, navController = navController)
                    }
                }
            }
        }
    }
}
