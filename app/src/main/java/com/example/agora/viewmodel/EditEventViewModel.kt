package com.example.agora.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class EditEventViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _place = MutableStateFlow("")
    val place = _place.asStateFlow()

    private val _date = MutableStateFlow("")
    val date = _date.asStateFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val doc = db.collection("event").document(eventId).get().await()
                _name.value = doc.getString("name") ?: ""
                _description.value = doc.getString("description") ?: ""
                _place.value = doc.getString("place") ?: ""
                val timestamp = doc.getTimestamp("date")?.toDate()
                _date.value = timestamp?.let {
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                } ?: ""
            } catch (e: Exception) {
                _message.value = "Erreur : ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateEvent(eventId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val parsedDate = format.parse(_date.value) ?: Date()
                val timestamp = Timestamp(parsedDate)

                db.collection("event").document(eventId)
                    .update(
                        mapOf(
                            "name" to _name.value,
                            "description" to _description.value,
                            "place" to _place.value,
                            "date" to timestamp
                        )
                    ).await()

                _message.value = "Événement mis à jour ✅"
            } catch (e: Exception) {
                _message.value = "Erreur lors de la mise à jour : ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun setName(value: String) { _name.value = value }
    fun setDescription(value: String) { _description.value = value }
    fun setPlace(value: String) { _place.value = value }
    fun setDate(value: String) { _date.value = value }
}
