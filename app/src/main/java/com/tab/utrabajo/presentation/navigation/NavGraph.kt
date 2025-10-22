package com.tab.utrabajo.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tab.utrabajo.presentation.screens.*
// 🔹 AGREGAR ESTE IMPORT:
import com.tab.utrabajo.ui.company.CompanyHomeScreen

// ... el resto de tu código permanece igual ...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            val canNavigateBack = navController.previousBackStackEntry != null
            if (canNavigateBack) {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_media_previous),
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) { SplashScreen(navController) }
            composable(Screen.Login.route) { LoginScreen(navController) }
            composable(Screen.RoleSelection.route) { RoleSelectionScreen(navController) }
            composable(Screen.RegisterStudent.route) { RegisterStudentScreen(navController) }
            composable(Screen.StudentWorkInfo.route) { StudentWorkInfoScreen(navController) }
            composable(Screen.StudentSkills.route) { StudentSkillsScreen(navController) }
            composable(Screen.StudentUploadCV.route) { StudentUploadCVScreen(navController) }
            composable(Screen.RegisterCompany.route) { RegisterCompanyScreen(navController) }
            composable(Screen.CompanyRepInfo.route) { CompanyRepresentativeScreen(navController) }
            composable(Screen.CompanyDocsUpload.route) { CompanyDocumentsUploadScreen(navController) }
            composable(Screen.CompleteCompany.route) { CompleteCompanyScreen(navController) }
            composable(Screen.RegistrationComplete.route) { RegistrationCompleteScreen(navController) }
            composable(Screen.RecoverStart.route) { RecoverStartScreen(navController) }
            composable(Screen.VerifyCode.route) { VerifyCodeScreen(navController) }
            composable(Screen.ResetPassword.route) { ResetPasswordScreen(navController) }
            composable(Screen.RecoverSuccess.route) { RecoverSuccessScreen(navController) }

            // ✅ Esto ahora funcionará porque tenemos el import correcto
            composable(Screen.CompanyHome.route) { CompanyHomeScreen(navController) }

            composable(Screen.JobsList.route) { JobsListScreen(navController) }
            composable(Screen.Profile.route) { ProfileScreen(navController = navController) }
        }
    }
}