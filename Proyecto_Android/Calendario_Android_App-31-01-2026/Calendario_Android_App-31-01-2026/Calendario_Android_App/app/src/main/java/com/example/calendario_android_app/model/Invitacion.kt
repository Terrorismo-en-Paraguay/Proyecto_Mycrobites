package com.example.calendario_android_app.model

/**
 * Modelo de datos que representa una invitación a un evento por correo electrónico.
 * Permite invitar a personas que no están registradas en el sistema.
 * 
 * @property id Identificador único de la invitación
 * @property idEvento ID del evento al que se invita
 * @property email Correo electrónico del invitado
 */
data class Invitacion(
    val id: Int = 0,
    val idEvento: Int,
    val email: String
)
