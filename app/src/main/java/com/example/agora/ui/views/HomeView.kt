package com.example.agora.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.agora.viewmodel.EventViewModel
import com.example.agora.viewmodel.AdminViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen() {
    val auth = FirebaseAuth.getInstance()
    val eventViewModel: EventViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()
    val isAdmin by adminViewModel.isAdmin
    val navController = rememberNavController()

    var selectedItem by remember { mutableStateOf(1) }

    val baseItems = listOf("Ajouter", "Événements", "Mon Compte")
    val baseIcons = listOf(Icons.Default.Add, Icons.AutoMirrored.Filled.List, Icons.Default.AccountCircle)

    val items = if (isAdmin) listOf("Statistiques") + baseItems else baseItems
    val icons = if (isAdmin) listOf(Icons.Default.Info) + baseIcons else baseIcons

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
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            val offset = if (isAdmin) 1 else 0

            when (selectedItem) {
                0 -> if (isAdmin) {
                    AdminView(modifier = Modifier.fillMaxSize())
                } else {
                    AddEventView(
                        viewModel = viewModel(),
                        modifier = Modifier.fillMaxSize(),
                        eventViewModel = eventViewModel
                    )
                }

                0 + offset -> AddEventView(
                    viewModel = viewModel(),
                    modifier = Modifier.fillMaxSize(),
                    eventViewModel = eventViewModel
                )

                1 + offset -> {
                    LaunchedEffect(selectedItem) {
                        eventViewModel.refreshUserCities(auth)
                        eventViewModel.refreshPromotedStatus()
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "event_list",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("event_list") {
                            EventListView(
                                eventViewModel = eventViewModel,
                                navController = navController,
                                auth = auth
                            )
                        }
                        composable("event_detail/{eventId}") { backStackEntry ->
                            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                            EventDetailView(
                                eventId = eventId,
                                eventViewModel = eventViewModel,
                                navController = navController
                            )
                        }
                    }
                }

                2 + offset -> SettingsView(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
