package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla Empleo — igual a la imagen: botón grande "Crear Empleo" arriba y
 * lista de tarjetas con logo a la izquierda y estado a la derecha.
 *
 * Copiar/pegar en tu proyecto. Usa Material3.
 */

@Composable
fun EmpleoScreen(navController: NavHostController) {
    // lista en memoria (mutable). Puedes reemplazar por Room/DataStore luego.
    val jobsState = remember { mutableStateListOf<Job>() }

    // precargar ejemplos (como en la imagen)
    LaunchedEffect(Unit) {
        if (jobsState.isEmpty()) {
            repeat(6) { idx ->
                val status = if (idx >= 4) JobStatus.FINISHED else JobStatus.IN_PROCESS
                jobsState.add(
                    Job(
                        id = idx.toLong(),
                        company = "Microsoft",
                        title = "Full Stack Developer",
                        date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                        status = status
                    )
                )
            }
        }
    }

    // dialog control para crear empleo con inputs
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = Color(0xFFE9F3F8)) // fondo celeste suave
        ) {
            // Top: back arrow + botón "Crear Empleo" CENTRADO (overlay box)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                // Back at start
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color(0xFF2B7BBF)
                    )
                }

                // Botón centrado absoluto
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                        .background(Color(0xFF2B7BBF), shape = RoundedCornerShape(24.dp))
                        .clickable { showCreateDialog = true }
                        .padding(horizontal = 22.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Crear Empleo",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Lista de empleos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(items = jobsState, key = { it.id }) { job ->
                    JobCard(job = job) { /* click */ }
                }
            }
        }
    }

    // Dialog para crear empleo (titulo, empresa, ubicacion opcional, descripcion opcional)
    if (showCreateDialog) {
        CreateJobDialog(
            onCancel = { showCreateDialog = false },
            onCreate = { created ->
                // insertar arriba y cerrar
                jobsState.add(0, created)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CreateJobDialog(onCancel: () -> Unit, onCreate: (Job) -> Unit) {
    val tfTitle = remember { mutableStateOf("") }
    val tfCompany = remember { mutableStateOf("") }
    val tfLocation = remember { mutableStateOf("") }
    val tfDesc = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = {
                val title = tfTitle.value.trim().ifEmpty { "Puesto" }
                val company = tfCompany.value.trim().ifEmpty { "Empresa" }
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                val job = Job(
                    id = System.currentTimeMillis(),
                    company = company,
                    title = title,
                    date = date,
                    status = JobStatus.IN_PROCESS
                )
                onCreate(job)
            }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancelar") }
        },
        title = { Text("Crear empleo") },
        text = {
            Column {
                OutlinedTextField(
                    value = tfTitle.value,
                    onValueChange = { tfTitle.value = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tfCompany.value,
                    onValueChange = { tfCompany.value = it },
                    label = { Text("Empresa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tfLocation.value,
                    onValueChange = { tfLocation.value = it },
                    label = { Text("Ubicación (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tfDesc.value,
                    onValueChange = { tfDesc.value = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

/* --- JobCard --- */
@Composable
private fun JobCard(job: Job, onClick: () -> Unit) {
    // se fija una altura uniforme para todos los cards
    val cardHeight = 95.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight) // <-- altura fija para uniformidad
            .clickable { onClick() },
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

            // Texto empresa + puesto (centrado verticalmente)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = job.company,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E88E5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = job.title,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Estado a la derecha - ancho fijo y centrado para alinear todos los iconos
            Column(
                modifier = Modifier
                    .width(120.dp) // ancho mayor para que "Proceso finalizado" quepa en 1 línea
                    .padding(start = 6.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (job.status == JobStatus.IN_PROCESS) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFEEEEEE), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = "En Proceso",
                            tint = Color(0xFF6D6D6D),
                            modifier = Modifier.size(20.dp) // uniform size
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "En Proceso",
                        fontSize = 11.sp,
                        color = Color(0xFF6D6D6D),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFF2F8FB), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Finalizado",
                            tint = Color(0xFF000000),
                            modifier = Modifier.size(20.dp) // uniform size
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Proceso finalizado",
                        fontSize = 11.sp,
                        color = Color(0xFF6D6D6D),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1, // <-- fuerza una sola línea
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/* --- Modelo --- */
data class Job(
    val id: Long,
    val company: String,
    val title: String,
    val date: String,
    val status: JobStatus
)

enum class JobStatus { IN_PROCESS, FINISHED }
