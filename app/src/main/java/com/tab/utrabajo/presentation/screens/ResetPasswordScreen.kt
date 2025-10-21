package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // ¡AGREGAR ESTA IMPORTACIÓN!
import androidx.navigation.NavHostController
import com.tab.utrabajo.presentation.navigation.Screen
import android.widget.Toast

@Composable
fun ResetPasswordScreen(navController: NavHostController) {
    val context = LocalContext.current
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Función para validar la contraseña
    fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) return false
        if (!password.any { it.isUpperCase() }) return false
        if (!password.any { it.isDigit() }) return false
        if (!password.any { it in "!@#$%^&*()_+-=[]{}|;:',.<>?/" }) return false
        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Ingrese la nueva contraseña:", color = Color(0xFF2F90D9))
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nueva contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(12.dp))
        Text("Confirme la nueva contraseña", color = Color(0xFF2F90D9))
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(12.dp))
        Text("Requisitos de contraseña", color = Color(0xFF2F90D9))
        Spacer(Modifier.height(6.dp))
        Text("• Mínimo 8 caracteres")
        Text("• Al menos una letra mayúscula")
        Text("• Al menos un número")
        Text("• Al menos un símbolo (ej: !, @, #, $)")

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(context, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                } else if (!isPasswordValid(newPassword)) {
                    Toast.makeText(context, "La contraseña no cumple los requisitos", Toast.LENGTH_SHORT).show()
                } else if (newPassword != confirmPassword) {
                    Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                } else {
                    // Aquí iría la lógica para cambiar la contraseña en tu backend
                    Toast.makeText(context, "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.RecoverSuccess.route)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Siguiente")
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Tenga en cuenta que, una vez restablecida la contraseña, deberá esperar un plazo de 1 mes para volver a restablecerla",
            color = Color(0xFF2F90D9),
            fontSize = 14.sp // Esta es la línea que estaba causando el error
        )
    }
}