package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.tab.utrabajo.R
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
            stringResource(R.string.completecompany_title),
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
                    contentDescription = stringResource(R.string.completecompany_avatar_desc)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        var nit by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var workers by remember { mutableStateOf("") }

        OutlinedTextField(
            value = nit,
            onValueChange = { nit = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.completecompany_label_nit)) }
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.completecompany_label_phone)) }
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.completecompany_label_email)) }
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = workers,
            onValueChange = { workers = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.completecompany_label_workers)) }
        )
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { navController.navigate(Screen.RegistrationComplete.route) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.completecompany_button_next))
        }
    }
}
