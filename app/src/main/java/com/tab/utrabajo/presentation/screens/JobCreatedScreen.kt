package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.res.stringResource
import com.tab.utrabajo.R

@Composable
fun JobCreatedScreen(navController: NavController) {
    val title = stringResource(R.string.jobcreated_title)
    val subtitle = stringResource(R.string.jobcreated_subtitle)
    val successIconDesc = stringResource(R.string.jobcreated_icon_success_desc)
    val homeBtnLabel = stringResource(R.string.jobcreated_btn_home)
    val createAnotherLabel = stringResource(R.string.jobcreated_btn_create_another)
    val chatBtnLabel = stringResource(R.string.jobcreated_btn_chat)
    val homeIconDesc = stringResource(R.string.jobcreated_icon_home_desc)
    val workIconDesc = stringResource(R.string.jobcreated_icon_work_desc)
    val chatIconDesc = stringResource(R.string.jobcreated_icon_chat_desc)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de éxito
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = successIconDesc,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = subtitle,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate("company_home") {
                        popUpTo("company_home") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Filled.Home, contentDescription = homeIconDesc)
                Spacer(modifier = Modifier.width(8.dp))
                Text(homeBtnLabel)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate("create_job")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Filled.Work, contentDescription = workIconDesc)
                Spacer(modifier = Modifier.width(8.dp))
                Text(createAnotherLabel)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate("chat")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = chatIconDesc)
                Spacer(modifier = Modifier.width(8.dp))
                Text(chatBtnLabel)
            }
        }
    }
}
