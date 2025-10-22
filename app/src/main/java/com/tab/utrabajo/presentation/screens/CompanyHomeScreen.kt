package com.tab.utrabajo.ui.company

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun CompanyHomeScreen(
    navController: NavHostController // 🔹 CAMBIO: Agregar este parámetro
) {
    val repo = FirebaseRepository.getInstance()
    val user = repo.getCurrentUser()
    val uid = user?.uid
    var companyData by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        if (uid != null) {
            val db = FirebaseRepository.getInstance()
            db.getUserProfile(uid, onSuccess = {
                companyData = it
                loading = false
            }, onError = {
                loading = false
            })
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido, ${companyData["nombre"] ?: "Empresa"}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))

            Text("📧 Email: ${companyData["email"] ?: "-"}")
            Text("🏢 NIT: ${companyData["nit"] ?: "-"}")
            Text("📞 Teléfono: ${companyData["telefono"] ?: "-"}")

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate(Screen.CompanyDocsUpload.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Subir documentos")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    navController.navigate(Screen.CompanyRepInfo.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Representante legal")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    repo.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}