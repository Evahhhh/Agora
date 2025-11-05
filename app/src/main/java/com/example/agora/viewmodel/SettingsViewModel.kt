package com.example.agora.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.Date
import com.google.firebase.Timestamp

data class UserEvent(
    val id: String,
    val name: String,
    val place: String,
    val date: Date?,
    val isHighlighted: Boolean = false
)

class SettingsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var userName by mutableStateOf("Utilisateur")
        private set

    var userCities by mutableStateOf<List<String>>(emptyList())
        private set

    var allCities by mutableStateOf<List<Pair<String,String>>>(emptyList())
        private set

    var userEvents by mutableStateOf<List<UserEvent>>(emptyList())
        private set

    var loading by mutableStateOf(true)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    fun showMessage(newMessage: String?) {
        message = newMessage
    }

    fun loadUserData(auth: FirebaseAuth) {
        viewModelScope.launch {
            loading = true
            try {
                val uid = auth.currentUser?.uid ?: return@launch

                val userDoc = db.collection("user").document(uid).get().await()
                userName = userDoc.getString("firstname") ?: "Utilisateur"
                userCities = (userDoc.get("cities") as? List<String>) ?: emptyList()

                val cityDocs = db.collection("city").get().await()
                allCities = cityDocs.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    name to doc.id
                }

                val eventsQuery = db.collection("event")
                    .whereEqualTo("creator", db.collection("user").document(uid))
                    .get()
                    .await()

                val paymentsQuery = db.collection("payment")
                    .whereEqualTo("user", db.collection("user").document(uid))
                    .get()
                    .await()

                val highlightedEventIds = paymentsQuery.mapNotNull { it.getDocumentReference("event")?.id }

                userEvents = eventsQuery.map { doc ->
                    val name = doc.getString("name") ?: "Événement inconnu"
                    val place = doc.getString("place") ?: "Inconnu"
                    val timestamp = doc.get("date") as? Timestamp
                    val date = timestamp?.toDate()
                    val isHighlighted = highlightedEventIds.contains(doc.id)
                    UserEvent(doc.id, name, place, date, isHighlighted)
                }.sortedByDescending { it.date ?: Date(0) }

            } catch (e: Exception) {
                message = e.message ?: "Erreur inconnue"
            } finally {
                loading = false
            }
        }
    }

    fun toggleCitySelection(cityId: String) {
        userCities = if (userCities.contains(cityId)) userCities - cityId else userCities + cityId
    }

    fun updateCities(auth: FirebaseAuth) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("user").document(uid).update("cities", userCities)
            .addOnSuccessListener { message = "Villes mises à jour ✅" }
            .addOnFailureListener { message = "Erreur : ${it.message}" }
    }

    fun highlightEvent(auth: FirebaseAuth, eventId: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            try {
                val paymentRef = db.collection("payment").document()
                val userRef = db.collection("user").document(uid)
                val eventRef = db.collection("event").document(eventId)

                val paymentData = mapOf(
                    "amount" to 5.0,
                    "user" to userRef,
                    "event" to eventRef
                )

                paymentRef.set(paymentData).await()
                userEvents = userEvents.map { if (it.id == eventId) it.copy(isHighlighted = true) else it }
                message = "Événement mis en avant ⭐"
            } catch (e: Exception) {
                message = e.message ?: "Erreur inconnue"
            }
        }
    }
}
