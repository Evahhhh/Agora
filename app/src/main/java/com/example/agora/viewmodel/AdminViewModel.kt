package com.example.agora.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserStats(
    val id: String,
    val name: String,
    val email: String,
    val city: String?,
    val department: String?,
    val eventCount: Int,
    val moneySpent: Double
)

class AdminViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _isAdmin = mutableStateOf(false)
    val isAdmin: State<Boolean> = _isAdmin

    private val _loading = mutableStateOf(true)
    val loading: State<Boolean> = _loading

    private val _totalUsers = mutableStateOf(0)
    val totalUsers: State<Int> = _totalUsers

    private val _totalEvents = mutableStateOf(0)
    val totalEvents: State<Int> = _totalEvents

    private val _upcomingEventsPercentage = mutableStateOf(0.0)
    val upcomingEventsPercentage: State<Double> = _upcomingEventsPercentage

    private val _usersByDepartment = mutableStateOf<Map<String, Int>>(emptyMap())
    val usersByDepartment: State<Map<String, Int>> = _usersByDepartment

    private val _eventsByDepartment = mutableStateOf<Map<String, Int>>(emptyMap())
    val eventsByDepartment: State<Map<String, Int>> = _eventsByDepartment

    private val _usersByCity = mutableStateOf<Map<String, Int>>(emptyMap())
    val usersByCity: State<Map<String, Int>> = _usersByCity

    private val _eventsByCity = mutableStateOf<Map<String, Int>>(emptyMap())
    val eventsByCity: State<Map<String, Int>> = _eventsByCity

    private val _userDetails = mutableStateOf<List<UserStats>>(emptyList())
    val userDetails: State<List<UserStats>> = _userDetails

    init {
        checkIfAdmin()
        loadAdminData()
    }

    fun checkIfAdmin() {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val userDoc = db.collection("user").document(uid).get().await()
                _isAdmin.value = userDoc.getBoolean("isAdmin") ?: false
            } catch (e: Exception) {
                _isAdmin.value = false
            }
        }
    }

    fun loadAdminData() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val usersSnapshot = db.collection("user").get().await()
                val eventsSnapshot = db.collection("event").get().await()
                val paymentsSnapshot = db.collection("payment").get().await()

                _totalUsers.value = usersSnapshot.documents.size

                val totalEventsCount = eventsSnapshot.documents.size
                _totalEvents.value = totalEventsCount

                val now = System.currentTimeMillis()
                val upcomingCount = eventsSnapshot.documents.count { doc ->
                    val timestamp = doc.getTimestamp("date")?.toDate()?.time ?: 0L
                    timestamp >= now
                }
                _upcomingEventsPercentage.value =
                    if (totalEventsCount > 0) (upcomingCount * 100.0 / totalEventsCount) else 0.0

                val usersByDept = mutableMapOf<String, Int>()
                val eventsByDept = mutableMapOf<String, Int>()

                for (userDoc in usersSnapshot.documents) {
                    val deptName = getDepartmentNameFromUser(userDoc)
                    usersByDept[deptName] = usersByDept.getOrDefault(deptName, 0) + 1
                }

                for (eventDoc in eventsSnapshot.documents) {
                    val deptName = getDepartmentNameFromEvent(eventDoc)
                    eventsByDept[deptName] = eventsByDept.getOrDefault(deptName, 0) + 1
                }

                _usersByDepartment.value = usersByDept
                _eventsByDepartment.value = eventsByDept

                val usersByCityMap = mutableMapOf<String, Int>()
                val eventsByCityMap = mutableMapOf<String, Int>()

                for (userDoc in usersSnapshot.documents) {
                    val cityName = getCityNameFromUser(userDoc)
                    usersByCityMap[cityName] = usersByCityMap.getOrDefault(cityName, 0) + 1
                }

                for (eventDoc in eventsSnapshot.documents) {
                    val cityName = getCityNameFromEvent(eventDoc)
                    eventsByCityMap[cityName] = eventsByCityMap.getOrDefault(cityName, 0) + 1
                }

                _usersByCity.value = usersByCityMap
                _eventsByCity.value = eventsByCityMap

                val eventsByUser = eventsSnapshot.documents.groupBy { it.getDocumentReference("creator")?.id ?: "unknown" }

                val userList = usersSnapshot.documents.map { userDoc ->
                    val uid = userDoc.id
                    val name = (userDoc.getString("firstname") ?: "") + " " + (userDoc.getString("lastname") ?: "")
                    val email = userDoc.getString("email") ?: ""
                    val cityName = formatCitiesByDepartment(userDoc)
                    val deptName = getDepartmentNameFromUser(userDoc)

                    val userEvents = eventsByUser[uid] ?: emptyList()
                    val eventCount = userEvents.size

                    val userPayments = paymentsSnapshot.documents.filter {
                        it.getDocumentReference("user")?.id == uid
                    }
                    val moneySpent = userPayments.sumOf { it.getDouble("amount") ?: 0.0 }

                    UserStats(uid, name.trim(), email, cityName, deptName, eventCount, moneySpent)
                }

                _userDetails.value = userList.sortedByDescending { it.moneySpent }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun getDepartmentNameFromEvent(eventDoc: DocumentSnapshot): String {
        val cityRef = eventDoc.getDocumentReference("city") ?: return "Inconnu"
        val cityDoc = cityRef.get().await()
        val deptRef = cityDoc.getDocumentReference("department") ?: return "Inconnu"
        return deptRef.get().await().getString("name") ?: "Inconnu"
    }

    private suspend fun getCityNameFromUser(userDoc: DocumentSnapshot): String {
        val citiesList = userDoc.get("cities") as? List<DocumentReference> ?: return "Inconnue"
        val firstCityRef = citiesList.firstOrNull() ?: return "Inconnue"

        return try {
            val cityDoc = firstCityRef.get().await()
            cityDoc.getString("name") ?: "Inconnue"
        } catch (e: Exception) {
            "Inconnue"
        }
    }

    private suspend fun getDepartmentNameFromUser(userDoc: DocumentSnapshot): String {
        val citiesList = userDoc.get("cities") as? List<DocumentReference> ?: return "Inconnu"
        val firstCityRef = citiesList.firstOrNull() ?: return "Inconnu"

        return try {
            val cityDoc = firstCityRef.get().await()
            val deptRef = cityDoc.getDocumentReference("department") ?: return "Inconnu"
            val deptDoc = deptRef.get().await()
            deptDoc.getString("name") ?: "Inconnu"
        } catch (e: Exception) {
            "Inconnu"
        }
    }

    private suspend fun formatCitiesByDepartment(userDoc: DocumentSnapshot): String {
        val citiesList = userDoc.get("cities") as? List<DocumentReference> ?: return "Inconnue"
        if (citiesList.isEmpty()) return "Inconnue"

        val deptToCities = mutableMapOf<String, MutableList<String>>()
        for (cityRef in citiesList) {
            try {
                val cityDoc = cityRef.get().await()
                val cityName = cityDoc.getString("name") ?: "Inconnue"
                val deptName = cityDoc.getDocumentReference("department")?.get()?.await()?.getString("name") ?: "Inconnu"
                deptToCities.getOrPut(deptName) { mutableListOf() }.add(cityName)
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Impossible de récupérer la ville ou le département", e)
            }
        }

        val result = deptToCities.entries.joinToString(", ") { (dept, cities) ->
            cities.joinToString(", ") + if (dept != "Inconnu") " ($dept)" else ""
        }

        return result
    }

    private suspend fun getCityNameFromEvent(eventDoc: DocumentSnapshot): String {
        val cityRef = eventDoc.getDocumentReference("city") ?: return "Inconnue"
        val cityDoc = cityRef.get().await()
        return cityDoc.getString("name") ?: "Inconnue"
    }
}
