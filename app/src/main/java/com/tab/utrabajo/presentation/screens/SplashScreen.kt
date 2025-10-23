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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.tab.utrabajo.presentation.navigation.Screen
import com.tab.utrabajo.R

@Composable
fun SplashScreen(navController: NavHostController) {
    val appTitle = stringResource(R.string.splash_title)
    val slogan = stringResource(R.string.splash_slogan)
    val signInLabel = stringResource(R.string.splash_sign_in)
    val registerLabel = stringResource(R.string.splash_register)

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF2F90D9)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(appTitle, fontSize = 72.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Text(slogan, color = Color.White)
            Spacer(Modifier.height(80.dp))
            Button(
                onClick = { navController.navigate(Screen.Login.route) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(signInLabel, color = Color(0xFF2F90D9))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate(Screen.RoleSelection.route) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(registerLabel, color = Color(0xFF2F90D9))
            }
        }
    }
}
