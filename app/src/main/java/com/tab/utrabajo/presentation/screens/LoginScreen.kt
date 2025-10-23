package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.R
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 📖 Strings internacionalizados
    val emailField = stringResource(R.string.email_field)
    val passwordField = stringResource(R.string.password_field)
    val loginButton = stringResource(R.string.login_button)
    val companyLoginButton = stringResource(R.string.company_login_button)
    val recoverPassword = stringResource(R.string.recover_password)
    val fillAllFieldsError = stringResource(R.string.fill_all_fields_error)
    val loadingText = stringResource(R.string.loading_text)
    val uidError = stringResource(R.string.uid_error)
    val userCheckError = stringResource(R.string.user_check_error)
    val companyCheckError = stringResource(R.string.company_check_error)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo UT - Mismo estilo que SplashScreen
        Text(
            text = "UT",
            fontSize = 72.sp,
            color = Color(0xFF2F90D9),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Tu futuro profesional comienza aquí.",
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 60.dp)
        )

        // Mensaje de error (si existe)
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        // Campo de nombre de usuario o correo
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre de usuario o correo") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !isLoading,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Campo de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Botón de Iniciar Sesión - Mismo estilo que SplashScreen
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = fillAllFieldsError
                    return@Button
                }

                isLoading = true
                errorMessage = null

                FirebaseRepository.getInstance().loginUser(
                    email = email.trim(),
                    password = password,
                    onSuccess = {
                        isLoading = false
                        val uid = FirebaseRepository.getInstance().getCurrentUser()?.uid

                        if (uid == null) {
                            errorMessage = uidError
                        } else {
                            val db = FirebaseFirestore.getInstance()
                            val startId = navController.graph.startDestinationId

                            db.collection("usuarios").document(uid).get()
                                .addOnSuccessListener { doc ->
                                    if (doc.exists()) {
                                        navController.navigate(Screen.JobsList.route) {
                                            popUpTo(startId) { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate(Screen.RoleSelection.route) {
                                            popUpTo(startId) { inclusive = true }
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    errorMessage = "$userCheckError ${e.message}"
                                }
                        }
                    },
                    onError = { error ->
                        isLoading = false
                        errorMessage = error
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2F90D9)
            )
        ) {
            if (isLoading) Text(loadingText) else Text("Iniciar Sesión", color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        // Botón para login de empresa - Mismo estilo que SplashScreen
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = fillAllFieldsError
                    return@Button
                }

                isLoading = true
                errorMessage = null

                FirebaseRepository.getInstance().loginUser(
                    email = email.trim(),
                    password = password,
                    onSuccess = {
                        isLoading = false
                        val uid = FirebaseRepository.getInstance().getCurrentUser()?.uid

                        if (uid == null) {
                            errorMessage = uidError
                        } else {
                            val db = FirebaseFirestore.getInstance()
                            val startId = navController.graph.startDestinationId

                            db.collection("empresas").document(uid).get()
                                .addOnSuccessListener { doc ->
                                    if (doc.exists()) {
                                        navController.navigate(Screen.CompanyHome.route) {
                                            popUpTo(startId) { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = companyCheckError
                                    }
                                }
                                .addOnFailureListener { e ->
                                    errorMessage = "$companyCheckError ${e.message}"
                                }
                        }
                    },
                    onError = { error ->
                        isLoading = false
                        errorMessage = error
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2F90D9)
            )
        ) {
            if (isLoading) Text(loadingText) else Text(companyLoginButton, color = Color.White)
        }

        Spacer(Modifier.height(24.dp))

        // Enlace de recuperar contraseña
        TextButton(
            onClick = { navController.navigate(Screen.RecoverStart.route) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Recuperar contraseña", color = Color(0xFF2F90D9))
        }
    }
}