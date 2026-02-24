package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Usuario
import com.example.calendario_android_app.model.Cliente

interface UsuarioDAO {
    fun loguearUsuario(correo: String, contrasena: String): Usuario?
    
    fun existeCorreo(correo: String): Boolean
    
    fun crearUsuario(correo: String, contrasena: String, idCliente: Int): String?
    
    fun getUsuarioIdByEmail(correo: String): Int?
    
    suspend fun getUserByEmail(correo: String): Usuario?
    
    suspend fun getUserInfo(idUsuario: Int): Pair<String, String>?
    
    suspend fun getUserById(idUsuario: Int): Usuario?
    
    suspend fun updatePassword(idUsuario: Int, newPassword: String): Boolean
}