package com.tab.utrabajo

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
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
    // Registro, login, logout (tu código original)
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
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, "tempPassword123")
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

    fun saveCompanyRepresentative(
        userId: String,
        repName: String,
        docType: String,
        docNumber: String,
        docUri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
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
    }

    fun uploadCompanyDocuments(
        userId: String,
        rutUri: Uri,
        camaraComercioUri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val rutRef = storage.reference.child("empresas/$userId/documentos/rut_${UUID.randomUUID()}.pdf")
        val camaraRef = storage.reference.child("empresas/$userId/documentos/camara_${UUID.randomUUID()}.pdf")

        rutRef.putFile(rutUri)
            .addOnSuccessListener {
                rutRef.downloadUrl.addOnSuccessListener { rutUrl ->
                    camaraRef.putFile(camaraComercioUri)
                        .addOnSuccessListener {
                            camaraRef.downloadUrl.addOnSuccessListener { camaraUrl ->
                                val data = hashMapOf<String, Any>(
                                    "rutUrl" to rutUrl.toString(),
                                    "camaraComercioUrl" to camaraUrl.toString(),
                                    "completado" to true,
                                    "ultimaActualizacion" to Timestamp.now()
                                )

                                db.collection("empresas").document(userId)
                                    .update(data)
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { e ->
                                        onError("Error guardando URLs de documentos: ${e.message}")
                                    }
                            }.addOnFailureListener { e ->
                                onError("Error obteniendo URL de cámara de comercio: ${e.message}")
                            }
                        }
                        .addOnFailureListener { e ->
                            onError("Error subiendo cámara de comercio: ${e.message}")
                        }
                }.addOnFailureListener { e ->
                    onError("Error obteniendo URL del RUT: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                onError("Error subiendo RUT: ${e.message}")
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
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al guardar habilidades")
            }
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
    // Nuevas funciones para perfil
    // -------------------

    /**
     * Leer perfil del usuario desde Firestore (colección "usuarios")
     * Retorna un Map<String, Any?> (puede venir vacío)
     */
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

    /**
     * Actualizar teléfono y dirección (merge)
     */
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

    /**
     * Subir avatar (imagen) y devolver URL, también actualizar Auth display photo si quieres.
     * onSuccess(urlString)
     */
    fun uploadAvatar(
        userId: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val ref = storage.reference.child("avatars/$userId/avatar_${UUID.randomUUID()}.jpg")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    val url = uri.toString()
                    // Guardar url en Firestore y (opcional) en Auth
                    val data = hashMapOf<String, Any>(
                        "photoUrl" to url,
                        "ultimaActualizacion" to Timestamp.now()
                    )
                    db.collection("usuarios").document(userId)
                        .set(data, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            // actualizar Auth photoURL si existe user
                            auth.currentUser?.let { user ->
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setPhotoUri(uri)
                                    .build()
                                user.updateProfile(profileUpdates).addOnCompleteListener {
                                    // ignoramos resultado
                                    onSuccess(url)
                                }
                            } ?: onSuccess(url)
                        }
                        .addOnFailureListener { e -> onError(e.message ?: "Error guardando avatar") }
                }.addOnFailureListener { e ->
                    onError(e.message ?: "Error obteniendo URL de avatar")
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error subiendo avatar")
            }
    }

    /**
     * Actualizar displayName y/o photoUrl en Auth y Firestore (merge)
     */
    fun updateAuthProfileAndFirestore(
        userId: String,
        displayName: String?,
        photoUrl: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // actualizar Auth
        val user = auth.currentUser
        if (user != null) {
            val builder = UserProfileChangeRequest.Builder()
            displayName?.let { builder.setDisplayName(it) }
            photoUrl?.let { builder.setPhotoUri(Uri.parse(it)) }
            user.updateProfile(builder.build()).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    // no fatal: seguimos y guardamos en Firestore (pero avisamos)
                }
                // guardar en Firestore
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
}
