package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun JobsListScreen(navController: NavHostController) {
    var query by remember { mutableStateOf("") }
    val items = remember {
        listOf(
            JobItemData("Microsoft", "Full Stack Developer", false),
            JobItemData("Google", "Full Stack Developer", false),
            JobItemData("Amazon", "Full Stack Developer", false),
            JobItemData("Apple", "Full Stack Developer", false),
            JobItemData("Netflix", "Full Stack Developer", false)
        )
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
                    onClick = { navController.navigate(Screen.Profile.route) }
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
                    selected = false,
                    onClick = { navController.popBackStack() }
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

                // Empieo (Empleo)
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
                    selected = true,
                    onClick = { }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = Color(0xFFE9F3F8)) // fondo celeste suave igual al anterior
        ) {
            // Header - Manteniendo el diseño original pero con el color de fondo
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
                    text = "Encuentra el trabajo de tus sueños",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Barra de búsqueda
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar trabajos...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de trabajos - CON EL NUEVO DISEÑO
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(items = items, key = { it.company }) { job ->
                    JobCardForList(
                        company = job.company,
                        position = job.position,
                        onClick = { /* open detail */ }
                    )
                }
            }
        }
    }
}

/* --- Nuevo JobCard con el diseño de la primera pantalla --- */
@Composable
private fun JobCardForList(company: String, position: String, onClick: () -> Unit) {
    // se fija una altura uniforme para todos los cards
    val cardHeight = 95.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight) // <-- altura fija para uniformidad
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo circular - IDÉNTICO al de la primera pantalla
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFFF2F8FB), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0xFF2B7BBF), shape = RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Texto empresa + puesto (centrado verticalmente) - Mismo estilo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = company,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E88E5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = position,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Estado a la derecha - Similar al diseño original pero simplificado
            // Como no tenemos estado de proceso, mostramos un icono genérico
            Column(
                modifier = Modifier
                    .width(80.dp)
                    .padding(start = 6.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFEEEEEE), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ver detalles",
                        tint = Color(0xFF6D6D6D),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ver detalles",
                    fontSize = 11.sp,
                    color = Color(0xFF6D6D6D),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

data class JobItemData(
    val company: String,
    val position: String,
    val isChecked: Boolean
)