package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
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
import androidx.compose.material3.ButtonDefaults

@Composable
fun RoleSelectionScreen(navController: NavHostController) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF2F90D9)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("UT", fontSize = 64.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(40.dp))
            Button(onClick = { navController.navigate(Screen.RegisterStudent.route) }, modifier = Modifier.fillMaxWidth(0.7f).height(48.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                Text("Estudiante", color = Color(0xFF2F90D9))
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.navigate(Screen.RegisterCompany.route) }, modifier = Modifier.fillMaxWidth(0.7f).height(48.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                Text("Empresa", color = Color(0xFF2F90D9))
            }
        }
    }
}
