package com.example.calendario_android_app.model

import java.time.LocalDateTime

data class Evento(
    val idEvento: Int = 0,
    val idCreador: Int?,
    val idEtiqueta: Int? = null,
    val titulo: String,
    val descripcion: String?,
    val fechaInicio: LocalDateTime,
    val fechaFin: LocalDateTime,
    val ubicacion: String?,
    val estado: EstadoEvento,
    val creadoEl: LocalDateTime? = null,
    val actualizadoEl: LocalDateTime? = null,
    val colorEtiqueta: String? = null
)

enum class EstadoEvento(val valor: String) {
    /** Evento que ya ha concluido */
    FINALIZADO("finalizado"),
    
    /** Evento que está ocurriendo actualmente */
    EN_PROGRESO("en progreso"),
    
    /** Evento que aún no ha comenzado */
    PENDIENTE("pendiente");
    
    companion object {
        fun fromString(valor: String): EstadoEvento {
            return entries.find { it.valor == valor } ?: PENDIENTE
        }
    }
}
