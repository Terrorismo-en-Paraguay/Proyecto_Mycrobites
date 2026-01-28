package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Evento
import java.time.LocalDate

interface EventoDAO {
    /**
     * Obtiene todos los eventos de un usuario para una fecha específica
     */
    suspend fun getEventosByUsuarioAndFecha(idUsuario: Int, fecha: LocalDate): List<Evento>
    
    /**
     * Obtiene todos los eventos de un usuario
     */
    suspend fun getEventosByUsuario(idUsuario: Int): List<Evento>
    
    /**
     * Inserta un nuevo evento
     */
    suspend fun insertEvento(evento: Evento, idUsuario: Int): Boolean
    
    suspend fun insertEventoReturnId(evento: Evento, idUsuario: Int): Int
    
    suspend fun insertInvitados(idEvento: Int, emails: List<String>): Boolean
    
    /**
     * Obtiene los días del mes que tienen eventos para un usuario
     */
    suspend fun getDiasConEventos(idUsuario: Int, year: Int, month: Int): List<Int>

    /**
     * Obtiene todos los eventos de un usuario dentro de un rango de fechas
     */
    suspend fun getEventosByUsuarioAndRango(idUsuario: Int, fechaInicio: LocalDate, fechaFin: LocalDate): List<Evento>
    
    /**
     * Busca eventos por título
     */
    suspend fun searchEventos(idUsuario: Int, query: String): List<Evento>

    /**
     * Obtiene un evento por su ID
     */
    suspend fun getEventoById(idEvento: Int): Evento?

    /**
     * Elimina un evento
     */
    suspend fun deleteEvento(idEvento: Int): Boolean
}
