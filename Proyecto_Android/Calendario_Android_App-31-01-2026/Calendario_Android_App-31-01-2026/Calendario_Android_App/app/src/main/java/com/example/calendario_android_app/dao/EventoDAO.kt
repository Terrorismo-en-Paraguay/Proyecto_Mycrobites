package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Evento
import java.time.LocalDate

/**
 * Interfaz que define las operaciones de acceso a datos para la entidad Evento.
 * Gestiona la creación, consulta, búsqueda y eliminación de eventos del calendario.
 */
interface EventoDAO {
    /**
     * Obtiene todos los eventos de un usuario para una fecha específica.
     * @param idUsuario ID del usuario
     * @param fecha Fecha para filtrar los eventos
     * @return Lista de eventos en la fecha indicada
     */
    suspend fun getEventosByUsuarioAndFecha(idUsuario: Int, fecha: LocalDate): List<Evento>
    
    /**
     * Obtiene todos los eventos de un usuario sin filtro de fecha.
     * @param idUsuario ID del usuario
     * @return Lista completa de eventos del usuario
     */
    suspend fun getEventosByUsuario(idUsuario: Int): List<Evento>
    
    /**
     * Inserta un nuevo evento en la base de datos.
     * @param evento Datos del evento a crear
     * @param idUsuario ID del usuario creador
     * @return true si se creó correctamente
     */
    suspend fun insertEvento(evento: Evento, idUsuario: Int): Boolean
    
    /**
     * Inserta un nuevo evento y retorna su ID generado.
     * @param evento Datos del evento a crear
     * @param idUsuario ID del usuario creador
     * @return ID del evento creado
     */
    suspend fun insertEventoReturnId(evento: Evento, idUsuario: Int): Int
    
    /**
     * Registra invitados externos a un evento por correo electrónico.
     * @param idEvento ID del evento
     * @param emails Lista de correos de invitados
     * @return true si se registraron correctamente
     */
    suspend fun insertInvitados(idEvento: Int, emails: List<String>): Boolean
    
    /**
     * Obtiene los días del mes que tienen eventos para un usuario.
     * Útil para marcar días con eventos en la vista de calendario.
     * @param idUsuario ID del usuario
     * @param year Año
     * @param month Mes (1-12)
     * @return Lista de días (1-31) que tienen eventos
     */
    suspend fun getDiasConEventos(idUsuario: Int, year: Int, month: Int): List<Int>

    /**
     * Obtiene todos los eventos de un usuario dentro de un rango de fechas.
     * @param idUsuario ID del usuario
     * @param fechaInicio Fecha de inicio del rango (inclusive)
     * @param fechaFin Fecha de fin del rango (inclusive)
     * @return Lista de eventos en el rango especificado
     */
    suspend fun getEventosByUsuarioAndRango(idUsuario: Int, fechaInicio: LocalDate, fechaFin: LocalDate): List<Evento>
    
    /**
     * Busca eventos por título usando coincidencia parcial.
     * @param idUsuario ID del usuario
     * @param query Texto a buscar en el título
     * @return Lista de eventos que coinciden con la búsqueda
     */
    suspend fun searchEventos(idUsuario: Int, query: String): List<Evento>

    /**
     * Obtiene un evento por su ID.
     * @param idEvento ID del evento
     * @return Evento encontrado o null si no existe
     */
    suspend fun getEventoById(idEvento: Int): Evento?

    /**
     * Elimina un evento de la base de datos.
     * @param idEvento ID del evento a eliminar
     * @return true si se eliminó correctamente
     */
    suspend fun deleteEvento(idEvento: Int): Boolean
}
