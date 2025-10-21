package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun RecoverSuccessScreen(navController: NavHostController) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Contraseña restablecida con éxito.", color = Color(0xFF2F90D9))
            Spacer(Modifier.height(40.dp))
            Button(onClick = { navController.navigate(Screen.Login.route) }, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F90D9))) { Text("Siguiente", color = Color.White) }
        }
    }
}
