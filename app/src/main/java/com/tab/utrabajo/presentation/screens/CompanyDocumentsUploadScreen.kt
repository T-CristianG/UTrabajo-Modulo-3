package com.tab.utrabajo.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.R
import com.tab.utrabajo.presentation.components.SingleDocumentUploadField
import com.tab.utrabajo.presentation.navigation.Screen
import java.util.Locale

@Composable
fun CompanyDocumentsUploadScreen(navController: NavHostController) {
    var rutUri by remember { mutableStateOf<Uri?>(null) }
    var camaraComercioUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val rutLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> rutUri = uri }
    val camaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> camaraComercioUri = uri }

    // Recursos de texto
    val instructionText = stringResource(R.string.companydocs_instruction)
    val uploadRutText = stringResource(R.string.companydocs_upload_rut)
    val uploadRutLabel = stringResource(R.string.companydocs_label_rut)
    val uploadCamaraText = stringResource(R.string.companydocs_upload_camara)
    val uploadCamaraLabel = stringResource(R.string.companydocs_label_camara)
    val continueWithoutDocs = stringResource(R.string.companydocs_continue_without)
    val companyNotRegisteredMsg = stringResource(R.string.companydocs_error_no_registered)
    val processingText = stringResource(R.string.companydocs_processing)
    val continueText = stringResource(R.string.companydocs_button_continue)
    val errorFmt = stringResource(R.string.companydocs_error_fmt) // e.g. "Error: %s"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(8.dp))

        errorMessage?.let { msg ->
            Text(
                text = msg,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        Text(
            instructionText,
            color = Color(0xFF2F90D9),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(16.dp))

        Text(uploadRutText, color = Color(0xFF2F90D9))
        Spacer(Modifier.height(8.dp))

        SingleDocumentUploadField(
            label = uploadRutLabel,
            selectedFileUri = rutUri,
            onFileSelected = { if (!isLoading) rutLauncher.launch("application/pdf") }
        )

        Spacer(Modifier.height(16.dp))
        Text(uploadCamaraText, color = Color(0xFF2F90D9))
        Spacer(Modifier.height(8.dp))

        SingleDocumentUploadField(
            label = uploadCamaraLabel,
            selectedFileUri = camaraComercioUri,
            onFileSelected = { if (!isLoading) camaraLauncher.launch("application/pdf") }
        )

        Spacer(Modifier.height(24.dp))

        Text(
            continueWithoutDocs,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                val currentUser = FirebaseRepository.getInstance().getCurrentUser()
                if (currentUser == null) {
                    errorMessage = companyNotRegisteredMsg
                    return@Button
                }

                isLoading = true
                errorMessage = null

                FirebaseRepository.getInstance().uploadCompanyDocuments(
                    userId = currentUser.uid,
                    rutUri = rutUri,
                    camaraComercioUri = camaraComercioUri,
                    onSuccess = {
                        isLoading = false
                        navController.navigate(Screen.CompleteCompany.route)
                    },
                    onError = { error ->
                        isLoading = false
                        // error may be null — formateamos seguro
                        val errText = error ?: ""
                        // Formateamos el mensaje usando el patrón local obtenido antes
                        errorMessage = String.format(Locale.getDefault(), errorFmt, errText)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) Text(processingText) else Text(continueText)
        }
    }
}
