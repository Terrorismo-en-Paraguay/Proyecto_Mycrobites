package com.example.calendario_android_app.model

import java.time.LocalDateTime

data class Grupo(
    val idGrupo: Int = 0,
    val nombre: String,
    val descripcion: String? = null,
    val creadoEl: LocalDateTime? = null,
    val actualizadoEl: LocalDateTime? = null
)
