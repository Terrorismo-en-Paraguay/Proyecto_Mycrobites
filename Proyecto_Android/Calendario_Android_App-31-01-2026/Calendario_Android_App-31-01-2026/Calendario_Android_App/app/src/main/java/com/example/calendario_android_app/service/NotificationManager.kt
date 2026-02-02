package com.example.calendario_android_app.service

import android.content.Context
import android.util.Log
import com.example.calendario_android_app.dao.impl.UsuarioDAOImpl
import com.example.calendario_android_app.dao.impl.GrupoDAOImpl
import com.example.calendario_android_app.model.Evento
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * Gestor encargado de coordinar todas las operaciones de notificación.
 * Esta clase actúa como un puente entre la base de datos (DAOs) y el servicio de correo (EmailService).
 * Se debe llamar desde la capa de UI (Fragments/ViewModels) donde el Context esté disponible.
 */
object NotificationManager {
    private const val TAG = "NotificationManager"
    
    /**
     * Notifica al creador que ha creado una nueva etiqueta personal.
     */
    fun notifyLabelCreated(
        context: Context,
        idUsuario: Int,
        labelName: String,
        labelColor: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usuarioDAO = UsuarioDAOImpl()
                val userInfo = usuarioDAO.getUserInfo(idUsuario)
                
                userInfo?.let { (email, name) ->
                    EmailService.sendLabelCreationNotification(
                        userEmail = email,
                        userName = name,
                        labelName = labelName,
                        labelColor = labelColor,
                        context = context
                    )
                    Log.i(TAG, "Notificación de creación de etiqueta enviada a $email")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar notificación de etiqueta", e)
            }
        }
    }
    
    /**
     * Envía una notificación de creación de etiqueta a todos los miembros de un grupo.
     */
    fun notifyLabelCreatedToGroup(
        context: Context,
        idGroup: Int,
        labelName: String,
        labelColor: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val grupoDAO = GrupoDAOImpl()
                val members = grupoDAO.getIntegrantesGrupo(idGroup)
                
                members.forEach { member ->
                    member.userId?.let { userId ->
                        EmailService.sendLabelCreationNotification(
                            userEmail = member.email,
                            userName = member.userName ?: member.email,
                            labelName = labelName,
                            labelColor = labelColor,
                            context = context
                        )
                    }
                }
                Log.i(TAG, "Notificaciones de etiqueta enviadas a los miembros del grupo $idGroup")
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar notificaciones de etiqueta al grupo", e)
            }
        }
    }
    
    /**
     * Notifica al creador que su evento ha sido creado correctamente.
     */
    fun notifyEventCreated(
        context: Context,
        evento: Evento,
        idCreador: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usuarioDAO = UsuarioDAOImpl()
                val userInfo = usuarioDAO.getUserInfo(idCreador)
                
                userInfo?.let { (email, name) ->
                    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    
                    EmailService.sendEventCreationNotification(
                        userEmail = email,
                        userName = name,
                        eventTitle = evento.titulo,
                        eventDate = evento.fechaInicio.format(dateFormatter),
                        eventTime = evento.fechaInicio.format(timeFormatter),
                        eventLocation = evento.ubicacion,
                        context = context
                    )
                    Log.i(TAG, "Notificación de evento enviada a $email")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar notificación de evento", e)
            }
        }
    }
    
    /**
     * Notifica a todos los miembros de un grupo sobre un nuevo evento compartido.
     */
    fun notifyEventCreatedToGroup(
        context: Context,
        evento: Evento,
        idGroup: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val grupoDAO = GrupoDAOImpl()
                val members = grupoDAO.getIntegrantesGrupo(idGroup)
                val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                
                members.forEach { member ->
                    try {
                        EmailService.sendEventCreationNotification(
                            userEmail = member.email,
                            userName = member.userName ?: member.email,
                            eventTitle = evento.titulo,
                            eventDate = evento.fechaInicio.format(dateFormatter),
                            eventTime = evento.fechaInicio.format(timeFormatter),
                            eventLocation = evento.ubicacion,
                            context = context
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al notificar al miembro ${member.email}", e)
                    }
                }
                Log.i(TAG, "Notificaciones de evento grupal enviadas al grupo $idGroup")
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar notificaciones grupales", e)
            }
        }
    }
    
    /**
     * Notifica al creador que ha creado un nuevo grupo/calendario.
     */
    fun notifyGroupCreated(
        context: Context,
        idUsuario: Int,
        groupName: String,
        groupDescription: String?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usuarioDAO = UsuarioDAOImpl()
                val userInfo = usuarioDAO.getUserInfo(idUsuario)
                
                userInfo?.let { (email, name) ->
                    EmailService.sendGroupCreationNotification(
                        userEmail = email,
                        userName = name,
                        groupName = groupName,
                        groupDescription = groupDescription,
                        context = context
                    )
                    Log.i(TAG, "Notificación de creación de grupo enviada a $email")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar notificación de grupo", e)
            }
        }
    }
    
    /**
     * Notifica a uno o más usuarios que han sido añadidos a un grupo determinado.
     */
    fun notifyUsersAddedToGroup(
        context: Context,
        idUsuarioAddedList: List<Int>,
        idUsuarioAdder: Int,
        groupName: String,
        roles: Map<Int, String> // Mapeo de ID de usuario a su rol
    ) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usuarioDAO = UsuarioDAOImpl()
                val adderUserInfo = usuarioDAO.getUserInfo(idUsuarioAdder)
                
                if (adderUserInfo != null) {
                    val (_, adderName) = adderUserInfo
                    
                    idUsuarioAddedList.forEach { idUsuarioAdded ->
                        try {
                            val addedUserInfo = usuarioDAO.getUserInfo(idUsuarioAdded)
                            addedUserInfo?.let { (addedEmail, addedName) ->
                                EmailService.sendUserAddedToGroupNotification(
                                    userEmail = addedEmail,
                                    userName = addedName,
                                    groupName = groupName,
                                    addedByName = adderName,
                                    role = roles[idUsuarioAdded] ?: "miembro",
                                    context = appContext
                                )
                                Log.i(TAG, "Usuario $addedEmail notificado de ingreso al grupo")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al notificar al usuario $idUsuarioAdded", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en el proceso de notificación a nuevos miembros", e)
            }
        }
    }

    /**
     * Envía notificaciones a invitados directos (lista de correos) de un evento.
     */
    fun notifyEventGuests(
        context: Context,
        evento: Evento,
        guestEmails: List<String>
    ) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                
                guestEmails.forEach { guestEmail ->
                    try {
                        EmailService.sendEventCreationNotification(
                            userEmail = guestEmail,
                            userName = "Invitado",
                            eventTitle = evento.titulo,
                            eventDate = evento.fechaInicio.format(dateFormatter),
                            eventTime = evento.fechaInicio.format(timeFormatter),
                            eventLocation = evento.ubicacion,
                            context = appContext
                        )
                        Log.i(TAG, "Notificación enviada al invitado: $guestEmail")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al notificar al invitado $guestEmail", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar notificaciones a invitados", e)
            }
        }
    }

    /**
     * Notificación de seguridad por inicio de sesión exitoso.
     */
    fun notifyLoginSuccessful(
        context: Context,
        userEmail: String,
        userName: String
    ) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loginTime = java.time.LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                
                EmailService.sendLoginNotification(
                    userEmail = userEmail,
                    userName = userName,
                    loginTime = loginTime,
                    context = appContext
                )
                Log.i(TAG, "Notificación de login enviada a $userEmail")
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar notificación de login", e)
            }
        }
    }

    /**
     * Notificación de bienvenida tras el registro de un nuevo usuario.
     */
    fun notifyRegistrationSuccessful(
        context: Context,
        userEmail: String,
        userName: String
    ) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                EmailService.sendRegistrationNotification(
                    userEmail = userEmail,
                    userName = userName,
                    context = appContext
                )
                Log.i(TAG, "Notificación de bienvenida enviada a $userEmail")
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar notificación de bienvenida", e)
            }
        }
    }

    /**
     * Notificación de seguridad tras un cambio de contraseña.
     */
    fun notifyPasswordChanged(
        context: Context,
        idUsuario: Int
    ) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usuarioDAO = UsuarioDAOImpl()
                val userInfo = usuarioDAO.getUserInfo(idUsuario)
                
                userInfo?.let { (email, name) ->
                    EmailService.sendPasswordChangedNotification(
                        userEmail = email,
                        userName = name,
                        context = appContext
                    )
                    Log.i(TAG, "Notificación de cambio de contraseña enviada a $email")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar notificación de seguridad", e)
            }
        }
    }
}
