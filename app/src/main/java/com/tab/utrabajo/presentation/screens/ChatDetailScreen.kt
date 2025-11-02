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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(navController: NavHostController, chatId: String?) {
    val firebaseRepo = remember { FirebaseRepository.getInstance() }
    val currentUser = firebaseRepo.getCurrentUser()

    // mensajes reales
    var messages by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    // mensajes locales pendientes de confirmación por el servidor
    val pendingMessages = remember { mutableStateListOf<Map<String, Any>>() }

    var newMessage by remember { mutableStateOf("") }
    var chatInfo by remember { mutableStateOf<Map<String, Any>?>(null) }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Carga info del chat
    var messagesListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    LaunchedEffect(chatId) {
        if (chatId != null) {
            // Cargar informacion del chat
            firebaseRepo.getChatsForUser(
                userId = currentUser?.uid ?: "",
                userType = "student",
                onSuccess = { chats ->
                    val currentChat = chats.find { it["id"] == chatId }
                    chatInfo = currentChat
                },
                onError = { /* ignore */ }
            )

            messagesListener = firebaseRepo.listenToMessages(
                chatId = chatId,
                onUpdate = { newMessages ->

                    val server = newMessages.toMutableList()

                    val toRemove = mutableListOf<Map<String, Any>>()
                    pendingMessages.forEach { pending ->
                        val pendingMsg = pending["message"]?.toString() ?: ""
                        val pendingSender = pending["senderId"]?.toString() ?: ""
                        val pendingId = pending["id"]?.toString() ?: ""
                        val match = server.any { srv ->
                            val srvId = srv["id"]?.toString() ?: ""
                            val srvMsg = srv["message"]?.toString() ?: ""
                            val srvSender = srv["senderId"]?.toString() ?: ""
                            if (srvId.isNotBlank() && srvId == pendingId) return@any true
                            if (srvMsg == pendingMsg && srvSender == pendingSender) {
                                val srvTs = srv["timestamp"]
                                val pendTs = pending["timestamp"]
                                try {
                                    val srvMillis = when (srvTs) {
                                        is Timestamp -> srvTs.toDate().time
                                        is Long -> srvTs
                                        is Number -> srvTs.toLong()
                                        else -> null
                                    }
                                    val pendMillis = when (pendTs) {
                                        is Timestamp -> pendTs.toDate().time
                                        is Long -> pendTs
                                        is Number -> pendTs.toLong()
                                        else -> null
                                    }
                                    if (srvMillis != null && pendMillis != null) {

                                        return@any kotlin.math.abs(srvMillis - pendMillis) <= 5000
                                    }
                                } catch (_: Exception) { }
                                return@any true
                            }
                            false
                        }

                        if (match) toRemove.add(pending)
                    }

                    toRemove.forEach { pendingMessages.remove(it) }

                    val merged = server.toMutableList()
                    pendingMessages.forEach { pending ->
                        val existsOnServer = server.any { srv ->
                            val srvMsg = srv["message"]?.toString() ?: ""
                            val srvSender = srv["senderId"]?.toString() ?: ""
                            srvMsg == pending["message"]?.toString() && srvSender == pending["senderId"]?.toString()
                        }
                        if (!existsOnServer) merged.add(pending)
                    }

                    messages = merged

                    coroutineScope.launch {
                        if (messages.isNotEmpty()) {
                            scrollState.animateScrollToItem(messages.size - 1)
                        }
                    }
                },
                onError = { /* ignore */ }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { messagesListener?.remove() } catch (_: Exception) {}
        }
    }

    val jobTitle = chatInfo?.get("jobTitle")?.toString() ?: "Chat"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = jobTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.chat_detail_back)
                        )
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

            // Input de mensaje
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
                    placeholder = { Text(stringResource(R.string.chat_detail_input_placeholder)) },
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (newMessage.isNotBlank() && chatId != null && currentUser != null) {
                            val textToSend = newMessage.trim()
                            val tempId = "local-${UUID.randomUUID()}"
                            val nowTs = Timestamp.now()
                            val pending = mapOf<String, Any>(
                                "id" to tempId,
                                "message" to textToSend,
                                "senderId" to currentUser.uid,
                                "timestamp" to nowTs
                            )
                            pendingMessages.add(pending)
                            messages = messages + pending

                            newMessage = ""

                            coroutineScope.launch {
                                scrollState.animateScrollToItem(messages.size - 1)
                            }

                            firebaseRepo.sendMessage(
                                chatId = chatId,
                                senderId = currentUser.uid,
                                messageText = textToSend,
                                onSuccess = {
                                },
                                onError = {

                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF2B7BBF), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Map<String, Any>,
    isOwnMessage: Boolean
) {
    val messageText = message["message"]?.toString() ?: ""
    val timestampAny = message["timestamp"]
    val timestamp = when (timestampAny) {
        is Timestamp -> timestampAny
        is com.google.firebase.Timestamp -> timestampAny
        is Long -> Timestamp(timestampAny / 1000, ((timestampAny % 1000) * 1000).toInt())
        is Number -> Timestamp(timestampAny.toLong() / 1000, 0)
        else -> null
    }

    val timeText = if (timestamp != null) {
        val date = timestamp.toDate()
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        format.format(date)
    } else {
        ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isOwnMessage) Color(0xFF2B7BBF) else Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = messageText,
                    color = if (isOwnMessage) Color.White else Color.Black,
                    fontSize = 14.sp
                )
            }
            Text(
                text = timeText,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
