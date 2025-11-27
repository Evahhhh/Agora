package com.example.agora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.agora.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdowns(eventViewModel: EventViewModel) {
    val types = remember { mutableStateListOf<String>() }
    val cities = remember { mutableStateListOf<String>() }

    var typeExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(eventViewModel.events) {
        types.clear()
        eventViewModel.events.flatMap { it.types }.distinct().let { types.addAll(it) }

        cities.clear()
        eventViewModel.events.map { it.cityName }.distinct().let { cities.addAll(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded }
        ) {
            TextField(
                value = eventViewModel.selectedType ?: "Tous les types",
                onValueChange = {},
                readOnly = true,
                label = { Text("Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Tous les types") },
                    onClick = {
                        eventViewModel.setFilter(null, eventViewModel.selectedCities)
                        typeExpanded = false
                    }
                )
                types.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            eventViewModel.setFilter(type, eventViewModel.selectedCities)
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = cityExpanded,
            onExpandedChange = { cityExpanded = !cityExpanded }
        ) {
            TextField(
                value = if (eventViewModel.selectedCities.isEmpty()) "Toutes les villes"
                else eventViewModel.selectedCities.joinToString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Ville") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = cityExpanded,
                onDismissRequest = { cityExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Toutes les villes") },
                    onClick = {
                        eventViewModel.setFilter(eventViewModel.selectedType, emptyList())
                        cityExpanded = false
                    }
                )
                cities.forEach { city ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = city in eventViewModel.selectedCities,
                                    onCheckedChange = { checked ->
                                        val newCities = if (checked) eventViewModel.selectedCities + city
                                        else eventViewModel.selectedCities - city
                                        eventViewModel.setFilter(eventViewModel.selectedType, newCities)
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(city)
                            }
                        },
                        onClick = {}
                    )
                }
            }
        }
    }
}
