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

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // 🔹 Obtener usuario actual
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

                    // actualizar displayName en Auth
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()

                    user.updateProfile(profileUpdates).addOnCompleteListener {
                        // ignoramos el resultado aquí; seguimos guardando Firestore
                    }

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
        password: String, // Nuevo parámetro
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password) // Usar la contraseña real
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

    // 🔹 Documento de representante opcional
    fun saveCompanyRepresentative(
        userId: String,
        repName: String,
        docType: String,
        docNumber: String,
        docUri: Uri?, // Cambiado a nullable
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (docUri != null) {
            // Si hay documento, subirlo y luego guardar los datos
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
            // Si no hay documento, guardar solo los datos básicos
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

    // 🔹 MODIFICACIÓN: Documentos opcionales para CompanyDocumentsUpload
    fun uploadCompanyDocuments(
        userId: String,
        rutUri: Uri?,  // Cambiado a nullable
        camaraComercioUri: Uri?,  // Cambiado a nullable
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        var rutUrl: String? = null
        var camaraUrl: String? = null
        var tasksCompleted = 0
        val totalTasks = 2 // Siempre intentamos actualizar, aunque no haya archivos

        fun checkCompletion() {
            tasksCompleted++
            if (tasksCompleted == totalTasks) {
                val data = hashMapOf<String, Any>()

                // Solo agregamos las URLs si existen
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

        // Subir RUT si existe
        if (rutUri != null) {
            val rutRef = storage.reference.child("empresas/$userId/documentos/rut_${UUID.randomUUID()}.pdf")
            rutRef.putFile(rutUri)
                .addOnSuccessListener {
                    rutRef.downloadUrl.addOnSuccessListener { url ->
                        rutUrl = url.toString()
                        checkCompletion()
                    }.addOnFailureListener { e ->
                        // Si falla la descarga de URL, continuamos sin RUT
                        checkCompletion()
                    }
                }
                .addOnFailureListener { e ->
                    // Si falla la subida, continuamos sin RUT
                    checkCompletion()
                }
        } else {
            tasksCompleted++
            checkCompletion()
        }

        // Subir Cámara de Comercio si existe
        if (camaraComercioUri != null) {
            val camaraRef = storage.reference.child("empresas/$userId/documentos/camara_${UUID.randomUUID()}.pdf")
            camaraRef.putFile(camaraComercioUri)
                .addOnSuccessListener {
                    camaraRef.downloadUrl.addOnSuccessListener { url ->
                        camaraUrl = url.toString()
                        checkCompletion()
                    }.addOnFailureListener { e ->
                        // Si falla la descarga de URL, continuamos sin cámara
                        checkCompletion()
                    }
                }
                .addOnFailureListener { e ->
                    // Si falla la subida, continuamos sin cámara
                    checkCompletion()
                }
        } else {
            tasksCompleted++
            checkCompletion()
        }
    }

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
                // Obtener la URL de descarga
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()

                    // Guardar en Firestore
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
    // Funciones para empleos
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

    /**
     * Petición one-time para obtener empleos activos (no vinculante).
     */
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

    /**
     * Listener en tiempo real para empleos activos.
     * Devuelve ListenerRegistration para poder remover el listener cuando se deseé.
     */
    fun listenToActiveJobOffers(
        onUpdate: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return db.collection("empleos")
            .whereEqualTo("activa", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e.message ?: "Error al escuchar empleos")
                    return@addSnapshotListener
                }
                val jobs = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                onUpdate(jobs)
            }
    }

    // -------------------
    // Nuevas funciones: actualizar y eliminar oferta
    // -------------------

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
        // Eliminamos el documento completamente
        db.collection("empleos").document(jobId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error eliminando oferta")
            }
    }
}
