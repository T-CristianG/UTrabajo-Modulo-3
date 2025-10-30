package com.tab.utrabajo.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object RoleSelection : Screen("role_selection")
    object RegisterStudent : Screen("register_student")
    object StudentWorkInfo : Screen("student_work_info")
    object StudentSkills : Screen("student_skills")
    object StudentUploadCV : Screen("student_upload_cv")
    object RegisterCompany : Screen("register_company")
    object CompanyRepInfo : Screen("company_rep_info")
    object CompanyDocsUpload : Screen("company_docs_upload")
    object CompleteCompany : Screen("complete_company")
    object RegistrationComplete : Screen("registration_complete")
    object RecoverStart : Screen("recover_start")
    object VerifyCode : Screen("verify_code")
    object ResetPassword : Screen("reset_password")
    object RecoverSuccess : Screen("recover_success")

    // <-- Home y Perfil que añadimos:
    object JobsList : Screen("jobs_list")
    object Profile : Screen("profile")
    object CompanyHome : Screen("company_home") // ✅ Agregada esta línea
    object CreateJob : Screen("create_job")
    object JobCreated : Screen("job_created")

    // NUEVAS PANTALLAS DE CHAT
    object ChatList : Screen("chat_list")
    object ChatDetail : Screen("chat_detail")



}
