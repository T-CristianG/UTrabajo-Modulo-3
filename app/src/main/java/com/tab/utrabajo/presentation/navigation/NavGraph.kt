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

// NavGraph con uso del innerPadding para evitar aviso "parameter innerPadding is not used"
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
        // Pasamos innerPadding al NavHost vía modifier para usarlo y evitar warning.
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

            // Nuevas rutas: Home y Perfil
            // ...
            composable(Screen.JobsList.route) { JobsListScreen(navController) }
            composable(Screen.Profile.route) { ProfileScreen(navController) } // <- PASAMOS navController
// ...

        }
    }
}
