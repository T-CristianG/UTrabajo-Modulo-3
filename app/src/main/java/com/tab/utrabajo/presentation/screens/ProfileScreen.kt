package com.tab.utrabajo.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.presentation.navigation.Screen
import java.util.*

/**
 * ProfileScreen corregido:
 * - evita try/catch envolviendo llamadas @Composable (mueve try/catch a listeners / LaunchedEffect)
 * - admite navController opcional (por si tu NavGraph llama ProfileScreen() sin parámetro)
 * - sube avatar y CV usando FirebaseRepository / Storage
 * - guarda teléfono y dirección en Firestore
 */
@Composable
fun ProfileScreen(navController: NavHostController? = null) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Usuario actual (puede ser null)
    val currentUser = auth.currentUser

    // Si no hay usuario autenticado, redirigir a Login (si nos pasaron navController)
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController?.let {
                it.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    // Estados UI
    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    val email = currentUser?.email ?: ""
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(currentUser?.photoUrl?.toString()) }
    var cvUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    // Pickers (avatar y cv)
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val uid = currentUser?.uid
        if (uid == null) {
            message = "Usuario no autenticado"
            return@rememberLauncherForActivityResult
        }
        isLoading = true
        message = "Subiendo avatar..."
        val ref = FirebaseStorage.getInstance().reference.child("users/$uid/avatar_${UUID.randomUUID()}.jpg")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    avatarUrl = url.toString()
                    // Guardar en Firestore (merge)
                    db.collection("usuarios").document(uid)
                        .set(mapOf("photoUrl" to avatarUrl), com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            isLoading = false
                            message = "Avatar actualizado"
                            Toast.makeText(context, "Avatar actualizado", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            message = "Error guardando avatar: ${e.message}"
                        }
                }.addOnFailureListener { e ->
                    isLoading = false
                    message = "Error obteniendo URL avatar: ${e.message}"
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                message = "Error subiendo avatar: ${e.message}"
            }
    }

    val cvPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val uid = currentUser?.uid
        if (uid == null) {
            message = "Usuario no autenticado"
            return@rememberLauncherForActivityResult
        }
        isLoading = true
        message = "Subiendo CV..."
        // Usa FirebaseRepository (ya implementado)
        FirebaseRepository.getInstance().uploadCV(
            fileUri = uri,
            userId = uid,
            onSuccess = { url ->
                isLoading = false
                cvUrl = url
                message = "CV subido correctamente"
                Toast.makeText(context, "CV subido", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                isLoading = false
                message = "Error subiendo CV: $error"
            }
        )
    }

    // Cargar información del profile desde Firestore
    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid == null) return@LaunchedEffect
        isLoading = true
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                isLoading = false
                doc?.data?.let { data ->
                    phone = (data["telefono"] as? String) ?: ""
                    address = (data["direccion"] as? String) ?: ""
                    (data["cvUrl"] as? String)?.let { cvUrl = it }
                    (data["photoUrl"] as? String)?.let { avatarUrl = it }
                    val nameFromDoc = (data["nombre"] as? String)
                    if (!nameFromDoc.isNullOrBlank() && displayName.isBlank()) displayName = nameFromDoc
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                message = "Error leyendo perfil: ${e.message}"
            }
    }

    // UI
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(8.dp))

            // Avatar (placeholder seguro)
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                if (!avatarUrl.isNullOrBlank()) {
                    // Si no tienes Coil/AsyncImage instalado, usamos un icono por defecto para evitar crash
                    Image(
                        painter = painterResource(id = android.R.drawable.sym_def_app_icon),
                        contentDescription = "avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCEAF6)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCEAF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_camera),
                            contentDescription = "avatar icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Botón editar avatar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-6).dp, y = (-6).dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { avatarPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(id = android.R.drawable.ic_menu_edit), contentDescription = "editar avatar")
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(text = if (displayName.isNotBlank()) displayName else "Nombre no definido", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = email, color = Color(0xFF2F90D9))
            Spacer(Modifier.height(18.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = android.R.drawable.ic_menu_call), contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (phone.isNotBlank()) phone else "Sin teléfono", fontWeight = FontWeight.Medium)
                            Text(if (address.isNotBlank()) address else "Sin dirección")
                        }
                        IconButton(onClick = { /* puedes scrollear a los textfields para editar */ }) {
                            Icon(painter = painterResource(id = android.R.drawable.ic_menu_edit), contentDescription = "Editar")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Teléfono") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = address, onValueChange = { address = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Dirección") })

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                        val uid = currentUser?.uid
                        if (uid == null) {
                            message = "Usuario no autenticado"
                            return@Button
                        }
                        isLoading = true
                        db.collection("usuarios").document(uid)
                            .set(mapOf("telefono" to phone, "direccion" to address), com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener {
                                isLoading = false
                                message = "Perfil actualizado"
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                message = "Error guardando: ${e.message}"
                            }
                    },
                    enabled = !isLoading
                ) {
                    Text("Guardar")
                }

                Spacer(Modifier.width(8.dp))

                Button(onClick = { cvPicker.launch("application/pdf") }, enabled = !isLoading) {
                    Text("Subir HV (PDF)")
                }
            }

            Spacer(Modifier.height(18.dp))

            if (cvUrl != null) Text(text = "HV cargada", color = Color.Gray) else Text(text = "No hay HV cargada", color = Color.Gray)

            message?.let {
                Spacer(Modifier.height(12.dp))
                Text(text = it, color = Color.Red)
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    // Cerrar sesión y navegar a Login si tenemos navController
                    FirebaseAuth.getInstance().signOut()
                    navController?.let {
                        it.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F90D9))
            ) {
                Text("Cerrar sesión", color = Color.White)
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
