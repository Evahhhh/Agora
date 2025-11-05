package com.example.agora.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    var cities = listOf<Pair<String,String>>()

    fun loadCities(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val cityDocs = db.collection("city").get().await()

                val citiesList = mutableListOf<Pair<String, String>>()

                for (cityDoc in cityDocs) {
                    val cityName = cityDoc.getString("name") ?: continue
                    val departmentRef = cityDoc.getDocumentReference("department") ?: continue

                    val departmentDoc = departmentRef.get().await()
                    val departmentName = departmentDoc.getString("name") ?: "Inconnu"

                    citiesList.add(Pair("$cityName ($departmentName)", cityDoc.id))
                }

                cities = citiesList
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Erreur de chargement des villes")
            }
        }
    }

    fun login(email: String, password: String, onSuccess: ()->Unit, onError: (String)->Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Erreur inconnue") }
    }

    fun register(
        firstname: String,
        lastname: String,
        email: String,
        password: String,
        selectedCities: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                val cityRefs = selectedCities.map { cityId ->
                    db.collection("city").document(cityId)
                }

                val userMap = hashMapOf(
                    "id" to userId,
                    "firstname" to firstname,
                    "lastname" to lastname,
                    "email" to email,
                    "cities" to cityRefs,
                    "isAdmin" to false
                )
                db.collection("user").document(userId).set(userMap)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Erreur Firestore") }
            }
            .addOnFailureListener { onError(it.message ?: "Erreur Auth") }
    }
}
