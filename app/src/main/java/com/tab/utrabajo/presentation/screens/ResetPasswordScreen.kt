package com.tab.utrabajo.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tab.utrabajo.R
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun ResetPasswordScreen(navController: NavHostController) {
    val context = LocalContext.current
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Strings
    val titleText = stringResource(R.string.resetpw_title)
    val newPasswordLabel = stringResource(R.string.resetpw_label_new)
    val confirmPasswordLabel = stringResource(R.string.resetpw_label_confirm)
    val pwRequirementsTitle = stringResource(R.string.resetpw_requirements_title)
    val pwReq1 = stringResource(R.string.resetpw_req_1)
    val pwReq2 = stringResource(R.string.resetpw_req_2)
    val pwReq3 = stringResource(R.string.resetpw_req_3)
    val pwReq4 = stringResource(R.string.resetpw_req_4)

    val errorFill = stringResource(R.string.resetpw_error_fill)
    val errorInvalid = stringResource(R.string.resetpw_error_invalid)
    val errorMismatch = stringResource(R.string.resetpw_error_mismatch)
    val successChanged = stringResource(R.string.resetpw_success_changed)
    val nextButton = stringResource(R.string.resetpw_button_next)
    val noteText = stringResource(R.string.resetpw_note_wait_period)

    // Función para validar la contraseña
    fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) return false
        if (!password.any { it.isUpperCase() }) return false
        if (!password.any { it.isDigit() }) return false
        if (!password.any { "!@#$%^&*()_+-=[]{}|;:',.<>?/".contains(it) }) return false
        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(8.dp))
        Text(titleText, color = Color(0xFF2F90D9))
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(newPasswordLabel) },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(12.dp))
        Text(confirmPasswordLabel, color = Color(0xFF2F90D9))
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(confirmPasswordLabel) },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(12.dp))
        Text(pwRequirementsTitle, color = Color(0xFF2F90D9))
        Spacer(Modifier.height(6.dp))
        Text(pwReq1)
        Text(pwReq2)
        Text(pwReq3)
        Text(pwReq4)

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                when {
                    newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                        Toast.makeText(context, errorFill, Toast.LENGTH_SHORT).show()
                    }
                    !isPasswordValid(newPassword) -> {
                        Toast.makeText(context, errorInvalid, Toast.LENGTH_SHORT).show()
                    }
                    newPassword != confirmPassword -> {
                        Toast.makeText(context, errorMismatch, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Aquí iría la lógica para cambiar la contraseña en tu backend
                        Toast.makeText(context, successChanged, Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.RecoverSuccess.route)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(nextButton)
        }

        Spacer(Modifier.height(24.dp))
        Text(
            noteText,
            color = Color(0xFF2F90D9),
            fontSize = 14.sp
        )
    }
}
