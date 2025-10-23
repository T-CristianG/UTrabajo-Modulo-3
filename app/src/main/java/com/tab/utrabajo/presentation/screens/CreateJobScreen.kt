package com.tab.utrabajo.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tab.utrabajo.FirebaseRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = FirebaseRepository.getInstance()
    val currentUser = repository.getCurrentUser()

    // Verificar usuario autenticado
    if (currentUser == null) {
        Toast.makeText(context, "Error: No hay usuario autenticado", Toast.LENGTH_LONG).show()
        navController.navigate("company_home")
        return
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Crear Nueva Oferta") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título del puesto*") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4
            )

            OutlinedTextField(
                value = requirements,
                onValueChange = { requirements = it },
                label = { Text("Requisitos (separados por coma)*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            OutlinedTextField(
                value = salary,
                onValueChange = { salary = it },
                label = { Text("Salario*") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación*") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "* Campos obligatorios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || salary.isBlank() || location.isBlank() || requirements.isBlank()) {
                        Toast.makeText(context, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true

                    // Convertir requisitos de string a lista
                    val requirementsList = requirements.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    repository.createJobOffer(
                        companyId = currentUser.uid,
                        title = title,
                        description = description,
                        requirements = requirementsList,
                        salary = salary,
                        location = location,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, "Empleo creado exitosamente", Toast.LENGTH_SHORT).show()
                            navController.navigate("job_created")
                        },
                        onError = { error ->
                            isLoading = false
                            Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Publicar Empleo")
                }
            }
        }
    }
}