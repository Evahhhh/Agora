package com.example.agora.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class CityUI(
    val id: String,
    val name: String,
    val departmentName: String
)

data class TypeUI(
    val id: String,
    val name: String
)

class AddEventViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // --- UI States ---
    private val _cities = MutableStateFlow<List<CityUI>>(emptyList())
    val cities = _cities.asStateFlow()

    private val _types = MutableStateFlow<List<TypeUI>>(emptyList())
    val types = _types.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    init {
        loadCitiesAndTypes()
    }

    private fun loadCitiesAndTypes() {
        viewModelScope.launch {
            try {
                // --- Charger les villes ---
                val cityDocs = db.collection("city").get().await()
                val cityList = mutableListOf<CityUI>()
                for (cityDoc in cityDocs) {
                    val depRef = cityDoc.getDocumentReference("department")
                    val depName = depRef?.get()?.await()?.getString("name") ?: "Inconnu"
                    cityList.add(
                        CityUI(
                            id = cityDoc.id,
                            name = cityDoc.getString("name") ?: "Ville inconnue",
                            departmentName = depName
                        )
                    )
                }
                _cities.value = cityList.sortedBy { it.name }

                // --- Charger les types ---
                val typeDocs = db.collection("type").get().await()
                val typeList = typeDocs.map {
                    TypeUI(
                        id = it.id,
                        name = it.getString("name") ?: "Type inconnu"
                    )
                }
                _types.value = typeList.sortedBy { it.name }

            } catch (e: Exception) {
                _message.value = "Erreur lors du chargement : ${e.message}"
            }
        }
    }

    fun addEvent(
        name: String,
        description: String,
        place: String,
        date: String,
        time: String,
        cityId: String,
        selectedTypeIds: List<String>,
        photoUrls: List<String>
    ) {
        viewModelScope.launch {
            if (auth.currentUser == null) {
                _message.value = "Utilisateur non connecté"
                return@launch
            }

            if (name.isBlank() || cityId.isBlank() || selectedTypeIds.isEmpty()) {
                _message.value = "Veuillez remplir tous les champs obligatoires"
                return@launch
            }

            try {
                _loading.value = true

                // --- Convertir la date ---
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val parsedDate = format.parse("$date $time") ?: Date()
                val timestamp = Timestamp(parsedDate)

                // --- Préparer les références ---
                val eventRef = db.collection("event").document()
                val cityRef = db.collection("city").document(cityId)
                val typeRefs = selectedTypeIds.map { db.collection("type").document(it) }

                val eventData = mapOf(
                    "name" to name,
                    "description" to description,
                    "place" to place,
                    "date" to timestamp,
                    "creator" to db.collection("user").document(auth.currentUser!!.uid),
                    "city" to cityRef,
                    "types" to typeRefs
                )

                eventRef.set(eventData).await()

                photoUrls.filter { it.isNotBlank() }.forEach { url ->
                    db.collection("photo").add(
                        mapOf(
                            "event" to eventRef,
                            "file_url" to url
                        )
                    ).await()
                }

                _message.value = "Événement ajouté avec succès ✅"
            } catch (e: Exception) {
                _message.value = "Erreur : ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

}
