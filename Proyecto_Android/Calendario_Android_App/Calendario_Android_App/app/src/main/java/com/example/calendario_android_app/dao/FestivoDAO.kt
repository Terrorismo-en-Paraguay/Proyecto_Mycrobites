package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Festivo

interface FestivoDAO {
    suspend fun getFestivoByMesYDia(mes: Int, dia: Int): Festivo?
    
    suspend fun getFestivosByMes(month: Int): List<Festivo>
    
    suspend fun getDiasFestivos(month: Int): List<Int>
}
