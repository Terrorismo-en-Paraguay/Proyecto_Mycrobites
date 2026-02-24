package com.example.calendario_android_app.models

import java.time.LocalDate

data class DayUI(
    val date: LocalDate,
    val isCurrentMonth: Boolean = true,
    val hasEvent: Boolean = false,
    val isSelected: Boolean = false
)
