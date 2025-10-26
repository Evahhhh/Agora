package com.example.agora.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

data class EventUI(
    val id: String,
    val name: String,
    val description: String,
    val place: String,
    val date: String,
    val cityName: String,
    val departmentName: String,
    val types: List<String>,
    val imageUrl: String? = null
)

class EventViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var events: List<EventUI> = emptyList()
        private set

    var filteredEvents: List<EventUI> by mutableStateOf(emptyList())
        private set

    // Valeurs de filtre
    var selectedType: String? by mutableStateOf(null)
    var selectedCity: String? by mutableStateOf(null)

    fun loadEvents(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val eventDocs = db.collection("event").get().await()
                val list = mutableListOf<EventUI>()

                for (doc in eventDocs) {
                    val name = doc.getString("name") ?: "Sans nom"
                    val description = doc.getString("description") ?: ""
                    val place = doc.getString("place") ?: ""
                    val date = doc.getTimestamp("date")?.toDate()?.toString() ?: "Date inconnue"

                    // Ville + département
                    val cityRef = doc.getDocumentReference("city")
                    var cityName = "Inconnue"
                    var departmentName = "Inconnu"
                    if (cityRef != null) {
                        val cityDoc = cityRef.get().await()
                        cityName = cityDoc.getString("name") ?: "Ville inconnue"
                        val depRef = cityDoc.getDocumentReference("department")
                        if (depRef != null) {
                            val depDoc = depRef.get().await()
                            departmentName = depDoc.getString("name") ?: "Département inconnu"
                        }
                    }

                    // Types
                    val typeRefs = doc.get("types") as? List<*> ?: emptyList<Any>()
                    val typeNames = mutableListOf<String>()
                    for (typeRef in typeRefs) {
                        val ref = typeRef as? com.google.firebase.firestore.DocumentReference
                        ref?.get()?.await()?.getString("name")?.let { typeNames.add(it) }
                    }

                    // Image
                    val photoQuery = db.collection("photo")
                        .whereEqualTo("event", doc.reference)
                        .limit(1)
                        .get()
                        .await()
                    val imageUrl = if (photoQuery.documents.isNotEmpty()) {
                        photoQuery.documents.first().getString("file_url")
                    } else null

                    list.add(
                        EventUI(
                            id = doc.id,
                            name = name,
                            description = description,
                            place = place,
                            date = date,
                            cityName = cityName,
                            departmentName = departmentName,
                            types = typeNames,
                            imageUrl = imageUrl
                        )
                    )
                }

                // 🔹 Tri décroissant par date
                events = list.sortedByDescending { it.date }
                applyFilters()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Erreur de chargement des événements")
            }
        }
    }

    fun applyFilters() {
        filteredEvents = events.filter { event ->
            val matchType = selectedType?.let { it in event.types } ?: true
            val matchCity = selectedCity?.let { it == event.cityName } ?: true
            matchType && matchCity
        }
    }

    fun setFilter(type: String? = null, city: String? = null) {
        selectedType = type
        selectedCity = city
        applyFilters()
    }


    suspend fun loadEventPhotos(eventId: String): List<String> {
        val photos = mutableListOf<String>()
        val eventRef = db.collection("event").document(eventId)

        try {
            val photoQuery = db.collection("photo")
                .whereEqualTo("event", eventRef)
                .get()
                .await()

            for (doc in photoQuery.documents) {
                doc.getString("file_url")?.let { photos.add(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (photos.isEmpty()) {
            photos.add("https://wallpaperbat.com/img/869012-aesthetic-green-background-minimalist-image-free-photo-png-stickers-wallpaper-background.jpg")
        }

        return photos
    }


}
