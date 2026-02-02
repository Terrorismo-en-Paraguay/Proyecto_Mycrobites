package com.example.calendario_android_app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.calendario_android_app.dao.impl.EventoDAOImpl
import com.example.calendario_android_app.dao.impl.UsuarioDAOImpl
import com.example.calendario_android_app.service.EmailService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Worker that runs daily to send email reminders to all users with events today
 */
class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val TAG = "DailyReminderWorker"
    private val eventoDAO = EventoDAOImpl()
    private val usuarioDAO = UsuarioDAOImpl()

    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting daily reminder worker")
        
        try {
            val today = LocalDate.now()
            
            // In a real app, we might want to batch this or only do it for users who have reminders enabled
            // For now, we'll iterate through all users who have events today
            
            // Note: We need a way to get all users. Our UsuarioDAO doesn't have getAllUsers.
            // Let's implement a simple query to get all users with events today.
            
            val usersWithEvents = getUsersWithEventsForDate(today)
            
            usersWithEvents.forEach { (userId, email, name) ->
                val events = eventoDAO.getEventosByUsuarioAndFecha(userId, today)
                if (events.isNotEmpty()) {
                    val eventInfos = events.map { event ->
                        EmailService.DailyEventInfo(
                            title = event.titulo,
                            time = event.fechaInicio.format(DateTimeFormatter.ofPattern("HH:mm")),
                            location = event.ubicacion
                        )
                    }
                    
                    EmailService.sendDailyReminder(
                        userEmail = email,
                        userName = name,
                        events = eventInfos,
                        context = applicationContext
                    )
                    Log.i(TAG, "Sent daily reminder to $email with ${events.size} events")
                }
            }
            
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in daily reminder worker", e)
            return Result.retry()
        }
    }

    /**
     * Helper to get users who have events on a specific date
     */
    private suspend fun getUsersWithEventsForDate(date: LocalDate): List<Triple<Int, String, String>> {
        val users = mutableListOf<Triple<Int, String, String>>()
        val connection = com.example.calendario_android_app.util.DatabaseConnection.getConnection()
        
        try {
            connection?.let { conn ->
                val sql = """
                    SELECT DISTINCT u.id_usuario, u.correo, c.nombre
                    FROM usuarios u
                    INNER JOIN eventos_usuarios eu ON u.id_usuario = eu.id_usuario
                    INNER JOIN eventos e ON eu.id_evento = e.id_evento
                    LEFT JOIN clientes c ON u.id_cliente = c.id_cliente
                    WHERE DATE(e.fecha_inicio) = ?
                """
                val statement = conn.prepareStatement(sql)
                statement.setString(1, date.toString())
                
                val rs = statement.executeQuery()
                while (rs.next()) {
                    users.add(Triple(
                        rs.getInt("id_usuario"),
                        rs.getString("correo"),
                        rs.getString("nombre") ?: "Usuario"
                    ))
                }
                rs.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return users
    }
}
