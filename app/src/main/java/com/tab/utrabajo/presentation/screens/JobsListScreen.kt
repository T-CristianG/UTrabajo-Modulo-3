package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.google.firebase.firestore.ListenerRegistration
import com.tab.utrabajo.R
import com.tab.utrabajo.FirebaseRepository
import com.tab.utrabajo.presentation.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState

@Composable
fun JobsListScreen(navController: NavHostController) {
    val firebaseRepo = remember { FirebaseRepository.getInstance() }

    var query by remember { mutableStateOf("") }
    var jobList by remember { mutableStateOf<List<JobItemData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Recursos
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

    val headerTitle = stringResource(R.string.jobslist_header_title)
    val headerSubtitle = stringResource(R.string.jobslist_header_subtitle)
    val searchPlaceholder = stringResource(R.string.jobslist_search_placeholder)
    val viewDetails = stringResource(R.string.jobslist_view_details)
    val viewDetailsDesc = stringResource(R.string.jobslist_view_details_desc)

    // Listener en tiempo real
    DisposableEffect(Unit) {
        val listener: ListenerRegistration = firebaseRepo.listenToActiveJobOffers(
            onUpdate = { jobs ->
                // Mapear los documentos a JobItemData - AHORA INCLUYE DESCRIPCION
                val mapped = jobs.map { doc ->
                    // Obtener título del puesto
                    val position = (
                            doc["titulo"]
                                ?: doc["title"]
                                ?: "Sin título"
                            ).toString()

                    // Obtener salario
                    val salary = (
                            doc["salario"]
                                ?: doc["salary"]
                                ?: "Salario no especificado"
                            ).toString()

                    // Obtener descripción (si existe)
                    val description = (
                            doc["descripcion"]
                                ?: doc["description"]
                                ?: ""
                            ).toString()

                    JobItemData(
                        position = position,
                        salary = salary,
                        description = description,
                        isChecked = false
                    )
                }
                jobList = mapped
                isLoading = false
            },
            onError = {
                jobList = emptyList()
                isLoading = false
            }
        )

        onDispose {
            try {
                listener.remove()
            } catch (_: Exception) { }
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
                            contentDescription = perfilDesc,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            perfilLabel,
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { navController.navigate(Screen.Profile.route) }
                )

                // Chat
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = chatDesc,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            chatLabel,
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { /* TODO: Navegar a Chat */ }
                )

                // Home
                // En JobsListScreen, reemplaza el onClick del Home:
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = homeDesc,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            homeLabel,
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = {
                        // Navegar al home del estudiante
                        navController.navigate(Screen.JobsList.route) {
                            popUpTo(Screen.JobsList.route) { inclusive = true }
                        }
                    }
                )
                // Notificaciones
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = notificationsDesc,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            notificationsLabel,
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { /* TODO: Navegar a Notificaciones */ }
                )

                // Empleo
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Work,
                            contentDescription = empleoDesc,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            empleoLabel,
                            fontSize = 12.sp
                        )
                    },
                    selected = true,
                    onClick = { }
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
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = headerSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Barra de búsqueda
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(searchPlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading o lista
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2B7BBF))
                }
            } else {
                // Filtrar por query (ahora también podrías filtrar por descripción si quieres)
                val filtered = jobList.filter {
                    it.position.contains(query, ignoreCase = true) ||
                            it.salary.contains(query, ignoreCase = true)
                }

                // Lista de trabajos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (filtered.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay empleos disponibles",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(items = filtered, key = { it.position + it.salary }) { job ->
                            JobCardForList(
                                position = job.position,
                                salary = job.salary,
                                description = job.description, // ahora pasamos la descripción
                                onClick = { /* mantenemos posible navegación si la necesitas */ },
                                viewDetails = viewDetails,
                                viewDetailsDesc = viewDetailsDesc
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JobCardForList(
    position: String,
    salary: String,
    description: String,
    onClick: () -> Unit,
    viewDetails: String,
    viewDetailsDesc: String
) {
    val cardHeight = 110.dp
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var animateToExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable {
                // Ejecutamos el callback externo y además mostramos el diálogo con la descripción
                onClick()
                // abrir dialogo (inicia animación)
                showDescriptionDialog = true
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo circular
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFFF2F8FB), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0xFF2B7BBF), shape = RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // SOLO CARGO Y SALARIO - SIN EMPRESA NI ID (seguimos igual)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = position, // CARGO en azul
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E88E5),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp)) // Más espacio entre cargo y salario
                Text(
                    text = salary, // SALARIO en verde
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E7D32),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Botón ver detalles
            Column(
                modifier = Modifier
                    .width(80.dp)
                    .padding(start = 6.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFEEEEEE), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = viewDetailsDesc,
                        tint = Color(0xFF6D6D6D),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = viewDetails,
                    fontSize = 11.sp,
                    color = Color(0xFF6D6D6D),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // Diálogo personalizado que "se expande" desde el tamaño de la tarjeta
    if (showDescriptionDialog) {
        // Control de animación: primero aparece el Dialog (con tamaño inicial),
        // luego activamos animateToExpanded para expandir altura/scale.
        LaunchedEffect(Unit) {
            animateToExpanded = false
            // pequeña pausa para que compose monte el Dialog y luego anime
            delay(10)
            animateToExpanded = true
        }

        // animaciones: altura y escala
        val targetHeight = if (animateToExpanded) 380.dp else cardHeight
        val animatedHeight by animateDpAsState(targetValue = targetHeight, animationSpec = tween(durationMillis = 380))
        val scaleTarget = if (animateToExpanded) 1f else 0.96f
        val animatedScale by animateFloatAsState(targetValue = scaleTarget, animationSpec = tween(durationMillis = 380))

        Dialog(onDismissRequest = {
            // al cerrar, invertimos la animación para que "encoga"
            coroutineScope.launch {
                animateToExpanded = false
                // dejamos tiempo a la animación de encoger
                delay(200)
                showDescriptionDialog = false
            }
        }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(animatedHeight)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    },
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 6.dp,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header del dialogo (título del puesto y un icono de cerrar arriba a la derecha)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = position,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E88E5),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            // cerrar con animación usando coroutineScope
                            coroutineScope.launch {
                                animateToExpanded = false
                                delay(180)
                                showDescriptionDialog = false
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Contenido: descripción (puede ser multilinea)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 6.dp)
                    ) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (description.isNotBlank()) description else "Descripción no disponible",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }

                    // Pie con salario y botón (manteniendo diseño simple)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = salary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )

                        Button(onClick = { /* si quieres acción adicional */ }) {
                            Text(text = "Aplicar")
                        }
                    }
                }
            }
        }
    }
}

data class JobItemData(
    val position: String,
    val salary: String,
    val description: String,
    val isChecked: Boolean
)
