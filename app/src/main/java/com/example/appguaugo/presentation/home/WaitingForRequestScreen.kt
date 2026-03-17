package com.example.appguaugo.presentation.home

import androidx.compose.foundation.background
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appguaugo.R // Asegúrate de importar tu clase R para los recursos
import com.example.appguaugo.ui.theme.GuauYellow
import com.example.appguaugo.ui.theme.GuauYellowDark

// Define los colores de tu app si no lo has hecho
val GuauYellow = Color(0xFFFFD15C)

@OptIn(ExperimentalMaterial3Api::class) // Necesario para TopAppBar
@Composable
fun WaitingForRequestsScreen(
    onNavigateBack: () -> Unit, // <-- CAMBIO 1: Callback para el botón de atrás
    onStopWaiting: () -> Unit   // Callback para la lógica de "desconectarse"
) {
    // --- Lógica de Animación (Sin cambios) ---
    val infiniteTransition = rememberInfiniteTransition(label = "infinite_transition")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 50f, targetValue = 250f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse_radius"
    )
    val pulseColor by infiniteTransition.animateColor(
        initialValue = GuauYellow.copy(alpha = 0.5f), targetValue = GuauYellow.copy(alpha = 0.0f),
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse_color"
    )

    // --- CAMBIO 2: Envolver todo en un Scaffold ---
    Scaffold(
        topBar = {
            // --- CAMBIO 3: Añadir la TopAppBar ---
            TopAppBar(
                title = { Text("Modo Paseador Activo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GuauYellow,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding -> // El padding que nos da el Scaffold para no solapar contenido
        // --- UI de la Pantalla (ahora dentro del Scaffold) ---
        Column(
            // Aplicamos el padding del Scaffold
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp, vertical = 16.dp), // Padding adicional
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // El resto del contenido de la columna es el mismo...

            // --- 1. Contenedor de la Animación ---
            Box(
                modifier = Modifier
                    .size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = pulseColor,
                        radius = pulseRadius * size.minDimension / 250f,
                        style = Stroke(width = 8f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(GuauYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.dog_walking_icon),
                        contentDescription = "Ícono de paseo",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- 2. Textos Informativos ---
            Text(
                text = "Esperando Solicitudes",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Esperando alguna contra oferta. Espere con paciencia por favor.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- 3. Botón de Acción ---
            Button(
                onClick = {
                    // --- CAMBIO 4: Ambas acciones se ejecutan al hacer clic ---
                    onStopWaiting()  // Primero, ejecuta la lógica de desconexión
                    onNavigateBack() // Después, navega hacia atrás
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GuauYellowDark,
                    contentColor = Color.White
                )
            ) {
                Text("Cancelar Solicitud", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


/*
// --- Preview para ver el diseño en Android Studio ---
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun WaitingForRequestsScreenPreview() {
    // Envuelve el preview en tu tema si tienes uno, para ver los colores correctos
    // TuAppTheme {
    WaitingForRequestsScreen(
        onNavigateBack = {} ,
        onPaseoClick = {}//, onStopWaiting = {}
     )
    // }
}
*/
