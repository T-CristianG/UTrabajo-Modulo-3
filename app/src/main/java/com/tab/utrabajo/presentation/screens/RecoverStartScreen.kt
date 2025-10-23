package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tab.utrabajo.R
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun RecoverStartScreen(navController: NavHostController) {
    val instruction = stringResource(R.string.recover_instruction)
    val identifierLabel = stringResource(R.string.recover_label_identifier)
    val nextButton = stringResource(R.string.recover_button_next)
    val noteText = stringResource(R.string.recover_note_wait_period)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            instruction,
            color = Color(0xFF2F90D9)
        )
        Spacer(Modifier.height(12.dp))

        var identifier by remember { mutableStateOf("") }
        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(identifierLabel) }
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate(Screen.VerifyCode.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(nextButton)
        }

        Spacer(Modifier.height(24.dp))
        Text(
            noteText,
            color = Color(0xFF2F90D9)
        )
    }
}
