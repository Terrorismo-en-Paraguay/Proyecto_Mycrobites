package com.example.calendario_android_app.utils

import com.example.calendario_android_app.model.Usuario

object SessionManager {
    var currentUser: Usuario? = null
    var currentClientName: String? = null

    fun clearSession() {
        currentUser = null
        currentClientName = null
    }
}
