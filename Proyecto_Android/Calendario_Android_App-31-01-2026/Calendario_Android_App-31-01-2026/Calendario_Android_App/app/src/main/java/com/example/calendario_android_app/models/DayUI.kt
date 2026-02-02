package com.example.calendario_android_app.models

import java.time.LocalDate

/**
 * Modelo de datos para la representación visual de un día en el calendario.
 * Se utiliza en los adaptadores para renderizar la vista mensual del calendario.
 * 
 * @property date Fecha del día
 * @property isCurrentMonth Indica si el día pertenece al mes actualmente visualizado
 * @property hasEvent Indica si hay eventos programados en este día
 * @property isSelected Indica si el día está seleccionado por el usuario
 */
data class DayUI(
    val date: LocalDate,
    val isCurrentMonth: Boolean = true,
    val hasEvent: Boolean = false,
    val isSelected: Boolean = false
)
