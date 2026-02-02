package com.example.calendario_android_app.util

import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Utilidades para el manejo de Hash y seguridad.
 * Esta clase se encarga de transformar las contraseñas planas en versiones seguras (hashes).
 */
object HashUtils {

    /**
     * Genera un hash de la contraseña utilizando el algoritmo SHA-256 y lo codifica en Base64.
     * 
     * @param password La contraseña en texto plano que se desea hashear.
     * @return Una cadena de texto (String) que representa la contraseña hasheada y codificada.
     * 
     * Funcionamiento:
     * 1. Se obtiene una instancia del algoritmo SHA-256.
     * 2. Se convierte la contraseña a bytes y se procesa.
     * 3. El resultado del hash (bytes) se convierte a una cadena Base64 legible para guardarla en la BD.
     */
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
