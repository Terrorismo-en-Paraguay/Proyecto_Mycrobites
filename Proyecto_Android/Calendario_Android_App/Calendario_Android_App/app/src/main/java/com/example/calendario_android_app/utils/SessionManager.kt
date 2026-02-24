package com.example.calendario_android_app.utils

import com.example.calendario_android_app.model.Usuario

object SessionManager {
    /** Usuario actualmente autenticado */
    var currentUser: Usuario? = null
    
    /** Nombre completo del cliente asociado al usuario */
    var currentClientName: String? = null

    fun clearSession() {
        currentUser = null
        currentClientName = null
    }
}
