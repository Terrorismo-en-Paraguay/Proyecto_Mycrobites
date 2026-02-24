package com.example.calendario_android_app.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.calendario_android_app.config.EmailConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import jakarta.mail.internet.MimeBodyPart

object EmailService {
    private const val TAG = "EmailService"
    
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    private suspend fun sendEmail(
        toEmail: String,
        subject: String,
        htmlBody: String,
        context: Context
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val appContext = context.applicationContext
            // 1. Verificación de red
            if (!isNetworkAvailable(appContext)) {
                Log.e(TAG, "No hay conexión a la red de Internet")
                return@withContext false
            }
            
            // 2. Configuración de propiedades SMTP
            val props = Properties().apply {
                put("mail.smtp.host", EmailConfig.SMTP_HOST)
                put("mail.smtp.port", EmailConfig.SMTP_PORT)
                put("mail.smtp.auth", EmailConfig.SMTP_AUTH)
                put("mail.smtp.starttls.enable", EmailConfig.SMTP_STARTTLS_ENABLE)
            }
            
            // 3. Creación de la sesión con autenticación
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(EmailConfig.EMAIL_FROM, EmailConfig.EMAIL_PASSWORD)
                }
            })
            
            // 4. Creación del mensaje Mime (formato HTML)
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(EmailConfig.EMAIL_FROM, EmailConfig.EMAIL_FROM_NAME))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                this.subject = subject
                
                val multipart = MimeMultipart()
                val messageBodyPart = MimeBodyPart()
                messageBodyPart.setContent(htmlBody, "text/html; charset=utf-8")
                multipart.addBodyPart(messageBodyPart)
                
                setContent(multipart)
            }
            
            // 5. Envío efectivo del correo
            Transport.send(message)
            Log.i(TAG, "Correo enviado correctamente a $toEmail")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar correo a $toEmail: ${e.message}", e)
            if (e.message?.contains("authentication", ignoreCase = true) == true) {
                Log.e(TAG, "AUTHENTICATION FAILED: Check your EMAIL_FROM and EMAIL_PASSWORD in EmailConfig.kt")
            }
            false
        }
    }
    
    suspend fun sendLabelCreationNotification(
        userEmail: String,
        userName: String,
        labelName: String,
        labelColor: String,
        context: Context
    ): Boolean {
        val subject = "Nueva etiqueta creada: $labelName"
        val htmlBody = EmailConfig.getLabelCreationTemplate(userName, labelName, labelColor)
        return sendEmail(userEmail, subject, htmlBody, context)
    }
    
    suspend fun sendEventCreationNotification(
        userEmail: String,
        userName: String,
        eventTitle: String,
        eventDate: String,
        eventTime: String,
        eventLocation: String?,
        context: Context
    ): Boolean {
        val subject = "Nuevo evento creado: $eventTitle"
        val htmlBody = EmailConfig.getEventCreationTemplate(userName, eventTitle, eventDate, eventTime, eventLocation)
        return sendEmail(userEmail, subject, htmlBody, context)
    }
    
    suspend fun sendGroupCreationNotification(
        userEmail: String,
        userName: String,
        groupName: String,
        groupDescription: String?,
        context: Context
    ): Boolean {
        val subject = "Nuevo calendario/grupo creado: $groupName"
        val htmlBody = EmailConfig.getGroupCreationTemplate(userName, groupName, groupDescription)
        return sendEmail(userEmail, subject, htmlBody, context)
    }
    
    suspend fun sendUserAddedToGroupNotification(
        userEmail: String,
        userName: String,
        groupName: String,
        addedByName: String,
        role: String,
        context: Context
    ): Boolean {
        val subject = "Te han agregado al grupo: $groupName"
        val htmlBody = EmailConfig.getUserAddedToGroupTemplate(userName, groupName, addedByName, role)
        return sendEmail(userEmail, subject, htmlBody, context)
    }
    
    suspend fun sendDailyReminder(
        userEmail: String,
        userName: String,
        events: List<DailyEventInfo>,
        context: Context
    ): Boolean {
        if (events.isEmpty()) return true
        
        val eventsHtml = events.joinToString("") { event ->
            """
            <div class="event-item">
                <h3>${event.title}</h3>
                <p><strong>🕐 Hora:</strong> ${event.time}</p>
                ${if (event.location != null) "<p><strong>📍 Ubicación:</strong> ${event.location}</p>" else ""}
            </div>
            """.trimIndent()
        }
        
        val subject = "Recordatorio: Tienes ${events.size} evento(s) hoy"
        val htmlBody = EmailConfig.getDailyReminderTemplate(userName, eventsHtml, events.size)
        return sendEmail(userEmail, subject, htmlBody, context)
    }
    
    suspend fun sendLoginNotification(
        userEmail: String,
        userName: String,
        loginTime: String,
        context: Context
    ): Boolean {
        val subject = "Inicio de sesión detectado"
        val htmlBody = EmailConfig.getLoginTemplate(userName, loginTime)
        return sendEmail(userEmail, subject, htmlBody, context)
    }

    suspend fun sendRegistrationNotification(
        userEmail: String,
        userName: String,
        context: Context
    ): Boolean {
        val subject = "¡Bienvenido a Calendario App!"
        val htmlBody = EmailConfig.getRegistrationTemplate(userName)
        return sendEmail(userEmail, subject, htmlBody, context)
    }

    suspend fun sendPasswordChangedNotification(
        userEmail: String,
        userName: String,
        context: Context
    ): Boolean {
        val subject = "Seguridad: Tu contraseña ha sido cambiada"
        val htmlBody = EmailConfig.getPasswordChangedTemplate(userName)
        return sendEmail(userEmail, subject, htmlBody, context)
    }

    data class DailyEventInfo(
        val title: String,
        val time: String,
        val location: String?
    )
}
