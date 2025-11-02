package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.tab.utrabajo.R
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun ChatListScreen(navController: NavHostController) {
    val firebaseRepo = remember { FirebaseRepository.getInstance() }
    val currentUser = firebaseRepo.getCurrentUser()
    var chats by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val perfilLabel = stringResource(R.string.bottom_perfil_label)
    val perfilDesc = stringResource(R.string.bottom_perfil_desc)
    val chatLabel = stringResource(R.string.bottom_chat_label)
    val chatDesc = stringResource(R.string.bottom_chat_desc)
    val homeLabel = stringResource(R.string.bottom_home_label)
    val homeDesc = stringResource(R.string.bottom_home_desc)
    val notificationsLabel = stringResource(R.string.bottom_notifications_label)
    val notificationsDesc = stringResource(R.string.bottom_notifications_desc)
    val empleoLabel = stringResource(R.string.bottom_empleo_label)
    val empleoDesc = stringResource(R.string.bottom_empleo_desc)

    val db = FirebaseFirestore.getInstance()
    var listenerStudent by remember { mutableStateOf<ListenerRegistration?>(null) }
    var listenerCompany by remember { mutableStateOf<ListenerRegistration?>(null) }
    var listenerParticipants by remember { mutableStateOf<ListenerRegistration?>(null) }


    fun timeMillisOf(chat: Map<String, Any>): Long {
        val t = (chat["lastMessageTime"] as? Timestamp) ?: (chat["createdAt"] as? Timestamp)
        return t?.toDate()?.time ?: 0L
    }

    fun normalizeAndSort(list: List<Map<String, Any>>): List<Map<String, Any>> {
        val distinct = list.distinctBy { it["id"]?.toString() ?: UUID.randomUUID().toString() }
        return distinct.sortedByDescending { timeMillisOf(it) }
    }


    var studentList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var companyList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var participantsList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    fun updateCombined() {

        val merged = normalizeAndSort(studentList + companyList + participantsList)
        chats = merged
        isLoading = false
    }


    LaunchedEffect(currentUser?.uid) {
        isLoading = true


        listenerStudent?.remove(); listenerStudent = null
        listenerCompany?.remove(); listenerCompany = null
        listenerParticipants?.remove(); listenerParticipants = null

        val uid = currentUser?.uid
        if (uid == null) {
            chats = emptyList()
            isLoading = false
            return@LaunchedEffect
        }

        //  chat donde studentId == uid
        listenerStudent = db.collection("chats")
            .whereEqualTo("studentId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {

                    println("ChatListScreen: error student query -> $error")
                } else if (snapshot != null) {
                    studentList = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        val withId = HashMap<String, Any>(data)
                        withId["id"] = doc.id
                        withId
                    }
                    updateCombined()
                }
            }

        // chat donde companyId == uid
        listenerCompany = db.collection("chats")
            .whereEqualTo("companyId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("ChatListScreen: error company query -> $error")
                } else if (snapshot != null) {
                    companyList = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        val withId = HashMap<String, Any>(data)
                        withId["id"] = doc.id
                        withId
                    }
                    updateCombined()
                }
            }

        // 3) chat donde participants array contains uid
        listenerParticipants = db.collection("chats")
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("ChatListScreen: error participants query -> $error")
                } else if (snapshot != null) {
                    participantsList = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        val withId = HashMap<String, Any>(data)
                        withId["id"] = doc.id
                        withId
                    }
                    updateCombined()
                }
            }


    }

    DisposableEffect(Unit) {
        onDispose {
            listenerStudent?.remove()
            listenerCompany?.remove()
            listenerParticipants?.remove()
        }
    }


    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                modifier = Modifier.height(70.dp)
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = perfilDesc,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(perfilLabel, fontSize = 12.sp) },
                    selected = false,
                    onClick = { navController.navigate(Screen.Profile.route) }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = chatDesc,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(chatLabel, fontSize = 12.sp) },
                    selected = true,
                    onClick = { /* ya estamos aquí */ }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = homeDesc,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(homeLabel, fontSize = 12.sp) },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.JobsList.route) {
                            popUpTo(Screen.JobsList.route) { inclusive = true }
                        }
                    }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = notificationsDesc,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(notificationsLabel, fontSize = 12.sp) },
                    selected = false,
                    onClick = { /* TODO: Notificaciones */ }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Work,
                            contentDescription = empleoDesc,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(empleoLabel, fontSize = 12.sp) },
                    selected = false,
                    onClick = { navController.navigate("applications_screen") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = Color(0xFFE9F3F8))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.chat_list_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.chat_list_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2B7BBF))
                    }
                }
                chats.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = stringResource(R.string.chat_list_empty),
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.chat_list_empty),
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Text(
                                text = stringResource(R.string.chat_list_empty_subtitle),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(items = chats, key = { it["id"].toString() }) { chat ->
                            ChatListItem(
                                chat = chat,
                                onClick = {
                                    val chatId = chat["id"].toString()
                                    navController.navigate("${Screen.ChatDetail.route}/$chatId")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: Map<String, Any>,
    onClick: () -> Unit
) {
    val jobTitle = chat["jobTitle"]?.toString() ?: "Empleo"
    val lastMessage = chat["lastMessage"]?.toString() ?: "No hay mensajes"

    val ts = (chat["lastMessageTime"] as? Timestamp) ?: (chat["createdAt"] as? Timestamp)
    val timeText = ts?.toDate()?.let {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        fmt.format(it)
    } ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFF2B7BBF), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = "Empresa",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = jobTitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastMessage,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            Text(
                text = timeText,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
