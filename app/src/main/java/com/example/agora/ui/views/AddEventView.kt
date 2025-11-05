package com.example.agora.ui.views

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agora.viewmodel.AddEventViewModel
import com.example.agora.viewmodel.EventViewModel
import java.util.*

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventView(
    viewModel: AddEventViewModel = viewModel(),
    modifier: Modifier = Modifier,
    eventViewModel: EventViewModel
) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return

    val cities by viewModel.cities.collectAsState()
    val types by viewModel.types.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var photoUrls by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var selectedTypes by remember { mutableStateOf(setOf<String>()) }

    val calendar = Calendar.getInstance()

    val datePickerDialog = remember {
        DatePickerDialog(
            activity,
            { _, year, month, dayOfMonth ->
                date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            activity,
            { _, hourOfDay, minute ->
                time = String.format("%02d:%02d", hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()

            if (it.contains("succès")) {
                name = ""
                description = ""
                place = ""
                date = ""
                time = ""
                photoUrls = ""
                selectedCity = null
                selectedTypes = emptySet()
                eventViewModel.loadEvents(onSuccess = {}, onError = {})
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Text("Ajouter un événement", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                label = { Text("Lieu") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = cities.find { it.id == selectedCity }?.let { "${it.name} (${it.departmentName})" }
                        ?: "Sélectionner une ville",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ville") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    cities.forEach { city ->
                        DropdownMenuItem(
                            text = { Text("${city.name} (${city.departmentName})") },
                            onClick = {
                                selectedCity = city.id
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { datePickerDialog.show() }
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { timePickerDialog.show() }
                ) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Heure") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = photoUrls,
                onValueChange = { photoUrls = it },
                label = { Text("URLs photos (séparés par des virgules)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        item {
            Column {
                Text("Types :", style = MaterialTheme.typography.titleSmall)
                types.forEach { type ->
                    val isSelected = selectedTypes.contains(type.id)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTypes = if (isSelected) {
                                    selectedTypes - type.id
                                } else {
                                    selectedTypes + type.id
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                selectedTypes = if (it) selectedTypes + type.id else selectedTypes - type.id
                            }
                        )
                        Text(type.name)
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.addEvent(
                        name,
                        description,
                        place,
                        date,
                        time,
                        cityId = selectedCity ?: "",
                        selectedTypeIds = selectedTypes.toList(),
                        photoUrls = photoUrls.split(",").map { it.trim() }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                if (loading) CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                else Text("Ajouter")
            }
        }
    }
}
