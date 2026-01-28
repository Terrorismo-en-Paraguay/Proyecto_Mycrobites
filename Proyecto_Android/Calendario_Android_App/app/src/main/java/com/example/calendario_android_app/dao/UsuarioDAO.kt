package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Usuario
import com.example.calendario_android_app.model.Cliente

interface UsuarioDAO {
    fun loguearUsuario(correo: String, contrasena: String): Usuario?
    fun existeCorreo(correo: String): Boolean
    fun crearUsuario(correo: String, contrasena: String, idCliente: Int): String?
    fun getUsuarioIdByEmail(correo: String): Int?
    suspend fun getUserByEmail(correo: String): Usuario?
}