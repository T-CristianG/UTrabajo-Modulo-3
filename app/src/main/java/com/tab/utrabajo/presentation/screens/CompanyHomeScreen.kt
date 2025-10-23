package com.tab.utrabajo.ui.company

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun CompanyHomeScreen(navController: NavHostController) {
    // Datos de ejemplo para candidatos (iguales a la imagen)
    val candidates = List(5) {
        Candidate("Carlos Lopez", "Full Stack Developer")
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                modifier = Modifier.height(70.dp)
            ) {
                // Perfil
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Perfil",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Perfil",
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { navController.navigate("profile") }
                )

                // Chat
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "Chat",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Chat",
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { /* TODO: Navegar a Chat */ }
                )

                // Hoger (Home)
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Hoger",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Hoger",
                            fontSize = 12.sp
                        )
                    },
                    selected = true,
                    onClick = { }
                )

                // Notificaciones
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Notificaciones",
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { /* TODO: Navegar a Notificaciones */ }
                )

                // Empieo (Empleo) -- CAMBIO: ahora navega a la ruta "empleo"
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Work,
                            contentDescription = "Empieo",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Empieo",
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { navController.navigate("empleo") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Buscador",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Descubre candidatos interesados en crecer contigo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Lista de candidatos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(candidates) { candidate ->
                    CandidateListItem(candidate = candidate)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun CandidateListItem(candidate: Candidate) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar o icono (puedes reemplazar con imagen real)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.LightGray, shape = androidx.compose.foundation.shape.CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Información del candidato
            Column {
                Text(
                    text = candidate.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = candidate.position,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

data class Candidate(
    val name: String,
    val position: String
)
