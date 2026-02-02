package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Etiqueta

/**
 * Interfaz que define las operaciones de acceso a datos para la entidad Etiqueta.
 * Las etiquetas permiten categorizar eventos y pueden ser personales o compartidas.
 */
interface EtiquetaDAO {
    /**
     * Inserta una nueva etiqueta en la base de datos.
     * @param etiqueta Datos de la etiqueta a crear
     * @param idUsuario ID del usuario propietario
     * @param idGrupo ID del grupo si es compartida (null si es personal)
     * @return true si se creó correctamente, false en caso contrario
     */
    suspend fun insertEtiqueta(etiqueta: Etiqueta, idUsuario: Int, idGrupo: Int?): Boolean
    
    /**
     * Obtiene todas las etiquetas visibles para un usuario.
     * Incluye etiquetas personales y las de grupos a los que pertenece.
     * @param idUsuario ID del usuario
     * @return Lista de etiquetas disponibles
     */
    suspend fun getEtiquetasByUsuario(idUsuario: Int): List<Etiqueta>
    
    /**
     * Actualiza el grupo asociado a una etiqueta.
     * @param idEtiqueta ID de la etiqueta
     * @param idGrupo ID del nuevo grupo
     * @return true si se actualizó correctamente
     */
    suspend fun updateEtiquetaGrupo(idEtiqueta: Int, idGrupo: Int): Boolean
    
    /**
     * Obtiene el ID del grupo asociado a una etiqueta.
     * @param idEtiqueta ID de la etiqueta
     * @return ID del grupo o null si es una etiqueta personal
     */
    suspend fun getGrupoIdByEtiqueta(idEtiqueta: Int): Int?
}
