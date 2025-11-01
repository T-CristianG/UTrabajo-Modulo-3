package com.tab.utrabajo

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class FirebaseRepository private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: FirebaseRepository? = null

        fun getInstance(): FirebaseRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseRepository().also { INSTANCE = it }
            }
        }
    }

    // Instancias de Firebase
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // Obtener usuario actual
    fun getCurrentUser() = auth.currentUser

    // -------------------
    // Registro, login, logout
    // -------------------

    fun registerStudent(
        email: String,
        password: String,
        fullName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val uid = user?.uid ?: run {
                        onError("Error al obtener UID del usuario")
                        return@addOnCompleteListener
                    }

                    // Guardar en Firestore
                    val data = hashMapOf(
                        "uid" to uid,
                        "nombre" to fullName,
                        "email" to email,
                        "rol" to "estudiante",
                        "fechaRegistro" to Timestamp.now(),
                        "completado" to false
                    )

                    db.collection("usuarios").document(uid)
                        .set(data)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e ->
                            onError(e.message ?: "Error al guardar usuario")
                        }
                } else {
                    onError(task.exception?.message ?: "Error al registrar usuario")
                }
            }
    }

    fun registerCompany(
        nit: String,
        phone: String,
        email: String,
        workers: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val uid = user?.uid ?: run {
                        onError("Error al obtener UID de la empresa")
                        return@addOnCompleteListener
                    }

                    val data = hashMapOf(
                        "uid" to uid,
                        "nit" to nit,
                        "telefono" to phone,
                        "email" to email,
                        "numeroTrabajadores" to workers,
                        "rol" to "empresa",
                        "fechaRegistro" to Timestamp.now(),
                        "completado" to false
                    )

                    db.collection("empresas").document(uid)
                        .set(data)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e ->
                            onError(e.message ?: "Error al guardar empresa")
                        }
                } else {
                    onError(task.exception?.message ?: "Error al registrar empresa")
                }
            }
    }

    // Documento de representante
    fun saveCompanyRepresentative(
        userId: String,
        repName: String,
        docType: String,
        docNumber: String,
        docUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (docUri != null) {
            val docRef = storage.reference.child("empresas/$userId/representante/${UUID.randomUUID()}.pdf")
            docRef.putFile(docUri)
                .addOnSuccessListener {
                    docRef.downloadUrl.addOnSuccessListener { docUrl ->
                        val data = hashMapOf<String, Any>(
                            "representanteLegal" to repName,
                            "tipoDocumento" to docType,
                            "numeroDocumento" to docNumber,
                            "documentoRepresentanteUrl" to docUrl.toString(),
                            "ultimaActualizacion" to Timestamp.now()
                        )

                        db.collection("empresas").document(userId)
                            .update(data)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e ->
                                onError("Error guardando información del representante: ${e.message}")
                            }
                    }.addOnFailureListener { e ->
                        onError("Error obteniendo URL del documento: ${e.message}")
                    }
                }
                .addOnFailureListener { e ->
                    onError("Error subiendo documento: ${e.message}")
                }
        } else {
            val data = hashMapOf<String, Any>(
                "representanteLegal" to repName,
                "tipoDocumento" to docType,
                "numeroDocumento" to docNumber,
                "ultimaActualizacion" to Timestamp.now()
            )

            db.collection("empresas").document(userId)
                .update(data)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e ->
                    onError("Error guardando información del representante: ${e.message}")
                }
        }
    }

    // Documentos opcionales para CompanyDocumentsUpload
    fun uploadCompanyDocuments(
        userId: String,
        rutUri: Uri?,
        camaraComercioUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        var rutUrl: String? = null
        var camaraUrl: String? = null
        var tasksCompleted = 0
        val totalTasks = 2

        fun checkCompletion() {
            tasksCompleted++
            if (tasksCompleted == totalTasks) {
                val data = hashMapOf<String, Any>()

                rutUrl?.let { data["rutUrl"] = it }
                camaraUrl?.let { data["camaraComercioUrl"] = it }

                data["completado"] = true
                data["ultimaActualizacion"] = Timestamp.now()

                db.collection("empresas").document(userId)
                    .update(data)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e ->
                        onError("Error guardando información: ${e.message}")
                    }
            }
        }

        if (rutUri != null) {
            val rutRef = storage.reference.child("empresas/$userId/documentos/rut_${UUID.randomUUID()}.pdf")
            rutRef.putFile(rutUri)
                .addOnSuccessListener {
                    rutRef.downloadUrl.addOnSuccessListener { url ->
                        rutUrl = url.toString()
                        checkCompletion()
                    }.addOnFailureListener { e ->
                        checkCompletion()
                    }
                }
                .addOnFailureListener { e ->
                    checkCompletion()
                }
        } else {
            tasksCompleted++
            checkCompletion()
        }

        if (camaraComercioUri != null) {
            val camaraRef = storage.reference.child("empresas/$userId/documentos/camara_${UUID.randomUUID()}.pdf")
            camaraRef.putFile(camaraComercioUri)
                .addOnSuccessListener {
                    camaraRef.downloadUrl.addOnSuccessListener { url ->
                        camaraUrl = url.toString()
                        checkCompletion()
                    }.addOnFailureListener { e ->
                        checkCompletion()
                    }
                }
                .addOnFailureListener { e ->
                    checkCompletion()
                }
        } else {
            tasksCompleted++
            checkCompletion()
        }
    }

    // Funciones de estudiante
    fun saveStudentWorkInfo(
        userId: String,
        worksNow: Boolean,
        companyName: String = "",
        role: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val data = hashMapOf<String, Any>(
            "trabajaActual" to worksNow,
            "empresaActual" to companyName,
            "rolActual" to role,
            "ultimaActualizacion" to Timestamp.now()
        )

        db.collection("usuarios").document(userId)
            .update(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al guardar información laboral")
            }
    }

    fun saveStudentSkills(
        userId: String,
        skills: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val data = hashMapOf<String, Any>(
            "habilidades" to skills,
            "ultimaActualizacion" to Timestamp.now()
        )

        db.collection("usuarios").document(userId)
            .update(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al guardar habilidades") }
    }

    fun uploadCV(
        fileUri: Uri,
        userId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val ref = storage.reference.child("cvs/$userId/${UUID.randomUUID()}.pdf")
        ref.putFile(fileUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    val updateData = hashMapOf<String, Any>(
                        "cvUrl" to uri.toString(),
                        "cvSubido" to true,
                        "completado" to true,
                        "ultimaActualizacion" to Timestamp.now()
                    )

                    db.collection("usuarios").document(userId)
                        .update(updateData)
                        .addOnSuccessListener { onSuccess(uri.toString()) }
                        .addOnFailureListener { e ->
                            onError("Error guardando URL del CV: ${e.message}")
                        }
                }.addOnFailureListener { e ->
                    onError("Error obteniendo URL del archivo: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                onError("Error subiendo archivo: ${e.message}")
            }
    }

    // Login y logout
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Error al iniciar sesión")
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    // -------------------
    // Funciones para perfil
    // -------------------

    fun getUserProfile(
        userId: String,
        onSuccess: (Map<String, Any?>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(doc.data ?: emptyMap())
                } else {
                    onSuccess(emptyMap())
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al leer perfil")
            }
    }

    fun updateUserProfile(
        userId: String,
        phone: String?,
        address: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val data = hashMapOf<String, Any>()
        phone?.let { data["telefono"] = it }
        address?.let { data["direccion"] = it }
        data["ultimaActualizacion"] = Timestamp.now()

        db.collection("usuarios").document(userId)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error actualizando perfil") }
    }

    fun uploadAvatar(
        userId: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val ref = storage.reference.child("avatars/$userId/avatar_${UUID.randomUUID()}.jpg")

        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()

                    val userData = hashMapOf<String, Any>(
                        "photoUrl" to imageUrl,
                        "ultimaActualizacion" to Timestamp.now()
                    )

                    db.collection("usuarios").document(userId)
                        .set(userData, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            onSuccess(imageUrl)
                        }
                        .addOnFailureListener { e ->
                            onError("Error guardando en Firestore: ${e.message}")
                        }
                }
                    .addOnFailureListener { e ->
                        onError("Error obteniendo URL: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onError("Error subiendo imagen: ${e.message}")
            }
    }

    fun updateAuthProfileAndFirestore(
        userId: String,
        displayName: String?,
        photoUrl: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            val builder = UserProfileChangeRequest.Builder()
            displayName?.let { builder.setDisplayName(it) }
            photoUrl?.let { builder.setPhotoUri(Uri.parse(it)) }
            user.updateProfile(builder.build()).addOnCompleteListener { task ->
                val data = hashMapOf<String, Any>()
                displayName?.let { data["nombre"] = it }
                photoUrl?.let { data["photoUrl"] = it }
                data["ultimaActualizacion"] = Timestamp.now()

                db.collection("usuarios").document(userId)
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.message ?: "Error guardando perfil") }
            }
        } else {
            onError("Usuario no autenticado")
        }
    }

    // -------------------
    // FUNCIONES DE EMPLEOS (LAS QUE TE FALTABAN)
    // -------------------

    fun createJobOffer(
        companyId: String,
        title: String,
        description: String,
        requirements: List<String>,
        salary: String?,
        location: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val jobId = UUID.randomUUID().toString()

        val data = hashMapOf<String, Any?>(
            "id" to jobId,
            "empresaId" to companyId,
            "titulo" to title,
            "descripcion" to description,
            "requisitos" to requirements,
            "salario" to salary,
            "ubicacion" to location,
            "fechaPublicacion" to Timestamp.now(),
            "activa" to true
        )

        db.collection("empleos").document(jobId)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al crear la oferta de empleo")
            }
    }

    fun updateJobOffer(
        jobId: String,
        title: String?,
        salary: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val data = hashMapOf<String, Any>()
        title?.let { data["titulo"] = it }
        salary?.let { data["salario"] = it }
        data["ultimaActualizacion"] = Timestamp.now()

        db.collection("empleos").document(jobId)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error actualizando oferta") }
    }

    fun deleteJobOffer(
        jobId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("empleos").document(jobId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error eliminando oferta")
            }
    }

    fun getActiveJobOffers(
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("empleos")
            .whereEqualTo("activa", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val jobs = querySnapshot.documents.mapNotNull { it.data }
                onSuccess(jobs)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al cargar empleos")
            }
    }

    fun listenToActiveJobOffers(
        onUpdate: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return db.collection("empleos")
            .whereEqualTo("activa", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Error al escuchar empleos")
                    return@addSnapshotListener
                }
                val jobs = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                onUpdate(jobs)
            }
    }

    // -------------------
    // FUNCIONES DE POSTULACIONES Y CHAT
    // -------------------

    fun applyToJob(
        jobId: String,
        studentId: String,
        companyId: String,
        jobTitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val applicationId = UUID.randomUUID().toString()
        val data = hashMapOf<String, Any>(
            "id" to applicationId,
            "jobId" to jobId,
            "studentId" to studentId,
            "companyId" to companyId,
            "jobTitle" to jobTitle,
            "status" to "active",
            "applicationDate" to Timestamp.now()
        )

        db.collection("applications").document(applicationId)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al aplicar al empleo")
            }
    }

    fun getStudentApplications(
        studentId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("applications")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val applications = querySnapshot.documents.mapNotNull { it.data }
                onSuccess(applications)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al cargar las postulaciones")
            }
    }

    fun cancelApplication(
        applicationId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("applications").document(applicationId)
            .update("status", "cancelled")
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al cancelar la postulación")
            }
    }

    // Funciones para el chat
    fun createOrGetChat(
        studentId: String,
        companyId: String,
        jobId: String,
        jobTitle: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("chats")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("jobId", jobId)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val chatId = querySnapshot.documents.first().id
                    onSuccess(chatId)
                } else {
                    val chatId = UUID.randomUUID().toString()
                    val chatData = hashMapOf<String, Any>(
                        "id" to chatId,
                        "studentId" to studentId,
                        "companyId" to companyId,
                        "jobId" to jobId,
                        "jobTitle" to jobTitle,
                        "createdAt" to Timestamp.now(),
                        "lastMessage" to "",
                        "lastMessageTime" to Timestamp.now()
                    )

                    db.collection("chats").document(chatId)
                        .set(chatData)
                        .addOnSuccessListener { onSuccess(chatId) }
                        .addOnFailureListener { e ->
                            onError("Error creando chat: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                onError("Error buscando chat: ${e.message}")
            }
    }

    fun sendMessage(
        chatId: String,
        senderId: String,
        messageText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val messageId = UUID.randomUUID().toString()
        val messageData = hashMapOf<String, Any>(
            "id" to messageId,
            "chatId" to chatId,
            "senderId" to senderId,
            "message" to messageText,
            "timestamp" to Timestamp.now()
        )

        db.collection("messages").document(messageId)
            .set(messageData)
            .addOnSuccessListener {
                // Actualizar último mensaje en el chat
                val updateData = hashMapOf<String, Any>(
                    "lastMessage" to messageText,
                    "lastMessageTime" to Timestamp.now()
                )
                db.collection("chats").document(chatId)
                    .update(updateData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e ->
                        onError("Error actualizando último mensaje: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onError("Error enviando mensaje: ${e.message}")
            }
    }

    fun getChatsForUser(
        userId: String,
        userType: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        val field = if (userType == "student") "studentId" else "companyId"

        db.collection("chats")
            .whereEqualTo(field, userId)
            .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val chats = querySnapshot.documents.mapNotNull { it.data }
                onSuccess(chats)
            }
            .addOnFailureListener { e ->
                onError("Error cargando chats: ${e.message}")
            }
    }

    fun getMessages(
        chatId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("messages")
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val messages = querySnapshot.documents.mapNotNull { it.data }
                onSuccess(messages)
            }
            .addOnFailureListener { e ->
                onError("Error cargando mensajes: ${e.message}")
            }
    }

    // Listener en tiempo real para mensajes
    fun listenToMessages(
        chatId: String,
        onUpdate: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return db.collection("messages")
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Error escuchando mensajes")
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                onUpdate(messages)
            }
    }
}