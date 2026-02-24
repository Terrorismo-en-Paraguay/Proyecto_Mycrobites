package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Evento
import java.time.LocalDate

interface EventoDAO {
    suspend fun getEventosByUsuarioAndFecha(idUsuario: Int, fecha: LocalDate): List<Evento>
    
    suspend fun getEventosByUsuario(idUsuario: Int): List<Evento>
    
    suspend fun insertEvento(evento: Evento, idUsuario: Int): Boolean
    
    suspend fun insertEventoReturnId(evento: Evento, idUsuario: Int): Int
    
    suspend fun insertInvitados(idEvento: Int, emails: List<String>): Boolean
    
    suspend fun getDiasConEventos(idUsuario: Int, year: Int, month: Int): List<Int>

    suspend fun getEventosByUsuarioAndRango(idUsuario: Int, fechaInicio: LocalDate, fechaFin: LocalDate): List<Evento>
    
    suspend fun searchEventos(idUsuario: Int, query: String): List<Evento>

    suspend fun getEventoById(idEvento: Int): Evento?

    suspend fun deleteEvento(idEvento: Int): Boolean
}
