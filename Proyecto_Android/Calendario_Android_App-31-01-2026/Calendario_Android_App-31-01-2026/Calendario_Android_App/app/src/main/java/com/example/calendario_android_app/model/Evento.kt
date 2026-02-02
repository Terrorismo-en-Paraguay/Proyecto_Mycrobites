package com.example.calendario_android_app.model

import java.time.LocalDateTime

/**
 * Modelo de datos que representa un evento en el calendario.
 * Los eventos pueden tener invitados, etiquetas y pertenecer a calendarios compartidos.
 * 
 * @property idEvento Identificador único del evento
 * @property idCreador ID del usuario que creó el evento
 * @property idEtiqueta ID de la etiqueta/categoría asociada (opcional)
 * @property titulo Título del evento
 * @property descripcion Descripción detallada del evento (opcional)
 * @property fechaInicio Fecha y hora de inicio del evento
 * @property fechaFin Fecha y hora de finalización del evento
 * @property ubicacion Lugar donde se realizará el evento (opcional)
 * @property estado Estado actual del evento (pendiente, en progreso, finalizado)
 * @property creadoEl Fecha y hora de creación del registro
 * @property actualizadoEl Fecha y hora de la última actualización
 * @property colorEtiqueta Color de la etiqueta para visualización (campo transitorio, no persistido)
 */
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

/**
 * Enumeración que define los posibles estados de un evento.
 */
enum class EstadoEvento(val valor: String) {
    /** Evento que ya ha concluido */
    FINALIZADO("finalizado"),
    
    /** Evento que está ocurriendo actualmente */
    EN_PROGRESO("en progreso"),
    
    /** Evento que aún no ha comenzado */
    PENDIENTE("pendiente");
    
    companion object {
        /**
         * Convierte un string del valor de la base de datos al enum correspondiente.
         * @param valor String con el estado del evento
         * @return Estado del evento, por defecto PENDIENTE si no se encuentra
         */
        fun fromString(valor: String): EstadoEvento {
            return entries.find { it.valor == valor } ?: PENDIENTE
        }
    }
}
