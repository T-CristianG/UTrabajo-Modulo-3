package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun StudentWorkInfoScreen(navController: NavHostController) {
    var worksNowState by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(8.dp))

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        Text(
            "¿Trabajas actualmente?",
            color = Color(0xFF2F90D9),
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = worksNowState,
                onClick = { if (!isLoading) worksNowState = true },
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2F90D9)),
                enabled = !isLoading
            )
            Spacer(Modifier.size(8.dp))
            Text("Si", color = Color(0xFF2F90D9))
            Spacer(Modifier.size(16.dp))
            RadioButton(
                selected = !worksNowState,
                onClick = { if (!isLoading) worksNowState = false },
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2F90D9)),
                enabled = !isLoading
            )
            Spacer(Modifier.size(8.dp))
            Text("No", color = Color(0xFF2F90D9))
        }

        Spacer(Modifier.height(18.dp))

        if (worksNowState) {
            Text("Nombre de la empresa", color = Color(0xFF2F90D9))
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre de la empresa") },
                enabled = !isLoading
            )
            Spacer(Modifier.height(12.dp))
            Text("Rol que desempeñas", color = Color(0xFF2F90D9))
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = role,
                onValueChange = { role = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rol que desempeñas") },
                enabled = !isLoading
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val currentUser = FirebaseRepository.getInstance().getCurrentUser()
                if (currentUser == null) {
                    errorMessage = "Error: Usuario no autenticado"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                FirebaseRepository.getInstance().saveStudentWorkInfo(
                    userId = currentUser.uid,
                    worksNow = worksNowState,
                    companyName = companyName.trim(),
                    role = role.trim(),
                    onSuccess = {
                        isLoading = false
                        navController.navigate(Screen.StudentSkills.route)
                    },
                    onError = { error ->
                        isLoading = false
                        errorMessage = "Error: $error"
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) Text("Guardando...") else Text("Continuar")
        }
    }
}