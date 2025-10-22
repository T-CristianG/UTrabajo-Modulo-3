package com.tab.utrabajo

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

object FirebaseConfig {

    private const val TAG = "FirebaseConfig"

    /**
     * 🔹 Inicializa Firebase en toda la app
     * Llama a esta función una sola vez, normalmente en onCreate() de MainActivity
     */
    fun init(context: Context): Boolean {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
                Log.d(TAG, "Firebase initialized successfully")
            } else {
                Log.d(TAG, "Firebase already initialized")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
            false
        }

    }

}