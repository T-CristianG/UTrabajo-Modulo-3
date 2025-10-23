package com.tab.utrabajo.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.R
import com.tab.utrabajo.presentation.components.SingleDocumentUploadField
import com.tab.utrabajo.presentation.navigation.Screen
import java.util.Locale

@Composable
fun CompanyRepresentativeScreen(navController: NavHostController) {
    var repName by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf("") }
    var docNumber by remember { mutableStateOf("") }
    var docUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val docLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        docUri = uri
    }

    // Recursos de texto
    val instruction = stringResource(R.string.companyrep_instruction)
    val repNameLabel = stringResource(R.string.companyrep_label_name)
    val docTypeLabel = stringResource(R.string.companyrep_label_type)
    val docNumberLabel = stringResource(R.string.companyrep_label_number)
    val uploadDocText = stringResource(R.string.companyrep_upload_text)
    val uploadDocLabel = stringResource(R.string.companyrep_label_upload)
    val validationError = stringResource(R.string.companyrep_error_fill_fields)
    val companyNotRegistered = stringResource(R.string.companyrep_error_no_registered)
    val savingText = stringResource(R.string.companyrep_saving)
    val nextText = stringResource(R.string.companyrep_next)
    val errorFmt = stringResource(R.string.companyrep_error_fmt)

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

        Text(instruction, color = Color(0xFF2F90D9))
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = repName,
            onValueChange = { repName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(repNameLabel) },
            enabled = !isLoading
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = docType,
            onValueChange = { docType = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(docTypeLabel) },
            enabled = !isLoading
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = docNumber,
            onValueChange = { docNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(docNumberLabel) },
            enabled = !isLoading
        )

        Spacer(Modifier.height(12.dp))
        Text(uploadDocText, color = Color(0xFF2F90D9))
        Spacer(Modifier.height(12.dp))

        SingleDocumentUploadField(
            label = uploadDocLabel,
            selectedFileUri = docUri,
            onFileSelected = {
                if (!isLoading) docLauncher.launch("application/pdf")
            }
        )

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                // Validaciones mínimas
                if (repName.isBlank() || docType.isBlank() || docNumber.isBlank()) {
                    errorMessage = validationError
                    return@Button
                }

                val currentUser = FirebaseRepository.getInstance().getCurrentUser()
                if (currentUser == null) {
                    errorMessage = companyNotRegistered
                    return@Button
                }

                isLoading = true
                errorMessage = null

                // <-- docUri puede ser null y se pasa tal cual
                FirebaseRepository.getInstance().saveCompanyRepresentative(
                    userId = currentUser.uid,
                    repName = repName.trim(),
                    docType = docType.trim(),
                    docNumber = docNumber.trim(),
                    docUri = docUri,
                    onSuccess = {
                        isLoading = false
                        navController.navigate(Screen.CompanyDocsUpload.route)
                    },
                    onError = { error ->
                        isLoading = false
                        // Formateamos mensaje; si error es null usamos cadena vacía
                        val errText = error ?: ""
                        errorMessage = String.format(Locale.getDefault(), errorFmt, errText)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) Text(savingText) else Text(nextText)
        }
    }
}
