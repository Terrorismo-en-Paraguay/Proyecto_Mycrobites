package com.example.calendario_android_app.utils

import com.example.calendario_android_app.model.Usuario

/**
 * Singleton que gestiona la sesión del usuario actual en memoria.
 * Almacena información del usuario autenticado durante la ejecución de la app.
 */
object SessionManager {
    /** Usuario actualmente autenticado */
    var currentUser: Usuario? = null
    
    /** Nombre completo del cliente asociado al usuario */
    var currentClientName: String? = null

    /**
     * Limpia la sesión actual (logout).
     * Elimina toda la información del usuario de la memoria.
     */
    fun clearSession() {
        currentUser = null
        currentClientName = null
    }
}
