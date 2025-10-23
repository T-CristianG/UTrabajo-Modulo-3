package com.tab.utrabajo.presentation.screens

import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tab.utrabajo.presentation.navigation.Screen
import com.tab.utrabajo.R
import kotlin.random.Random

@Composable
fun VerifyCodeScreen(navController: NavHostController) {
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf(generateRandomCode()) }
    var email by remember { mutableStateOf("usuario@ejemplo.com") } // reemplaza por el email real si lo tienes

    // Strings
    val instruction = stringResource(R.string.verifycode_instruction)
    val labelCode = stringResource(R.string.verifycode_label)
    val nextButton = stringResource(R.string.verifycode_button_next)
    val resendButton = stringResource(R.string.verifycode_button_resend)
    val sentFmt = stringResource(R.string.verifycode_sent_fmt) // formato: "Código enviado a %1$s: %2$s"
    val incorrectCode = stringResource(R.string.verifycode_incorrect)
    val codeLengthError = stringResource(R.string.verifycode_length_error)
    val expireNote = stringResource(R.string.verifycode_expire_note)

    // Función para "enviar" el código (simulado)
    fun sendVerificationCode() {
        // En app real aquí se llama al backend / servicio de email/SMS
        val msg = String.format(sentFmt, email, generatedCode)
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    // Enviar código automáticamente al cargar la pantalla
    LaunchedEffect(Unit) {
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
            instruction,
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
            label = { Text(labelCode) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                if (code.length == 5) {
                    if (code == generatedCode) {
                        navController.navigate(Screen.ResetPassword.route)
                    } else {
                        Toast.makeText(context, incorrectCode, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, codeLengthError, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(nextButton)
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                generatedCode = generateRandomCode()
                sendVerificationCode()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(resendButton)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            expireNote,
            color = Color(0xFF2F90D9),
            fontSize = 14.sp
        )
    }
}

// Genera código aleatorio de 5 dígitos
private fun generateRandomCode(): String {
    return Random.nextInt(10000, 99999).toString()
}
