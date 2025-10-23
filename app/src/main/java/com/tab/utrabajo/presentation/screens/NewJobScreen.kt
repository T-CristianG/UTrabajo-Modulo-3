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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tab.utrabajo.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EmpleoScreen(navController: NavHostController) {
    val jobsState = remember { mutableStateListOf<Job>() }

    // Obtener recursos (antes de LaunchedEffect)
    val sampleCompany = stringResource(R.string.empleo_sample_company)
    val sampleTitle = stringResource(R.string.empleo_sample_title)
    val datePattern = stringResource(R.string.empleo_date_pattern)
    val createJobLabel = stringResource(R.string.empleo_create_job)
    val backDesc = stringResource(R.string.empleo_back)

    LaunchedEffect(Unit) {
        if (jobsState.isEmpty()) {
            repeat(6) { idx ->
                val status = if (idx >= 4) JobStatus.FINISHED else JobStatus.IN_PROCESS
                jobsState.add(
                    Job(
                        id = idx.toLong(),
                        company = sampleCompany,
                        title = sampleTitle,
                        date = SimpleDateFormat(datePattern, Locale.getDefault()).format(Date()),
                        status = status
                    )
                )
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = Color(0xFFE9F3F8))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = backDesc,
                        tint = Color(0xFF2B7BBF)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                        .background(Color(0xFF2B7BBF), shape = RoundedCornerShape(24.dp))
                        .clickable {
                            navController.navigate("create_job")
                        }
                        .padding(horizontal = 22.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = createJobLabel,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(items = jobsState, key = { it.id }) { job ->
                    JobCard(job = job) { /* manejar click si hace falta */ }
                }
            }
        }
    }
}

@Composable
private fun JobCard(job: Job, onClick: () -> Unit) {
    val cardHeight = 95.dp
    val statusInProcess = stringResource(R.string.empleo_status_in_process)
    val statusFinished = stringResource(R.string.empleo_status_finished)
    val jobIconDesc = stringResource(R.string.empleo_icon_job)
    val finishedIconDesc = stringResource(R.string.empleo_icon_finished)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
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
                        contentDescription = jobIconDesc,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

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

            Column(
                modifier = Modifier
                    .width(120.dp)
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
                            contentDescription = statusInProcess,
                            tint = Color(0xFF6D6D6D),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = statusInProcess,
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
                            contentDescription = finishedIconDesc,
                            tint = Color(0xFF000000),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = statusFinished,
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
    }
}

data class Job(
    val id: Long,
    val company: String,
    val title: String,
    val date: String,
    val status: JobStatus
)

enum class JobStatus { IN_PROCESS, FINISHED }
