package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Festivo

interface FestivoDAO {
    /**
     * Obtiene el festivo para un mes y día específico (independiente del año)
     */
    suspend fun getFestivoByMesYDia(mes: Int, dia: Int): Festivo?
    
    /**
     * Obtiene todos los festivos de un mes (independiente del año)
     */
    suspend fun getFestivosByMes(month: Int): List<Festivo>
    
    /**
     * Obtiene los días del mes que son festivos (independiente del año)
     */
    suspend fun getDiasFestivos(month: Int): List<Int>
}
