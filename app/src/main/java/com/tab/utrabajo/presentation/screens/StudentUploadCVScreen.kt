package com.tab.utrabajo.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.presentation.components.SingleDocumentUploadField
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun StudentUploadCVScreen(navController: NavHostController) {
    val selectedFileUri = remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedFileUri.value = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Subir HV",
            fontSize = 20.sp,
            color = Color(0xFF2F90D9)
        )

        Spacer(Modifier.height(12.dp))

        // Mostrar error si existe
        errorMessage?.let { msg ->
            Text(
                text = msg,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        SingleDocumentUploadField(
            label = "Adjunte su hoja de vida (PDF) - Opcional",
            selectedFileUri = selectedFileUri.value,
            onFileSelected = {
                if (!isLoading) launcher.launch("application/pdf")
            }
        )

        Spacer(Modifier.height(32.dp))

        // Botón para subir CV si hay archivo seleccionado
        if (selectedFileUri.value != null) {
            Button(
                onClick = {
                    val fileUri = selectedFileUri.value
                    if (fileUri != null) {
                        isLoading = true
                        errorMessage = null

                        val currentUserId = FirebaseRepository.getInstance().getCurrentUser()?.uid
                        if (currentUserId == null) {
                            errorMessage = "Usuario no autenticado"
                            isLoading = false
                            return@Button
                        }

                        FirebaseRepository.getInstance().uploadCV(
                            fileUri = fileUri,
                            userId = currentUserId,
                            onSuccess = { _ ->
                                isLoading = false
                                navController.navigate(Screen.RegistrationComplete.route)
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F90D9)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    Text("Subiendo...", color = Color.White)
                } else {
                    Text("Subir HV y Finalizar", color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Botón para continuar sin subir CV
        Button(
            onClick = {
                navController.navigate(Screen.RegistrationComplete.route)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666))
        ) {
            Text(
                text = if (selectedFileUri.value != null) "Continuar sin subir HV" else "Finalizar sin subir HV",
                color = Color.White
            )
        }
    }
}