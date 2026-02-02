package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Usuario
import com.example.calendario_android_app.model.Cliente

/**
 * Interfaz que define las operaciones de acceso a datos para la entidad Usuario.
 * Gestiona autenticación, registro y gestión de cuentas de usuario.
 */
interface UsuarioDAO {
    /**
     * Autentica un usuario con correo y contraseña.
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña en texto plano (se hasheará internamente)
     * @return Usuario autenticado o null si las credenciales son incorrectas
     */
    fun loguearUsuario(correo: String, contrasena: String): Usuario?
    
    /**
     * Verifica si un correo electrónico ya está registrado.
     * @param correo Correo a verificar
     * @return true si el correo ya existe
     */
    fun existeCorreo(correo: String): Boolean
    
    /**
     * Crea un nuevo usuario en el sistema.
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña en texto plano (se hasheará)
     * @param idCliente ID del cliente asociado
     * @return ID del usuario creado o null si falla
     */
    fun crearUsuario(correo: String, contrasena: String, idCliente: Int): String?
    
    /**
     * Obtiene el ID de un usuario por su correo electrónico.
     * @param correo Correo del usuario
     * @return ID del usuario o null si no existe
     */
    fun getUsuarioIdByEmail(correo: String): Int?
    
    /**
     * Obtiene un usuario completo por su correo electrónico.
     * @param correo Correo del usuario
     * @return Usuario encontrado o null
     */
    suspend fun getUserByEmail(correo: String): Usuario?
    
    /**
     * Obtiene información básica de un usuario para notificaciones.
     * @param idUsuario ID del usuario
     * @return Par (email, nombre) o null si no existe
     */
    suspend fun getUserInfo(idUsuario: Int): Pair<String, String>?
    
    /**
     * Obtiene un usuario por su ID.
     * @param idUsuario ID del usuario
     * @return Usuario encontrado o null
     */
    suspend fun getUserById(idUsuario: Int): Usuario?
    
    /**
     * Actualiza la contraseña de un usuario.
     * @param idUsuario ID del usuario
     * @param newPassword Nueva contraseña en texto plano (se hasheará)
     * @return true si se actualizó correctamente
     */
    suspend fun updatePassword(idUsuario: Int, newPassword: String): Boolean
}