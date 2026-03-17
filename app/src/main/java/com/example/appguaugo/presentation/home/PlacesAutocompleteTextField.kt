package com.example.appguaugo.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// En un archivo como composables/PlacesAutocompleteTextField.kt
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.PopupProperties
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await

@Composable
fun PlacesAutocompleteTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null // Lo añadimos para que acepte iconos
) {
    val context = LocalContext.current
    val placesClient: PlacesClient = remember { Places.createClient(context) }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    // Usamos `LaunchedEffect(value)` para reaccionar a los cambios de texto.
    LaunchedEffect(value) {
        if (value.length > 2) {
            try {
                val token = AutocompleteSessionToken.newInstance()
                val request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setQuery(value)
                    .setCountries("PE") // O el país que prefieras
                    .build()
                val response = placesClient.findAutocompletePredictions(request).await()
                predictions = response.autocompletePredictions
                expanded = predictions.isNotEmpty()
            } catch (e: ApiException) {
                Log.e("PlacesAutocomplete", "Error al obtener predicciones", e)
                predictions = emptyList()
                expanded = false
            }
        } else {
            predictions = emptyList()
            expanded = false
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = leadingIcon, // Usamos el icono aquí
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(), // Para que ocupe el ancho
            properties = PopupProperties(focusable = false, dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            predictions.forEach { prediction ->
                DropdownMenuItem(
                    text = { Text(prediction.getFullText(null).toString()) },
                    onClick = {
                        val selectedText = prediction.getFullText(null).toString()
                        onValueChange(selectedText) // Notifica el cambio con el texto seleccionado
                        expanded = false
                        predictions = emptyList()
                    }
                )
            }
        }
    }
}
