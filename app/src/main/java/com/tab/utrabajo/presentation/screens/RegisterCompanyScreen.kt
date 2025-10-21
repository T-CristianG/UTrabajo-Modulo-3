package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Top) {
        Spacer(Modifier.height(8.dp))
        errorMessage?.let { Text(text = it, color = Color.Red, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) }
        CompanyAvatarField(enabled = !isLoading)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = nit, onValueChange = { nit = it }, modifier = Modifier.fillMaxWidth(), label = { Text("NIT y Nombre de la empresa *") }, enabled = !isLoading)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Número Telefónico de la empresa *") }, enabled = !isLoading)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Correo de la empresa *") }, enabled = !isLoading)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = workers, onValueChange = { workers = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Número de trabajadores *") }, enabled = !isLoading)
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (nit.isBlank() || phone.isBlank() || email.isBlank() || workers.isBlank()) { /* error */ return@Button }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { /* error */ return@Button }
            isLoading = true
            FirebaseRepository.getInstance().registerCompany(
                nit = nit.trim(),
                phone = phone.trim(),
                email = email.trim(),
                workers = workers.trim(),
                onSuccess = { isLoading = false; navController.navigate(Screen.CompanyRepInfo.route) },
                onError = { _ -> isLoading = false /* error */ }
            )
        }, modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), enabled = !isLoading) {
            if (isLoading) Text("Registrando...") else Text("Siguiente")
        }
    }
}
