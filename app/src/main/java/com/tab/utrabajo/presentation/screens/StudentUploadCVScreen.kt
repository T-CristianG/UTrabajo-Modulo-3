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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.R
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

    // Strings
    val titleText = stringResource(R.string.studentupload_title)
    val uploadFieldLabel = stringResource(R.string.studentupload_field_label)
    val unauthenticatedError = stringResource(R.string.studentupload_error_unauthenticated)
    val uploadingText = stringResource(R.string.studentupload_uploading)
    val uploadButtonText = stringResource(R.string.studentupload_button_upload_and_finish)
    val uploadButtonUploading = stringResource(R.string.studentupload_button_uploading)
    val continueWithFileText = stringResource(R.string.studentupload_continue_with_file)
    val continueWithoutFileText = stringResource(R.string.studentupload_continue_without_file)
    val genericErrorFmt = stringResource(R.string.studentupload_error_fmt)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            text = titleText,
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
            label = uploadFieldLabel,
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
                            errorMessage = unauthenticatedError
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
                                val errText = error ?: ""
                                errorMessage = String.format(genericErrorFmt, errText)
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
                    Text(uploadButtonUploading, color = Color.White)
                } else {
                    Text(uploadButtonText, color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Botón para continuar sin subir CV (o continuar si hay archivo pero no querer subir)
        Button(
            onClick = {
                navController.navigate(Screen.RegistrationComplete.route)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666))
        ) {
            Text(
                text = if (selectedFileUri.value != null) continueWithFileText else continueWithoutFileText,
                color = Color.White
            )
        }
    }
}
