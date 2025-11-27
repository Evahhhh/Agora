package com.example.agora.navigation

sealed class AppRoutes(val route: String) {
    object Auth : AppRoutes("auth")
    object Home : AppRoutes("home?tab={tab}") {
        fun createRoute(tab: Int = 1) = "home?tab=$tab"
    }
    object EditEvent : AppRoutes("edit_event/{eventId}") {
        fun createRoute(eventId: String) = "edit_event/$eventId"
    }
}

sealed class HomeRoutes(val route: String) {
    object List : HomeRoutes("event_list")
    object Detail : HomeRoutes("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
}
