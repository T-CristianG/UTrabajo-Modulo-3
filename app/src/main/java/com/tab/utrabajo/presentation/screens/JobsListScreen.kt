package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tab.utrabajo.presentation.navigation.Screen
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border

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
            CustomBottomNavigationBar(
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onJobsClick = { /* stay */ },
                onChatClick = { /* chat */ },
                onHomeClick = { /* home */ },
                onNotificationsClick = { /* notifications */ }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
        ) {
            // Título Buscador
            Text(
                text = "Buscador",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            // buscador
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar trabajos...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Encuentra el trabajo de tus sueños",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items.size) { idx ->
                    JobItem(
                        company = items[idx].company,
                        position = items[idx].position,
                        isChecked = items[idx].isChecked,
                        onClick = { /* open detail */ }
                    )
                }
            }
        }
    }
}

data class JobItemData(
    val company: String,
    val position: String,
    val isChecked: Boolean
)

@Composable
fun JobItem(company: String, position: String, isChecked: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isChecked,
                onCheckedChange = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Logo/icono de la empresa
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFEAF4FB), shape = CircleShape)
                    .border(1.dp, Color(0xFFBBDEFB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = company.first().toString(),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del trabajo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    company,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    position,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun CustomBottomNavigationBar(
    onProfileClick: () -> Unit,
    onJobsClick: () -> Unit,
    onChatClick: () -> Unit,
    onHomeClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(Color.White)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Perfil
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                contentDescription = "Perfil",
                modifier = Modifier.size(24.dp),
                tint = Color.Gray
            )
            Text(
                "Perfil",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        // Hogar - Centrado
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onHomeClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_view),
                contentDescription = "Hogar",
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF1976D2) // Azul para indicar selección
            )
            Text(
                "Hogar",
                fontSize = 12.sp,
                color = Color(0xFF1976D2), // Azul para indicar selección
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }

        // Chat
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onChatClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_dialog_email),
                contentDescription = "Chat",
                modifier = Modifier.size(24.dp),
                tint = Color.Gray
            )
            Text(
                "Chat",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        // Notificaciones
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onNotificationsClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_popup_reminder),
                contentDescription = "Notificaciones",
                modifier = Modifier.size(24.dp),
                tint = Color.Gray
            )
            Text(
                "Notificaciones",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        // Empleo
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onJobsClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_edit),
                contentDescription = "Empleo",
                modifier = Modifier.size(24.dp),
                tint = Color.Gray
            )
            Text(
                "Empleo",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}