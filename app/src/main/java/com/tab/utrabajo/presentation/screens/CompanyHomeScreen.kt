package com.tab.utrabajo.ui.company

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tab.utrabajo.R
import com.tab.utrabajo.FirebaseRepository

// Data class para ofertas de trabajo
data class JobOffer(
    val id: String = "",
    val title: String = "",
    val salary: String = ""
)

@Composable
fun CompanyHomeScreen(navController: NavHostController) {
    // Datos de ejemplo - se usarán sólo si no hay conexión inicial
    var jobOffers by remember { mutableStateOf<List<JobOffer>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingJob by remember { mutableStateOf<JobOffer?>(null) }

    val context = LocalContext.current
    val repo = FirebaseRepository.getInstance()

    // Recursos de bottom bar / labels / descripciones
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

    // Header
    val headerTitle = stringResource(R.string.company_header_title)
    val headerSubtitle = stringResource(R.string.company_header_subtitle)

    // Datos de ejemplo para fallback (se mantuvieron)
    val sampleOffers = listOf(
        JobOffer("1", "Desarrollador Android", "$3,000,000"),
        JobOffer("2", "Diseñador UX/UI", "$2,500,000"),
        JobOffer("3", "Analista de Datos", "$3,200,000")
    )

    // Escucha en tiempo real a Firestore para empleos activos
    DisposableEffect(Unit) {
        val listener = repo.listenToActiveJobOffers(onUpdate = { list ->
            // Mapear cada documento (mapa) a JobOffer
            val mapped = list.mapIndexed { index, item ->
                val idVal = item["id"] as? String
                val titleVal = item["titulo"] as? String ?: item["title"] as? String
                val salaryVal = item["salario"] as? String ?: item["salary"] as? String
                JobOffer(
                    id = idVal ?: (item["documentId"] as? String ?: index.toString()),
                    title = titleVal ?: "",
                    salary = salaryVal ?: ""
                )
            }
            jobOffers = mapped
        }, onError = { err ->
            // Si hay error, dejamos el sample para que no se vea vacío
            jobOffers = sampleOffers
            Toast.makeText(context, "Error cargando ofertas: $err", Toast.LENGTH_LONG).show()
        })

        onDispose {
            try {
                listener.remove()
            } catch (_: Exception) { /* ignore */ }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF2B7BBF)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar oferta")
            }
        },
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
                    onClick = { navController.navigate("profile") }
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
                    selected = true,
                    onClick = { }
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
                    selected = false,
                    onClick = { navController.navigate("empleo") }
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
            // Header - EXACTAMENTE IGUAL
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

            // Lista de ofertas de trabajo
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(jobOffers) { offer ->
                    JobOfferListItem(
                        offer = offer,
                        onEdit = { editingJob = offer },
                        onDelete = {
                            // Llamada a Firebase para eliminar
                            if (offer.id.isNotBlank()) {
                                repo.deleteJobOffer(offer.id, onSuccess = {
                                    Toast.makeText(context, "Oferta eliminada", Toast.LENGTH_SHORT).show()
                                }, onError = { err ->
                                    Toast.makeText(context, "Error al eliminar: $err", Toast.LENGTH_LONG).show()
                                })
                            } else {
                                Toast.makeText(context, "ID inválido para eliminar", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    // Diálogo para agregar/editar oferta
    if (showAddDialog || editingJob != null) {
        JobOfferDialog(
            jobOffer = editingJob,
            onDismiss = {
                showAddDialog = false
                editingJob = null
            },
            onSave = { jobOffer ->
                val currentUser = repo.getCurrentUser()
                val companyId = currentUser?.uid ?: ""
                if (editingJob != null) {
                    // Actualizar oferta existente en Firestore
                    if (jobOffer.id.isNotBlank()) {
                        repo.updateJobOffer(jobOffer.id, title = jobOffer.title, salary = jobOffer.salary, onSuccess = {
                            Toast.makeText(context, "Oferta actualizada", Toast.LENGTH_SHORT).show()
                        }, onError = { err ->
                            Toast.makeText(context, "Error al actualizar: $err", Toast.LENGTH_LONG).show()
                        })
                    } else {
                        Toast.makeText(context, "ID inválido para actualización", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Crear nueva oferta en Firestore
                    // usamos companyId (si no hay user, se crea con companyId vacío)
                    repo.createJobOffer(
                        companyId = companyId,
                        title = jobOffer.title,
                        description = "",
                        requirements = emptyList(),
                        salary = jobOffer.salary,
                        location = "",
                        onSuccess = {
                            Toast.makeText(context, "Oferta creada", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, "Error al crear oferta: $err", Toast.LENGTH_LONG).show()
                        }
                    )
                }

                // cerrar diálogo y limpiar
                showAddDialog = false
                editingJob = null
            }
        )
    }
}

@Composable
fun JobOfferListItem(
    offer: JobOffer,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // MANTENIENDO EXACTAMENTE EL MISMO DISEÑO que CandidateListItem
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar o icono - MANTENIDO IGUAL
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.LightGray, shape = androidx.compose.foundation.shape.CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Información de la oferta - MANTENIENDO LA MISMA ESTRUCTURA
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = offer.salary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Botones de acción
            Row {
                IconButton(
                    onClick = onEdit
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar oferta",
                        tint = Color(0xFF2B7BBF)
                    )
                }

                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar oferta",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
fun JobOfferDialog(
    jobOffer: JobOffer?,
    onDismiss: () -> Unit,
    onSave: (JobOffer) -> Unit
) {
    var title by remember { mutableStateOf(jobOffer?.title ?: "") }
    var salary by remember { mutableStateOf(jobOffer?.salary ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (jobOffer != null) "Editar Oferta" else "Nueva Oferta")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del puesto") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Salario") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedJobOffer = JobOffer(
                        id = jobOffer?.id ?: "",
                        title = title,
                        salary = salary
                    )
                    onSave(updatedJobOffer)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
