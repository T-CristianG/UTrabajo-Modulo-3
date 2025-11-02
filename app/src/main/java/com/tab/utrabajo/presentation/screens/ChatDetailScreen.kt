package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.tab.utrabajo.FirebaseRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ChatDetailScreen: muestra los mensajes de un chat y permite enviar mensajes.
 * - Listener en tiempo real para los mensajes.
 * - Mostrar mensajes optimistas (pending) inmediatamente.
 * - El listener del servidor sincroniza y elimina los pending cuando llegan.
 *
 * Nota: uso claves y tipos flexibles (Map<String, Any?>) para evitar errores
 * cuando algunos campos no existen en documentos Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(navController: NavHostController, chatId: String?) {
    val firebaseRepo = remember { FirebaseRepository.getInstance() }
    val currentUser = firebaseRepo.getCurrentUser()

    // Mensajes mostrados (mezcla de servidor + pending)
    var messages by remember { mutableStateOf<List<Map<String, Any?>>>(emptyList()) }
    // Mensajes optimistas locales (pendientes)
    val pendingMessages = remember { mutableStateListOf<Map<String, Any?>>() }

    var newMessage by remember { mutableStateOf("") }
    // chatInfo como Map flexible (acepta HashMap o Map)
    var chatInfo by remember { mutableStateOf<Map<String, Any?>?>(null) }

    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    var messagesListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var chatListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Registrar listeners cuando cambie chatId
    LaunchedEffect(chatId) {
        // remover listeners previos
        try { messagesListener?.remove() } catch (_: Exception) {}
        try { chatListener?.remove() } catch (_: Exception) {}

        if (chatId == null) return@LaunchedEffect

        val chatRef = db.collection("chats").document(chatId)

        // Listener para metadata del chat
        chatListener = chatRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: emptyMap<String, Any?>()
                val map = HashMap<String, Any?>()
                map.putAll(data)
                map["id"] = snapshot.id
                chatInfo = map
            }
        }

        // Listener para mensajes (orden asc)
        messagesListener = chatRef.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot == null) return@addSnapshotListener

                // Mensajes del servidor (convertir doc -> HashMap con id)
                val serverMsgs = snapshot.documents.mapNotNull { doc ->
                    val d = doc.data ?: return@mapNotNull null
                    val withId = HashMap<String, Any?>()
                    withId.putAll(d)
                    withId["id"] = doc.id
                    withId
                }

                // Merge servidor + pendientes que no estén ya en servidor
                val merged = serverMsgs.toMutableList()

                pendingMessages.forEach { pending ->
                    val pendingMsg = pending["message"]?.toString() ?: ""
                    val pendingSender = pending["senderId"]?.toString() ?: ""
                    val pendingId = pending["id"]?.toString() ?: ""

                    val existsOnServer = serverMsgs.any { srv ->
                        val srvId = srv["id"]?.toString() ?: ""
                        val srvMsg = srv["message"]?.toString() ?: ""
                        val srvSender = srv["senderId"]?.toString() ?: ""

                        if (srvId.isNotBlank() && pendingId.isNotBlank() && srvId == pendingId) {
                            true
                        } else if (srvMsg == pendingMsg && srvSender == pendingSender) {
                            val srvTs = srv["timestamp"] as? Timestamp
                            val pendTs = pending["timestamp"] as? Timestamp
                            if (srvTs != null && pendTs != null) {
                                kotlin.math.abs(srvTs.toDate().time - pendTs.toDate().time) <= 5000
                            } else true
                        } else false
                    }

                    if (!existsOnServer) merged.add(pending as HashMap<String, Any?>)
                }

                messages = merged

                // Scroll al final
                coroutineScope.launch {
                    if (messages.isNotEmpty()) scrollState.animateScrollToItem(messages.size - 1)
                }

                // Remover pendientes que ya llegaron
                val toRemove = mutableListOf<Map<String, Any?>>()
                pendingMessages.forEach { pending ->
                    val pendingMsg = pending["message"]?.toString() ?: ""
                    val pendingSender = pending["senderId"]?.toString() ?: ""
                    val pendingId = pending["id"]?.toString() ?: ""

                    val arrived = serverMsgs.any { srv ->
                        val srvId = srv["id"]?.toString() ?: ""
                        val srvMsg = srv["message"]?.toString() ?: ""
                        val srvSender = srv["senderId"]?.toString() ?: ""

                        if (srvId.isNotBlank() && pendingId.isNotBlank() && srvId == pendingId) {
                            true
                        } else if (srvMsg == pendingMsg && srvSender == pendingSender) {
                            val srvTs = srv["timestamp"] as? Timestamp
                            val pendTs = pending["timestamp"] as? Timestamp
                            if (srvTs != null && pendTs != null) {
                                kotlin.math.abs(srvTs.toDate().time - pendTs.toDate().time) <= 5000
                            } else true
                        } else false
                    }

                    if (arrived) toRemove.add(pending)
                }
                toRemove.forEach { pendingMessages.remove(it) }
            }
    }

    // Remover listeners al salir
    DisposableEffect(Unit) {
        onDispose {
            try { messagesListener?.remove() } catch (_: Exception) {}
            try { chatListener?.remove() } catch (_: Exception) {}
        }
    }

    // título: si chatInfo tiene jobTitle lo muestra, si no "Chat"
    val jobTitle = chatInfo?.get("jobTitle")?.toString() ?: "Chat"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = jobTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Lista de mensajes
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFE9F3F8)),
                state = scrollState,
                reverseLayout = false,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = messages, key = { it["id"].toString() }) { message ->
                    MessageBubble(
                        message = message,
                        isOwnMessage = message["senderId"] == currentUser?.uid
                    )
                }
            }

            // Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje") },
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (newMessage.isNotBlank() && chatId != null && currentUser != null) {
                            val textToSend = newMessage.trim()
                            val tempId = "local-${UUID.randomUUID()}"
                            val nowTs = Timestamp.now()

                            val pending = HashMap<String, Any?>().apply {
                                put("id", tempId)
                                put("message", textToSend)
                                put("senderId", currentUser.uid)
                                put("timestamp", nowTs)
                            }

                            // Mostrar optimista
                            pendingMessages.add(pending)
                            messages = messages + pending
                            newMessage = ""

                            coroutineScope.launch {
                                scrollState.animateScrollToItem(messages.size - 1)
                            }

                            // Enviar mensaje + actualizar metadata en batch
                            val chatRef = db.collection("chats").document(chatId)
                            val newMsgRef = chatRef.collection("messages").document()
                            val messageData = HashMap<String, Any?>().apply {
                                put("id", newMsgRef.id)
                                put("chatId", chatId)
                                put("senderId", currentUser.uid)
                                put("message", textToSend)
                                put("timestamp", Timestamp.now())
                            }

                            val batch = db.batch()
                            batch.set(newMsgRef, messageData)
                            batch.update(chatRef, mapOf(
                                "lastMessage" to textToSend,
                                "lastMessageTime" to Timestamp.now()
                            ))

                            batch.commit()
                                .addOnSuccessListener {
                                    // Listener actualizará vista y removerá pending
                                }
                                .addOnFailureListener { e ->
                                    // Mantener pending para que usuario vea mensaje local; logueamos.
                                    println("Error enviando mensaje: $e")
                                }
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF2B7BBF), CircleShape)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Map<String, Any?>,
    isOwnMessage: Boolean
) {
    val messageText = message["message"]?.toString() ?: ""

    // Normalizar timestamp
    val timestampAny = message["timestamp"]
    val timestamp: Timestamp? = when (timestampAny) {
        is Timestamp -> timestampAny
        is Long -> Timestamp((timestampAny / 1000), (((timestampAny % 1000) * 1000).toInt()))
        is Number -> Timestamp((timestampAny.toLong() / 1000), 0)
        else -> null
    }

    val timeText = timestamp?.let {
        val date = it.toDate()
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        format.format(date)
    } ?: ""

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isOwnMessage) Color(0xFF2B7BBF) else Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(text = messageText, color = if (isOwnMessage) Color.White else Color.Black, fontSize = 14.sp)
            }
            Text(text = timeText, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
        }
    }
}
