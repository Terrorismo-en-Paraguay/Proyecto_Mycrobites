package com.example.calendario_android_app.util

import java.util.Calendar

/**
 * Helper class to work with Calendar dates
 */
object CalendarHelper {
    
    fun getCurrentYearMonth(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        return Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }
    
    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    
    fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Convert Sunday=1 to Monday=1 format
        return if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
    }
    
    fun formatMonthYear(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        val monthNames = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return "${monthNames[month - 1]} $year"
    }
    
    fun formatDateHeader(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day)
        val dayNames = arrayOf(
            "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO"
        )
        val monthNames = arrayOf(
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
        )
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayName = if (dayOfWeek == Calendar.SUNDAY) dayNames[6] else dayNames[dayOfWeek - 2]
        return "$dayName, $day ${monthNames[month - 1]}"
    }
    
    fun addMonths(year: Int, month: Int, offset: Int): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        calendar.add(Calendar.MONTH, offset)
        return Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }
    
    fun getToday(): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance()
        return Triple(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
}
