package com.tab.utrabajo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.tab.utrabajo.presentation.navigation.NavGraph
import com.tab.utrabajo.ui.theme.UTrabajoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DEBUG EXPLÍCITO DE FIREBASE
        Log.d("FIREBASE_DEBUG", "🔍 Iniciando MainActivity...")

        try {
            // Forzar inicialización de Firebase
            val firebaseApp = FirebaseApp.initializeApp(this)
            Log.d("FIREBASE_DEBUG", "✅ Firebase App: $firebaseApp")

            // Testear Auth
            val auth = FirebaseAuth.getInstance()
            Log.d("FIREBASE_DEBUG", "✅ Firebase Auth: $auth")

            // Testear Firestore
            val db = FirebaseFirestore.getInstance()
            Log.d("FIREBASE_DEBUG", "✅ Firebase Firestore: $db")

            Log.d("FIREBASE_DEBUG", "🎯 Firebase configurado correctamente")

        } catch (e: Exception) {
            Log.e("FIREBASE_DEBUG", "💥 ERROR Firebase: ${e.message}")
            e.printStackTrace()
        }

        setContent {
            UTrabajoTheme {
                NavGraph()
            }
        }
    }
}