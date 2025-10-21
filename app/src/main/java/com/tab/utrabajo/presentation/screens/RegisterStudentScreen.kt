package com.tab.utrabajo.presentation.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.presentation.navigation.Screen


private fun testFirebaseConnectionDetailed(context: Context) {
    println("🔥 INICIANDO TEST DETALLADO DE FIREBASE")

    try {

        val apps = FirebaseApp.getApps(context)
        println("🔥 Número de apps Firebase: ${apps.size}")

        apps.forEachIndexed { index, app ->
            println("🔥 App $index: ${app.name}")
        }

        // 2. Test FirebaseAuth
        val auth = FirebaseAuth.getInstance()
        println("🔥 FirebaseAuth instance: $auth")
        println("🔥 Current user: ${auth.currentUser}")

        // 3. Test Firestore
        val db = FirebaseFirestore.getInstance()
        println("🔥 Firestore instance: $db")

        // 4. Test simple de escritura
        val testData = hashMapOf(
            "test" to "conexion",
            "timestamp" to Timestamp.now()
        )

        db.collection("debug_test").document("connection")
            .set(testData)
            .addOnSuccessListener {
                println("🔥 ✅ ESCRITURA EXITOSA en Firestore")
                Log.d("FIREBASE_TEST", "✅ Firestore funciona correctamente")
            }
            .addOnFailureListener { e ->
                println("🔥 ❌ ERROR escribiendo en Firestore: ${e.message}")
                Log.e("FIREBASE_TEST", "❌ Firestore error: ${e.message}")
            }

    } catch (e: Exception) {
        println("🔥 💥 EXCEPCIÓN en test: ${e.message}")
        Log.e("FIREBASE_TEST", "💥 Exception: ${e.message}")
        e.printStackTrace()
    }
}

@Composable
fun RegisterStudentScreen(navController: NavHostController) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(8.dp))

        // MOSTRAR ERROR DETALLADO
        errorMessage?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = " ",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Campo Nombre Completo
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre Completo *") },
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email *") },
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña *") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))

        // Campo Confirmar Contraseña
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirmar Contraseña *") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading
        )
        Spacer(Modifier.height(12.dp))

        // Requisitos de contraseña
        Text("Requisitos de contraseña", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text("• Mínimo 8 caracteres")
        Text("• Al menos una letra mayúscula")
        Text("• Al menos un número")
        Text("• Al menos un símbolo (ej: !, @, #, $)")
        Spacer(Modifier.height(18.dp))

        // BOTÓN PRINCIPAL - CONTINUAR
        Button(
            onClick = {
                // Resetear mensajes
                errorMessage = null

                // Validaciones básicas
                if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
                    errorMessage = "Por favor complete todos los campos"
                    return@Button
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = "Por favor ingrese un email válido"
                    return@Button
                }

                if (password != confirm) {
                    errorMessage = "Las contraseñas no coinciden"
                    return@Button
                }

                if (password.length < 8) {
                    errorMessage = "La contraseña debe tener al menos 8 caracteres"
                    return@Button
                }

                // Validar requisitos de contraseña
                val hasUpperCase = password.any { it.isUpperCase() }
                val hasDigit = password.any { it.isDigit() }
                val hasSpecialChar = password.any { !it.isLetterOrDigit() }

                if (!hasUpperCase || !hasDigit || !hasSpecialChar) {
                    errorMessage = "La contraseña debe tener:\n• Una letra mayúscula\n• Un número\n• Un símbolo especial"
                    return@Button
                }

                isLoading = true

                // Mostrar que estamos intentando registra

                FirebaseRepository.getInstance().registerStudent(
                    email = email.trim(),
                    password = password,
                    fullName = fullName.trim(),
                    onSuccess = {
                        isLoading = false
                        errorMessage = "✅ Registro exitoso! Redirigiendo..."
                        navController.navigate(Screen.StudentWorkInfo.route)
                    },
                    onError = { error ->
                        isLoading = false
                        errorMessage = " "
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) Text("🔄 Registrando...") else Text("Continuar")
        }
        }
    }