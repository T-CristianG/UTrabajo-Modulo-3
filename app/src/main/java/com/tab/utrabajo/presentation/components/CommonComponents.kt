package com.tab.utrabajo.presentation.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CompanyAvatarField(enabled: Boolean = true) {
    var selectedResId by remember { mutableStateOf<Int?>(null) }
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFDCEAF6), shape = CircleShape)
                .clickable(enabled = enabled) {
                    selectedResId = if (selectedResId == android.R.drawable.ic_menu_camera)
                        android.R.drawable.ic_menu_gallery
                    else
                        android.R.drawable.ic_menu_camera
                },
            contentAlignment = Alignment.Center
        ) {
            if (selectedResId != null) {
                Image(
                    painter = painterResource(id = selectedResId!!),
                    contentDescription = "avatar seleccionado",
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = "avatar",
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

@Composable
fun SingleDocumentUploadField(label: String, selectedFileUri: Uri?, onFileSelected: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFFEAF4FB), shape = RoundedCornerShape(8.dp))
                .clickable { onFileSelected() },
            contentAlignment = Alignment.Center
        ) {
            if (selectedFileUri != null) {
                Text(text = "Documento seleccionado", color = Color(0xFF2F90D9), fontWeight = FontWeight.Medium)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painter = painterResource(id = android.R.drawable.ic_menu_upload), contentDescription = "subir documento")
                    Spacer(Modifier.height(6.dp))
                    Text(label, color = Color(0xFF2F90D9))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
