package com.example.agora.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agora.viewmodel.AdminViewModel

@Composable
fun AdminView(
    modifier: Modifier = Modifier,
    viewModel: AdminViewModel = viewModel()
) {
    val loading by viewModel.loading
    val totalUsers by viewModel.totalUsers
    val totalEvents by viewModel.totalEvents
    val upcomingEventsPercentage by viewModel.upcomingEventsPercentage
    val usersByDepartment by viewModel.usersByDepartment
    val eventsByDepartment by viewModel.eventsByDepartment
    val usersByCity by viewModel.usersByCity
    val eventsByCity by viewModel.eventsByCity
    val userDetails by viewModel.userDetails

    LaunchedEffect(Unit) {
        viewModel.loadAdminData()
    }

    if (loading) {
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
                Text("📊 Statistiques globales", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("Utilisateurs : $totalUsers")
                Text("Événements : $totalEvents (${String.format("%.1f", upcomingEventsPercentage)}% à venir)")
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("🗺️ Par département", style = MaterialTheme.typography.titleMedium)
                usersByDepartment.forEach { (dept, count) ->
                    Text(
                        "$dept : $count utilisateurs, ${eventsByDepartment[dept] ?: 0} événements"
                    )
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("🏙️ Par ville", style = MaterialTheme.typography.titleMedium)
                usersByCity.forEach { (city, count) ->
                    Text(
                        "$city : $count utilisateurs, ${eventsByCity[city] ?: 0} événements"
                    )
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("Détails par utilisateur", style = MaterialTheme.typography.headlineSmall)
            }

            items(userDetails) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${user.name} (${user.email})", style = MaterialTheme.typography.titleMedium)
                        Text("Villes : ${user.city}")
                        Text("Événements créés : ${user.eventCount}")
                        Text("Argent dépensé : ${user.moneySpent} €")
                    }
                }
            }
        }
    }
}
