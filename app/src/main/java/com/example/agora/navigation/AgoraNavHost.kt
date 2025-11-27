package com.example.agora.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.agora.ui.views.AuthScreen
import com.example.agora.ui.views.HomeScreen
import com.example.agora.viewmodel.AuthViewModel
import com.example.agora.viewmodel.EventViewModel
import com.example.agora.ui.views.EventListView
import com.example.agora.ui.views.EventDetailView
import com.example.agora.ui.views.EditEventView
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AgoraNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) AppRoutes.Home.createRoute() else AppRoutes.Auth.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppRoutes.Auth.route) {
            AuthScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(AppRoutes.Home.createRoute()) {
                        popUpTo(AppRoutes.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = AppRoutes.Home.route,
            arguments = listOf(androidx.navigation.navArgument("tab") { 
                type = androidx.navigation.NavType.IntType 
                defaultValue = 1 
            })
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getInt("tab") ?: 1
            HomeScreen(
                initialTab = tab,
                onEditEvent = { eventId ->
                    navController.navigate(AppRoutes.EditEvent.createRoute(eventId))
                }
            )
        }
        composable(AppRoutes.EditEvent.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EditEventView(
                eventId = eventId,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(AppRoutes.Home.createRoute(tab = 2)) {
                        popUpTo(AppRoutes.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun HomeNavHost(
    navController: NavHostController,
    eventViewModel: EventViewModel,
    auth: FirebaseAuth,
    onEditEvent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoutes.List.route,
        modifier = modifier
    ) {
        composable(HomeRoutes.List.route) {
            EventListView(
                eventViewModel = eventViewModel,
                navController = navController,
                auth = auth
            )
        }
        composable(HomeRoutes.Detail.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDetailView(
                eventId = eventId,
                eventViewModel = eventViewModel,
                navController = navController,
                onEditEvent = onEditEvent
            )
        }
    }
}
