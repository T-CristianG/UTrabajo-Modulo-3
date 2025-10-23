package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.R
import com.tab.utrabajo.presentation.components.CompanyAvatarField
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun RegisterCompanyScreen(navController: NavHostController) {
    var nit by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var workers by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Recursos de texto
    val labelNit = stringResource(R.string.registercompany_label_nit)
    val labelPhone = stringResource(R.string.registercompany_label_phone)
    val labelEmail = stringResource(R.string.registercompany_label_email)
    val labelWorkers = stringResource(R.string.registercompany_label_workers)
    val labelPassword = stringResource(R.string.registercompany_label_password)
    val labelConfirmPassword = stringResource(R.string.registercompany_label_confirm_password)

    val errorFillAll = stringResource(R.string.registercompany_error_fill_all)
    val errorInvalidEmail = stringResource(R.string.registercompany_error_invalid_email)
    val errorPasswordsMismatch = stringResource(R.string.registercompany_error_passwords_mismatch)
    val errorPasswordShort = stringResource(R.string.registercompany_error_password_short)
    val registeringText = stringResource(R.string.registercompany_registering)
    val nextText = stringResource(R.string.registercompany_next)

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
            label = { Text(labelNit) },
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(labelPhone) },
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(labelEmail) },
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = workers,
            onValueChange = { workers = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(labelWorkers) },
            enabled = !isLoading
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(labelPassword) },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(labelConfirmPassword) },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                // Validaciones actualizadas
                if (nit.isBlank() || phone.isBlank() || email.isBlank() || workers.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    errorMessage = errorFillAll
                    return@Button
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = errorInvalidEmail
                    return@Button
                }

                if (password != confirmPassword) {
                    errorMessage = errorPasswordsMismatch
                    return@Button
                }

                if (password.length < 6) {
                    errorMessage = errorPasswordShort
                    return@Button
                }

                isLoading = true
                errorMessage = null

                FirebaseRepository.getInstance().registerCompany(
                    nit = nit.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    workers = workers.trim(),
                    password = password,
                    onSuccess = {
                        isLoading = false
                        navController.navigate(Screen.CompanyRepInfo.route)
                    },
                    onError = { error ->
                        isLoading = false
                        // Mantener el mensaje de error tal cual lo devuelve el repositorio
                        errorMessage = error
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) Text(registeringText) else Text(nextText)
        }
    }
}
