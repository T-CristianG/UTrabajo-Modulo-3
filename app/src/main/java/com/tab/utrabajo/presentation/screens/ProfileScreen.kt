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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
    var profession by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(currentUser?.photoUrl?.toString()) }
    var cvUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isEditing by remember { mutableStateOf(false) } // Controla si estamos en modo edición global

    // Nuevos estados: edición por campo
    var isEditingPhone by remember { mutableStateOf(false) }
    var isEditingAddress by remember { mutableStateOf(false) }
    // Para revertir si cancela, guardamos temporales
    var phoneTemp by remember { mutableStateOf("") }
    var addressTemp by remember { mutableStateOf("") }

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

    // Función para guardar los datos del perfil (global)
    fun saveProfile() {
        val uid = currentUser?.uid
        if (uid == null) {
            message = "Usuario no autenticado"
            return
        }
        isLoading = true
        val updates = hashMapOf<String, Any>(
            "telefono" to phone,
            "direccion" to address,
            "profesion" to profession
        )

        db.collection("usuarios").document(uid)
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                isLoading = false
                message = "Perfil actualizado"
                isEditing = false // Salir del modo edición después de guardar
                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                isLoading = false
                message = "Error guardando: ${e.message}"
            }
    }

    // Guardar solo telefono
    fun savePhone() {
        val uid = currentUser?.uid
        if (uid == null) {
            message = "Usuario no autenticado"
            return
        }
        isLoading = true
        db.collection("usuarios").document(uid)
            .set(mapOf("telefono" to phone), com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                isLoading = false
                isEditingPhone = false
                message = "Teléfono actualizado"
                Toast.makeText(context, "Teléfono actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                isLoading = false
                message = "Error guardando teléfono: ${e.message}"
            }
    }

    // Guardar solo direccion
    fun saveAddress() {
        val uid = currentUser?.uid
        if (uid == null) {
            message = "Usuario no autenticado"
            return
        }
        isLoading = true
        db.collection("usuarios").document(uid)
            .set(mapOf("direccion" to address), com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                isLoading = false
                isEditingAddress = false
                message = "Dirección actualizada"
                Toast.makeText(context, "Dirección actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                isLoading = false
                message = "Error guardando dirección: ${e.message}"
            }
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
                    profession = (data["profesion"] as? String) ?: ""
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

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                modifier = Modifier.height(70.dp)
            ) {
                // Perfil
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Perfil",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Perfil",
                            fontSize = 12.sp
                        )
                    },
                    selected = true,
                    onClick = { }
                )

                // Chat
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "Chat",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Chat",
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { /* TODO: Navegar a Chat */ }
                )

                // Hoger (Home)
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Hoger",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Hoger",
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { navController?.popBackStack() }
                )

                // Notificaciones
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Notificaciones",
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { /* TODO: Navegar a Notificaciones */ }
                )

                // Empieo (Empleo)
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Work,
                            contentDescription = "Empieo",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Empieo",
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { navController?.navigate(Screen.JobsList.route) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // Header con título
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Mi Perfil",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Black
                )
            }

            // Contenido del perfil
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
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
                                Icons.Default.Person,
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
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "editar avatar",
                            tint = Color(0xFF2F90D9)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (displayName.isNotBlank()) displayName else "Nombre no definido",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                // Mostrar profesión si está disponible
                if (profession.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = profession,
                        color = Color(0xFF2F90D9),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = email,
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(24.dp))

                // Información de contacto
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Fila principal (icono + contenido)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF2F90D9)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    // Telefono (editable por campo)
                                    if (isEditingPhone) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                            OutlinedTextField(
                                                value = phone,
                                                onValueChange = { phone = it },
                                                modifier = Modifier.weight(1f),
                                                label = { Text("Teléfono") },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            IconButton(onClick = { savePhone() }, enabled = !isLoading) {
                                                Icon(Icons.Default.Check, contentDescription = "Guardar teléfono", tint = Color(0xFF2F90D9))
                                            }
                                            IconButton(onClick = {
                                                // cancelar: revertir valor y salir
                                                isEditingPhone = false
                                                // recargar temporal (no había guardado otra variable previa), no hacemos revert profundo
                                            }, enabled = !isLoading) {
                                                Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color.Gray)
                                            }
                                        }
                                    } else {
                                        Column {
                                            Text(
                                                if (phone.isNotBlank()) phone else "Sin teléfono",
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                if (address.isNotBlank()) address else "Sin dirección",
                                                color = Color.Gray,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Botones de editar:
                            // Si NO estamos editando por campo, mostramos lápiz global (como antes)
                            Column(horizontalAlignment = Alignment.End) {
                                if (!isEditingPhone && !isEditingAddress) {
                                    // Lápiz global (deja entrar al modo edición global)
                                    IconButton(
                                        onClick = { isEditing = true },
                                        enabled = !isLoading
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar teléfono y dirección",
                                            tint = Color(0xFF2F90D9)
                                        )
                                    }
                                }
                                // Lápiz individual para teléfono
                                IconButton(
                                    onClick = {
                                        // Si entra a editar teléfono, asegura que address no esté en modo edición por campo
                                        phoneTemp = phone
                                        isEditingPhone = true
                                        isEditingAddress = false
                                        isEditing = false
                                    },
                                    enabled = !isLoading
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar teléfono",
                                        tint = Color(0xFF2F90D9)
                                    )
                                }

                                // Lápiz individual para dirección
                                IconButton(
                                    onClick = {
                                        addressTemp = address
                                        isEditingAddress = true
                                        isEditingPhone = false
                                        isEditing = false
                                    },
                                    enabled = !isLoading
                                ) {
                                    Icon(
                                        Icons.Default.EditLocation,
                                        contentDescription = "Editar dirección",
                                        tint = Color(0xFF2F90D9)
                                    )
                                }
                            }
                        }

                        // Si el usuario está editando la dirección por campo, mostrar el campo aquí debajo
                        if (isEditingAddress) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Dirección") },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { saveAddress() }, enabled = !isLoading) {
                                    Icon(Icons.Default.Check, contentDescription = "Guardar dirección", tint = Color(0xFF2F90D9))
                                }
                                IconButton(onClick = {
                                    isEditingAddress = false
                                    address = addressTemp
                                }, enabled = !isLoading) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Campos de edición global - SOLO se muestran cuando isEditing es true
                if (isEditing) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Teléfono") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Dirección") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Botones de acción en modo edición
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                saveProfile()
                            },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F90D9))
                        ) {
                            Text("Guardar", color = Color.White)
                        }

                        Spacer(Modifier.width(12.dp))

                        Button(
                            onClick = {
                                isEditing = false // Cancelar edición sin guardar
                            },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Cancelar", color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                // Botón para subir HV (siempre visible)
                Button(
                    onClick = { cvPicker.launch("application/pdf") },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F90D9))
                ) {
                    Text("Subir Hoja de Vida", color = Color.White)
                }

                Spacer(Modifier.height(16.dp))

                // Estado del CV
                if (cvUrl != null) {
                    Text(text = "HV cargada correctamente", color = Color(0xFF2F90D9))
                } else {
                    Text(text = "No hay HV cargada", color = Color.Gray)
                }

                message?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(text = it, color = Color.Red)
                }

                Spacer(Modifier.weight(1f))

                // Botón de cerrar sesión
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F90D9))
                ) {
                    Text("Cerrar sesión", color = Color.White)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}