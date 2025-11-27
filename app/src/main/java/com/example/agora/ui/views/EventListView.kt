package com.example.agora.ui.views

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.agora.viewmodel.EventViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.example.agora.ui.components.EventCard
import com.example.agora.ui.components.FilterDropdowns

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
