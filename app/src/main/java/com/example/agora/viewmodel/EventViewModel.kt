package com.example.agora.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

data class EventUI(
    val id: String,
    val name: String,
    val description: String,
    val place: String,
    val date: String,
    val cityName: String,
    val departmentName: String,
    val types: List<String>,
    val imageUrl: String? = null,
    val timestamp: Date,
    val isPromoted: Boolean = false
)

class EventViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var events: List<EventUI> = emptyList()
        private set

    var filteredEvents: List<EventUI> by mutableStateOf(emptyList())
        private set

    var selectedType: String? by mutableStateOf(null)
    var selectedCities: List<String> by mutableStateOf(emptyList())

    fun loadEvents(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val payments = db.collection("payment").get().await()
                val promotedEventIds = payments.mapNotNull {
                    val ref = it.getDocumentReference("event")
                    ref?.id
                }.toSet()

                val eventDocs = db.collection("event").get().await()
                val list = mutableListOf<EventUI>()

                for (doc in eventDocs) {
                    val name = doc.getString("name") ?: "Sans nom"
                    val description = doc.getString("description") ?: ""
                    val place = doc.getString("place") ?: ""
                    val timestamp = doc.getTimestamp("date")?.toDate() ?: Date()
                    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(timestamp)

                    if (timestamp.before(Date())) continue

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

                    val typeRefs = doc.get("types") as? List<*> ?: emptyList<Any>()
                    val typeNames = mutableListOf<String>()
                    for (typeRef in typeRefs) {
                        val ref = typeRef as? com.google.firebase.firestore.DocumentReference
                        ref?.get()?.await()?.getString("name")?.let { typeNames.add(it) }
                    }

                    val photoQuery = db.collection("photo")
                        .whereEqualTo("event", doc.reference)
                        .limit(1)
                        .get()
                        .await()
                    val imageUrl = if (photoQuery.documents.isNotEmpty()) {
                        photoQuery.documents.first().getString("file_url")
                    } else null

                    val isPromoted = doc.id in promotedEventIds

                    list.add(
                        EventUI(
                            id = doc.id,
                            name = name,
                            description = description,
                            place = place,
                            date = dateStr,
                            timestamp = timestamp,
                            cityName = cityName,
                            departmentName = departmentName,
                            types = typeNames,
                            imageUrl = imageUrl,
                            isPromoted = isPromoted
                        )
                    )
                }

                events = list.sortedBy { it.timestamp }
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
            val matchCity = if (selectedCities.isEmpty()) true else event.cityName in selectedCities
            matchType && matchCity
        }.sortedWith(compareByDescending<EventUI> { it.isPromoted }.thenBy { it.timestamp })
    }

    fun setFilter(type: String? = null, cities: List<String> = emptyList()) {
        selectedType = type
        selectedCities = cities
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

    fun refreshPromotedStatus(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val payments = db.collection("payment").get().await()
                val promotedEventIds = payments.mapNotNull { it.getDocumentReference("event")?.id }.toSet()
                events = events.map { it.copy(isPromoted = it.id in promotedEventIds) }
                applyFilters()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onComplete()
            }
        }
    }

    fun refreshUserCities(auth: FirebaseAuth, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val userDoc = db.collection("user").document(uid).get().await()
                val cityIds = userDoc.get("cities") as? List<String> ?: emptyList()

                val citiesList = mutableListOf<String>()
                for (cityId in cityIds) {
                    val cityDoc = db.collection("city").document(cityId).get().await()
                    cityDoc.getString("name")?.let { citiesList.add(it) }
                }

                if (citiesList != selectedCities) {
                    selectedCities = citiesList
                    applyFilters()
                }

                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete()
            }
        }
    }
}
