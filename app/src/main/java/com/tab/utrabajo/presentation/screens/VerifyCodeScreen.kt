package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tab.utrabajo.presentation.navigation.Screen
import android.widget.Toast
import kotlin.random.Random

@Composable
fun VerifyCodeScreen(navController: NavHostController) {
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf(generateRandomCode()) }
    var email by remember { mutableStateOf("usuario@ejemplo.com") } // Cambia por el email real

    // Función para enviar el código (simulación)
    fun sendVerificationCode() {
        // En una app real, aquí iría la lógica para enviar el código al email
        Toast.makeText(
            context,
            "Código enviado a $email: $generatedCode",
            Toast.LENGTH_LONG
        ).show()
    }

    // Enviar código automáticamente al cargar la pantalla
    androidx.compose.runtime.LaunchedEffect(Unit) {
        sendVerificationCode()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            "Por favor, ingrese el código de 5 dígitos enviado al correo o teléfono de la cuenta asociada",
            color = Color(0xFF2F90D9),
            fontSize = 16.sp
        )

        Spacer(Modifier.height(18.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { new ->
                code = new.filter { it.isDigit() }.take(5)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Código (5 dígitos)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                if (code.length == 5) {
                    if (code == generatedCode) {
                        navController.navigate(Screen.ResetPassword.route)
                    } else {
                        Toast.makeText(context, "Código incorrecto", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "El código debe tener 5 dígitos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Siguiente")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                generatedCode = generateRandomCode()
                sendVerificationCode()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reenviar código")
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Tenga en cuenta que el código tiene una duración de 2 minutos. En caso de necesitar otro código, haga clic en Reenviar código.",
            color = Color(0xFF2F90D9),
            fontSize = 14.sp
        )
    }
}

// Función para generar código aleatorio de 5 dígitos
private fun generateRandomCode(): String {
    return Random.nextInt(10000, 99999).toString()
}