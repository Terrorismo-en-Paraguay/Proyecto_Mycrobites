package com.example.calendario_android_app.util

import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object HashUtils {

    fun hashPassword(password: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(password.toByteArray())
            val resume = md.digest()

            // Usamos la clase Base64 de Android para mayor compatibilidad con dispositivos antiguos.
            // NO_WRAP evita que se inserten saltos de línea innecesarios en la cadena final.
            Base64.encodeToString(resume, Base64.NO_WRAP)
        } catch (e: NoSuchAlgorithmException) {
            // SHA-1 es un algoritmo estándar que siempre debería estar disponible.
            e.printStackTrace()
            // En caso de error crítico, devolvemos cadena vacía.
            ""
        }
    }
}
