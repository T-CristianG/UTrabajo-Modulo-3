package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.presentation.components.CompanyAvatarField
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun RegisterCompanyScreen(navController: NavHostController) {
    var nit by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var workers by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") } // Nuevo campo
    var confirmPassword by remember { mutableStateOf("") } // Nuevo campo
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

        CompanyAvatarField(enabled = !isLoading)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = nit,
            onValueChange = { nit = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("NIT y Nombre de la empresa *") },
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Número Telefónico de la empresa *") },
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo de la empresa *") },
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = workers,
            onValueChange = { workers = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Número de trabajadores *") },
            enabled = !isLoading
        )

        // Campos de contraseña agregados
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña *") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirmar Contraseña *") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                // Validaciones actualizadas
                if (nit.isBlank() || phone.isBlank() || email.isBlank() || workers.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    errorMessage = "Por favor complete todos los campos"
                    return@Button
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = "Por favor ingrese un correo electrónico válido"
                    return@Button
                }

                if (password != confirmPassword) {
                    errorMessage = "Las contraseñas no coinciden"
                    return@Button
                }

                if (password.length < 6) {
                    errorMessage = "La contraseña debe tener al menos 6 caracteres"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                FirebaseRepository.getInstance().registerCompany(
                    nit = nit.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    workers = workers.trim(),
                    password = password, // Nuevo parámetro
                    onSuccess = {
                        isLoading = false
                        navController.navigate(Screen.CompanyRepInfo.route)
                    },
                    onError = { error ->
                        isLoading = false
                        errorMessage = error
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) Text("Registrando...") else Text("Siguiente")
        }
    }
}