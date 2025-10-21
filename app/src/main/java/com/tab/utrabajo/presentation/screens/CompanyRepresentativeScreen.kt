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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.presentation.components.SingleDocumentUploadField
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun CompanyRepresentativeScreen(navController: NavHostController) {
    var repName by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf("") }
    var docNumber by remember { mutableStateOf("") }
    var docUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val docLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> docUri = uri }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Top) {
        Spacer(Modifier.height(8.dp))

        errorMessage?.let { msg ->
            Text(text = msg, color = Color.Red, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }

        Text("Nombre del representante legal.", color = Color(0xFF2F90D9))
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = repName,
            onValueChange = { repName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre del representante legal *") },
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = docType,
            onValueChange = { docType = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Tipo de documento *") },
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = docNumber,
            onValueChange = { docNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Número de documento *") },
            enabled = !isLoading
        )
        Spacer(Modifier.height(12.dp))
        Text("Suba copia del documento del representante legal.", color = Color(0xFF2F90D9))
        Spacer(Modifier.height(12.dp))

        SingleDocumentUploadField(
            label = "Por favor, adjunte copia del documento",
            selectedFileUri = docUri,
            onFileSelected = { if (!isLoading) docLauncher.launch("application/pdf") }
        )

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                if (repName.isBlank() || docType.isBlank() || docNumber.isBlank()) {
                    errorMessage = "Por favor complete todos los campos"
                    return@Button
                }

                if (docUri == null) {
                    errorMessage = "Por favor adjunte el documento del representante"
                    return@Button
                }

                val currentUser = FirebaseRepository.getInstance().getCurrentUser()
                if (currentUser == null) {
                    errorMessage = "Empresa no registrada. Por favor, reinicie el proceso."
                    return@Button
                }

                isLoading = true
                errorMessage = null

                FirebaseRepository.getInstance().saveCompanyRepresentative(
                    userId = currentUser.uid,
                    repName = repName.trim(),
                    docType = docType.trim(),
                    docNumber = docNumber.trim(),
                    docUri = docUri!!,
                    onSuccess = {
                        isLoading = false
                        navController.navigate(Screen.CompanyDocsUpload.route)
                    },
                    onError = { error ->
                        isLoading = false
                        errorMessage = error
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) Text("Guardando...")
            else Text("Siguiente")
        }
    }
}
