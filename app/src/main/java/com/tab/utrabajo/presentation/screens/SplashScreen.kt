package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun SplashScreen(navController: NavHostController) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF2F90D9)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("UT", fontSize = 72.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Text("Tu futuro profesional comienza aquí.", color = Color.White)
            Spacer(Modifier.height(80.dp))
            Button(
                onClick = { navController.navigate(Screen.Login.route) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Iniciar Sesión", color = Color(0xFF2F90D9))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate(Screen.RoleSelection.route) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(0.6f).height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Registro", color = Color(0xFF2F90D9))
            }
        }
    }
}
