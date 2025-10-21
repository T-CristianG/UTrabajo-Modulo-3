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
import androidx.navigation.NavHostController
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.presentation.components.SingleDocumentUploadField
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun CompanyDocumentsUploadScreen(navController: NavHostController) {
    var rutUri by remember { mutableStateOf<Uri?>(null) }
    var camaraComercioUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val rutLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> rutUri = uri }
    val camaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> camaraComercioUri = uri }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Top) {
        Spacer(Modifier.height(8.dp))

        errorMessage?.let { msg ->
            Text(text = msg, color = Color.Red, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }

        Text(
            "En este módulo, por favor, adjunte los documentos solicitados.",
            color = Color(0xFF2F90D9),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(16.dp))
        Text("Suba copia del RUT.", color = Color(0xFF2F90D9))
        Spacer(Modifier.height(8.dp))

        SingleDocumentUploadField(
            label = "Por favor, adjunte copia del RUT.",
            selectedFileUri = rutUri,
            onFileSelected = { if (!isLoading) rutLauncher.launch("application/pdf") }
        )

        Spacer(Modifier.height(16.dp))
        Text("Suba copia de la Cámara de Comercio.", color = Color(0xFF2F90D9))
        Spacer(Modifier.height(8.dp))

        SingleDocumentUploadField(
            label = "Por favor, adjunte copia de la Cámara de Comercio.",
            selectedFileUri = camaraComercioUri,
            onFileSelected = { if (!isLoading) camaraLauncher.launch("application/pdf") }
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (rutUri == null || camaraComercioUri == null) {
                    errorMessage = "Por favor adjunte todos los documentos requeridos"
                    return@Button
                }

                val currentUser = FirebaseRepository.getInstance().getCurrentUser()
                if (currentUser == null) {
                    errorMessage = "Empresa no registrada. Por favor, reinicie el proceso."
                    return@Button
                }

                isLoading = true
                errorMessage = null

                FirebaseRepository.getInstance().uploadCompanyDocuments(
                    userId = currentUser.uid,
                    rutUri = rutUri!!,
                    camaraComercioUri = camaraComercioUri!!,
                    onSuccess = {
                        isLoading = false
                        navController.navigate(Screen.CompleteCompany.route)
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
            if (isLoading) Text("Subiendo documentos...")
            else Text("Finalizar")
        }
    }
}
