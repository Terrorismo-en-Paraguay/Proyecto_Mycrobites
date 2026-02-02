package com.example.calendario_android_app.model

/**
 * Modelo de datos que representa un miembro de un grupo/calendario compartido.
 * Se utiliza principalmente en el proceso de creación de grupos y visualización de miembros.
 * 
 * @property email Correo electrónico del miembro
 * @property userId ID del usuario en el sistema (null si aún no se ha verificado su existencia)
 * @property userName Nombre completo del usuario obtenido de la tabla Cliente (opcional)
 * @property role Rol del miembro en el grupo: "miembro", "editor" o "admin"
 */
data class GroupMember(
    val email: String,
    val userId: Int? = null,
    val userName: String? = null,
    val role: String = "miembro"
)
