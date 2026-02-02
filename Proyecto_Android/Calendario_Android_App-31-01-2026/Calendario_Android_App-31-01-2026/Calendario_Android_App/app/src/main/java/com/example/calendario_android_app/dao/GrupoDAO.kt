package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Grupo

/**
 * Interfaz que define las operaciones de acceso a datos para la entidad Grupo.
 * Los grupos representan calendarios compartidos entre múltiples usuarios.
 */
interface GrupoDAO {
    /**
     * Crea un nuevo grupo/calendario compartido.
     * @param nombre Nombre del grupo
     * @param descripcion Descripción opcional del grupo
     * @return ID del grupo creado
     */
    suspend fun createGrupo(nombre: String, descripcion: String?): Int
    
    /**
     * Vincula un usuario a un grupo con un rol específico.
     * @param idGrupo ID del grupo
     * @param idUsuario ID del usuario
     * @param rol Rol del usuario: "miembro", "editor" o "admin"
     */
    suspend fun linkUsuarioToGrupo(idGrupo: Int, idUsuario: Int, rol: String)
    
    /**
     * Obtiene todos los grupos a los que pertenece un usuario.
     * @param idUsuario ID del usuario
     * @return Lista de grupos del usuario
     */
    suspend fun getGruposByUsuario(idUsuario: Int): List<Grupo>
    
    /**
     * Crea un grupo personal por defecto para un nuevo usuario.
     * @param idUsuario ID del usuario
     * @return true si se creó correctamente
     */
    suspend fun createDefaultPersonalGroup(idUsuario: Int): Boolean
    
    /**
     * Obtiene todos los miembros de un grupo.
     * @param idGrupo ID del grupo
     * @return Lista de miembros con sus roles
     */
    suspend fun getIntegrantesGrupo(idGrupo: Int): List<com.example.calendario_android_app.model.GroupMember>
    
    /**
     * Verifica si un usuario es administrador de un grupo.
     * @param idUsuario ID del usuario
     * @param idGrupo ID del grupo
     * @return true si el usuario es admin del grupo
     */
    suspend fun isAdmin(idUsuario: Int, idGrupo: Int): Boolean
    
    /**
     * Obtiene un grupo por su ID.
     * @param idGrupo ID del grupo
     * @return Grupo encontrado o null si no existe
     */
    suspend fun getGrupoById(idGrupo: Int): Grupo?
}
