package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Festivo

/**
 * Interfaz que define las operaciones de acceso a datos para la entidad Festivo.
 * Permite consultar días festivos para mostrarlos en el calendario.
 */
interface FestivoDAO {
    /**
     * Obtiene el festivo para un mes y día específico (independiente del año).
     * @param mes Mes del festivo (1-12)
     * @param dia Día del festivo (1-31)
     * @return Festivo encontrado o null si no hay festivo en esa fecha
     */
    suspend fun getFestivoByMesYDia(mes: Int, dia: Int): Festivo?
    
    /**
     * Obtiene todos los festivos de un mes (independiente del año).
     * @param month Mes a consultar (1-12)
     * @return Lista de festivos del mes
     */
    suspend fun getFestivosByMes(month: Int): List<Festivo>
    
    /**
     * Obtiene los días del mes que son festivos (independiente del año).
     * Útil para marcar días festivos en la vista de calendario.
     * @param month Mes a consultar (1-12)
     * @return Lista de días (1-31) que son festivos
     */
    suspend fun getDiasFestivos(month: Int): List<Int>
}
