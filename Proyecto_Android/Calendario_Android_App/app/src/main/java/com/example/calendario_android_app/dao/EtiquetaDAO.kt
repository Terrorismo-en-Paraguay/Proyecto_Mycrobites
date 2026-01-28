package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Etiqueta

interface EtiquetaDAO {
    suspend fun insertEtiqueta(etiqueta: Etiqueta, idUsuario: Int, idGrupo: Int?): Boolean
    suspend fun getEtiquetasByUsuario(idUsuario: Int): List<Etiqueta>
    suspend fun updateEtiquetaGrupo(idEtiqueta: Int, idGrupo: Int): Boolean
    suspend fun getGrupoIdByEtiqueta(idEtiqueta: Int): Int?
}
