package com.example.calendario_android_app.model

/**
 * Modelo de datos que representa una etiqueta o categoría para eventos.
 * Las etiquetas pueden ser personales (sin grupo) o compartidas (asociadas a un grupo).
 * 
 * @property idEtiqueta Identificador único de la etiqueta
 * @property nombre Nombre descriptivo de la etiqueta
 * @property color Color en formato hexadecimal (ej: "#FF5733") para identificación visual
 * @property idGrupo ID del grupo al que pertenece la etiqueta (null si es personal)
 */
data class Etiqueta(
    val idEtiqueta: Int = 0,
    val nombre: String,
    val color: String,
    val idGrupo: Int? = null
)
