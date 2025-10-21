package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun CompleteCompanyScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Completa tu Usuario",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color(0xFFDCEAF6), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = "avatar"
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        var nit by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var workers by remember { mutableStateOf("") }

        androidx.compose.material3.OutlinedTextField(value = nit, onValueChange = { nit = it }, modifier = Modifier.fillMaxWidth(), label = { Text("NIT y Nombre de la empresa") })
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.OutlinedTextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Número Telefónico de la empresa") })
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Correo de la empresa") })
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.OutlinedTextField(value = workers, onValueChange = { workers = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Número de trabajadores") })
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { navController.navigate(Screen.RegistrationComplete.route) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Siguiente")
        }
    }
}
