package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun StudentSkillsScreen(navController: NavHostController) {
    val skills = remember { mutableStateListOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
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
            "Habilidades",
            color = Color(0xFF2F90D9),
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn {
            itemsIndexed(skills) { index, skill ->
                OutlinedTextField(
                    value = skill,
                    onValueChange = { skills[index] = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = { Text("${index + 1}.") },
                    enabled = !isLoading
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (!isLoading) skills.add("")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F90D9)),
            enabled = !isLoading
        ) {
            Text("+ Añadir habilidad", color = Color.White)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val currentUser = FirebaseRepository.getInstance().getCurrentUser()
                if (currentUser == null) {
                    errorMessage = "Error: Usuario no autenticado"
                    return@Button
                }

                val habilidadesValidas = skills.filter { it.isNotBlank() }
                if (habilidadesValidas.isEmpty()) {
                    errorMessage = "Por favor ingrese al menos una habilidad"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                FirebaseRepository.getInstance().saveStudentSkills(
                    userId = currentUser.uid,
                    skills = habilidadesValidas,
                    onSuccess = {
                        isLoading = false
                        navController.navigate(Screen.StudentUploadCV.route)
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