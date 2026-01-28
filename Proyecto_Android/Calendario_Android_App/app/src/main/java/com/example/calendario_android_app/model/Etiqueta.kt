package com.example.calendario_android_app.model

data class Etiqueta(
    val idEtiqueta: Int = 0,
    val nombre: String,
    val color: String, // Hex color string
    val idGrupo: Int? = null
)
