package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.tab.utrabajo.presentation.navigation.Screen
import com.tab.utrabajo.R
import androidx.compose.material3.ButtonDefaults

@Composable
fun RegistrationCompleteScreen(navController: NavHostController) {
    val message = stringResource(R.string.registration_complete_message)
    val finishButton = stringResource(R.string.registration_complete_button)
    val note = stringResource(R.string.registration_complete_note)

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message, color = Color(0xFF2F90D9))
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { navController.navigate(Screen.Splash.route) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F90D9))
            ) {
                Text(finishButton, color = Color.White)
            }
            Spacer(Modifier.height(24.dp))
            Text(
                note,
                color = Color(0xFF2F90D9),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
