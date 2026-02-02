package com.example.calendario_android_app.util

import java.util.Calendar

/**
 * Clase de utilidad para trabajar con fechas del calendario.
 * Proporciona métodos para formateo, cálculos y conversiones de fechas.
 */
object CalendarHelper {
    
    /**
     * Obtiene el año y mes actuales.
     * @return Par (año, mes) donde mes está en rango 1-12
     */
    fun getCurrentYearMonth(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        return Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }
    
    /**
     * Calcula el número de días en un mes específico.
     * @param year Año
     * @param month Mes (1-12)
     * @return Número de días en el mes (28-31)
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    
    /**
     * Obtiene el día de la semana del primer día del mes.
     * @param year Año
     * @param month Mes (1-12)
     * @return Día de la semana (1=Lunes, 7=Domingo)
     */
    fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Convertir formato Domingo=1 a Lunes=1
        return if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
    }
    
    /**
     * Formatea un mes y año como texto en español.
     * @return String en formato "Enero 2024"
     */
    fun formatMonthYear(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        val monthNames = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return "${monthNames[month - 1]} $year"
    }
    
    /**
     * Formatea una fecha completa con día de la semana en español.
     * @return String en formato "LUNES, 15 enero"
     */
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
    
    /**
     * Añade o resta meses a una fecha.
     * @param offset Número de meses a añadir (negativo para restar)
     * @return Par (año, mes) resultante
     */
    fun addMonths(year: Int, month: Int, offset: Int): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        calendar.add(Calendar.MONTH, offset)
        return Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }
    
    /**
     * Obtiene la fecha actual del sistema.
     * @return Triple (año, mes, día)
     */
    fun getToday(): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance()
        return Triple(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
}
