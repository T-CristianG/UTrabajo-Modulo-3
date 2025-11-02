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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.tab.utrabajo.presentation.screens.ApplicationsScreen
import com.tab.utrabajo.presentation.screens.ChatDetailScreen
import com.tab.utrabajo.presentation.screens.ChatListScreen
import com.tab.utrabajo.presentation.screens.CompanyDocumentsUploadScreen
import com.tab.utrabajo.presentation.screens.CompanyRepresentativeScreen
import com.tab.utrabajo.presentation.screens.CompleteCompanyScreen
import com.tab.utrabajo.presentation.screens.CreateJobScreen
import com.tab.utrabajo.presentation.screens.EmpleoScreen
import com.tab.utrabajo.presentation.screens.JobCreatedScreen
import com.tab.utrabajo.presentation.screens.JobsListScreen
import com.tab.utrabajo.presentation.screens.LoginScreen
import com.tab.utrabajo.presentation.screens.ProfileScreen
import com.tab.utrabajo.presentation.screens.RecoverStartScreen
import com.tab.utrabajo.presentation.screens.RecoverSuccessScreen
import com.tab.utrabajo.presentation.screens.RegisterCompanyScreen
import com.tab.utrabajo.presentation.screens.RegisterStudentScreen
import com.tab.utrabajo.presentation.screens.RegistrationCompleteScreen
import com.tab.utrabajo.presentation.screens.ResetPasswordScreen
import com.tab.utrabajo.presentation.screens.RoleSelectionScreen
import com.tab.utrabajo.presentation.screens.StudentSkillsScreen
import com.tab.utrabajo.presentation.screens.StudentUploadCVScreen
import com.tab.utrabajo.presentation.screens.StudentWorkInfoScreen
import com.tab.utrabajo.presentation.screens.SplashScreen
import com.tab.utrabajo.presentation.screens.VerifyCodeScreen
import com.tab.utrabajo.presentation.navigation.Screen
// IMPORT CORREGIDO: CompanyHomeScreen vive en otro paquete UI
import com.tab.utrabajo.ui.company.CompanyHomeScreen

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

            // Company home screen - import corregido a com.tab.utrabajo.ui.company.CompanyHomeScreen
            composable(Screen.CompanyHome.route) { CompanyHomeScreen(navController) }

            // Ruta para pantalla de empleo (si la tenés separada)
            composable("empleo") { EmpleoScreen(navController) }

            // Lista de empleos (JobsListScreen)
            composable(Screen.JobsList.route) { JobsListScreen(navController) }

            // Perfil
            composable(Screen.Profile.route) { ProfileScreen(navController = navController) }

            // Rutas para crear empleo
            composable("create_job") { CreateJobScreen(navController) }
            composable("job_created") { JobCreatedScreen(navController) }

            // Chat list: registro de deep link para que la URI exista en el grafo.
            composable(
                route = Screen.ChatList.route,
                deepLinks = listOf(
                    navDeepLink { uriPattern = "android-app://androidx.navigation/company_chats" }
                )
            ) {
                ChatListScreen(navController)
            }

            // Chat detail
            composable(
                "${Screen.ChatDetail.route}/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId")
                ChatDetailScreen(navController, chatId)
            }

            // Pantalla de aplicaciones
            composable("applications_screen") { ApplicationsScreen(navController) }
        }
    }
}
