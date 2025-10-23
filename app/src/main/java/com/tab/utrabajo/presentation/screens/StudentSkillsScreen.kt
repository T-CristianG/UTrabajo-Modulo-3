package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.R
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun StudentSkillsScreen(navController: NavHostController) {
    val skills = remember { mutableStateListOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Strings
    val title = stringResource(R.string.studentskills_title)
    val addSkillLabel = stringResource(R.string.studentskills_add_skill)
    val unauthenticatedError = stringResource(R.string.studentskills_error_unauthenticated)
    val atLeastOneError = stringResource(R.string.studentskills_error_at_least_one)
    val savingText = stringResource(R.string.studentskills_saving)
    val continueText = stringResource(R.string.studentskills_continue)
    val genericErrorFmt = stringResource(R.string.studentskills_error_fmt)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        Text(
            title,
            color = Color(0xFF2F90D9),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn {
            itemsIndexed(skills) { index, skill ->
                OutlinedTextField(
                    value = skill,
                    onValueChange = { skills[index] = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = { Text("${index + 1}.") },
                    enabled = !isLoading
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (!isLoading) skills.add("")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F90D9)),
            enabled = !isLoading
        ) {
            Text(addSkillLabel, color = Color.White)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val currentUser = FirebaseRepository.getInstance().getCurrentUser()
                if (currentUser == null) {
                    errorMessage = unauthenticatedError
                    return@Button
                }

                val habilidadesValidas = skills.filter { it.isNotBlank() }
                if (habilidadesValidas.isEmpty()) {
                    errorMessage = atLeastOneError
                    return@Button
                }

                isLoading = true
                errorMessage = null

                FirebaseRepository.getInstance().saveStudentSkills(
                    userId = currentUser.uid,
                    skills = habilidadesValidas,
                    onSuccess = {
                        isLoading = false
                        navController.navigate(Screen.StudentUploadCV.route)
                    },
                    onError = { error ->
                        isLoading = false
                        // formateamos el error con la string de recurso
                        val errText = error ?: ""
                        errorMessage = String.format(genericErrorFmt, errText)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) Text(savingText) else Text(continueText)
        }
    }
}
