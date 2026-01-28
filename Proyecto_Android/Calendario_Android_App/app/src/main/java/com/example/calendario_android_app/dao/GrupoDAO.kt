package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Grupo

interface GrupoDAO {
    suspend fun createGrupo(nombre: String, descripcion: String?): Int
    suspend fun linkUsuarioToGrupo(idGrupo: Int, idUsuario: Int, rol: String)
    suspend fun getGruposByUsuario(idUsuario: Int): List<Grupo>
    suspend fun createDefaultPersonalGroup(idUsuario: Int): Boolean
    suspend fun getIntegrantesGrupo(idGrupo: Int): List<com.example.calendario_android_app.model.GroupMember>
    suspend fun isAdmin(idUsuario: Int, idGrupo: Int): Boolean
    suspend fun getGrupoById(idGrupo: Int): Grupo?
}
