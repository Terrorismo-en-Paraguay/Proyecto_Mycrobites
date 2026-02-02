package com.example.calendario_android_app.model

import java.time.LocalDateTime

/**
 * Modelo de datos que representa un grupo o calendario compartido.
 * Los grupos permiten que múltiples usuarios colaboren en un mismo calendario.
 * 
 * @property idGrupo Identificador único del grupo
 * @property nombre Nombre del grupo/calendario compartido
 * @property descripcion Descripción opcional del propósito del grupo
 * @property creadoEl Fecha y hora de creación del grupo
 * @property actualizadoEl Fecha y hora de la última actualización
 */
data class Grupo(
    val idGrupo: Int = 0,
    val nombre: String,
    val descripcion: String? = null,
    val creadoEl: LocalDateTime? = null,
    val actualizadoEl: LocalDateTime? = null
)
