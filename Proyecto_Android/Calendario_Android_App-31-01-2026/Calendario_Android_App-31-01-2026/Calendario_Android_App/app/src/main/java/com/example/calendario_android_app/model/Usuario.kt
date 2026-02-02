package com.example.calendario_android_app.model

/**
 * Modelo de datos que representa un usuario del sistema.
 * Los usuarios tienen credenciales de acceso y están vinculados a un cliente.
 * 
 * @property id_usuario Identificador único del usuario
 * @property id_cliente ID del cliente asociado (referencia a tabla clientes)
 * @property correo Correo electrónico del usuario (usado para login)
 * @property password_hash Contraseña hasheada con SHA-1 y codificada en Base64
 * @property rol Rol del usuario en el sistema (ej: "usuario", "admin")
 */
data class Usuario(
    val id_usuario: Int,
    val id_cliente: String,
    val correo: String,
    val password_hash: String,
    val rol: String
)
