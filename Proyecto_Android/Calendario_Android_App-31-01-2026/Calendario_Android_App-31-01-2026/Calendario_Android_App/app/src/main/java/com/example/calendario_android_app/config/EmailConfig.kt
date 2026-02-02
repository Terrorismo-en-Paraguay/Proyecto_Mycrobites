package com.example.calendario_android_app.config

/**
 * Configuración de correo electrónico para el servidor SMTP.
 * 
 * IMPORTANTE: Para uso en producción, estas credenciales deberían almacenarse de forma segura
 * (por ejemplo, en SharedPreferences cifradas o un servicio de backend seguro).
 */
object EmailConfig {
    // Configuración SMTP para Gmail
    const val SMTP_HOST = "smtp.gmail.com"
    const val SMTP_PORT = "587"
    const val SMTP_AUTH = "true"
    const val SMTP_STARTTLS_ENABLE = "true"
    
    // Credenciales de correo
    // Para Gmail, es necesario usar una "Contraseña de Aplicación", no la contraseña normal.
    // Se puede generar en: https://myaccount.google.com/apppasswords
    const val EMAIL_FROM = "2909015test@gmail.com"
    const val EMAIL_PASSWORD = "iort yfqe xmgl hdsp"
    const val EMAIL_FROM_NAME = "Calendario App Sergio"
    
    // --- Plantillas de Correo Electrónico ---
    // Estas funciones generan el cuerpo en formato HTML de los correos que se envían.
    
    /**
     * Plantilla para notificación de creación de una nueva etiqueta.
     */
    fun getLabelCreationTemplate(userName: String, labelName: String, labelColor: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; }
                    .label-preview { display: inline-block; padding: 10px 20px; border-radius: 5px; color: white; margin: 10px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✨ Nueva Etiqueta Creada</h1>
                    </div>
                    <div class="content">
                        <p>Hola <strong>$userName</strong>,</p>
                        <p>Has creado una nueva etiqueta en tu calendario:</p>
                        <div class="label-preview" style="background-color: $labelColor;">
                            $labelName
                        </div>
                        <p>Ahora puedes usar esta etiqueta para organizar tus eventos.</p>
                    </div>
                    <div class="footer">
                        <p>Este es un mensaje automático de Calendario App</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Plantilla para notificación de creación de un nuevo evento.
     */
    fun getEventCreationTemplate(userName: String, eventTitle: String, eventDate: String, eventTime: String, eventLocation: String?): String {
        val locationHtml = if (eventLocation != null) {
            "<p><strong>📍 Ubicación:</strong> $eventLocation</p>"
        } else {
            ""
        }
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; }
                    .event-details { background-color: white; padding: 15px; border-left: 4px solid #2196F3; margin: 15px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📅 Nuevo Evento Creado</h1>
                    </div>
                    <div class="content">
                        <p>Hola <strong>$userName</strong>,</p>
                        <p>Has creado un nuevo evento:</p>
                        <div class="event-details">
                            <h2>$eventTitle</h2>
                            <p><strong>📆 Fecha:</strong> $eventDate</p>
                            <p><strong>🕐 Hora:</strong> $eventTime</p>
                            $locationHtml
                        </div>
                        <p>El evento ha sido agregado a tu calendario.</p>
                    </div>
                    <div class="footer">
                        <p>Este es un mensaje automático de Calendario App</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Plantilla para notificación de creación de un nuevo calendario o grupo.
     */
    fun getGroupCreationTemplate(userName: String, groupName: String, groupDescription: String?): String {
        val descriptionHtml = if (groupDescription != null) {
            "<p><strong>Descripción:</strong> $groupDescription</p>"
        } else {
            ""
        }
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #FF9800; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; }
                    .group-info { background-color: white; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>👥 Nuevo Calendario/Grupo Creado</h1>
                    </div>
                    <div class="content">
                        <p>Hola <strong>$userName</strong>,</p>
                        <p>Has creado un nuevo calendario/grupo:</p>
                        <div class="group-info">
                            <h2>$groupName</h2>
                            $descriptionHtml
                        </div>
                        <p>Ahora puedes invitar a otros usuarios y compartir eventos con este grupo.</p>
                    </div>
                    <div class="footer">
                        <p>Este es un mensaje automático de Calendario App</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Plantilla para notificar a un usuario que ha sido añadido a un grupo.
     */
    fun getUserAddedToGroupTemplate(userName: String, groupName: String, addedByName: String, role: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #9C27B0; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; }
                    .group-info { background-color: white; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Te han agregado a un grupo</h1>
                    </div>
                    <div class="content">
                        <p>Hola <strong>$userName</strong>,</p>
                        <p><strong>$addedByName</strong> te ha agregado al grupo:</p>
                        <div class="group-info">
                            <h2>$groupName</h2>
                            <p><strong>Tu rol:</strong> $role</p>
                        </div>
                        <p>Ahora puedes ver y gestionar los eventos compartidos en este grupo.</p>
                    </div>
                    <div class="footer">
                        <p>Este es un mensaje automático de Calendario App</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Plantilla para el recordatorio diario de eventos programados.
     */
    fun getDailyReminderTemplate(userName: String, eventsHtml: String, eventCount: Int): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #F44336; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; }
                    .event-item { background-color: white; padding: 15px; margin: 10px 0; border-left: 4px solid #F44336; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔔 Recordatorio Diario</h1>
                    </div>
                    <div class="content">
                        <p>Hola <strong>$userName</strong>,</p>
                        <p>Tienes <strong>$eventCount</strong> evento(s) programado(s) para hoy:</p>
                        $eventsHtml
                        <p>¡Que tengas un excelente día!</p>
                    </div>
                    <div class="footer">
                        <p>Este es un mensaje automático de Calendario App</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Plantilla para notificar un inicio de sesión exitoso (Seguridad).
     */
    fun getLoginTemplate(userName: String, loginTime: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #607D8B; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Inicio de Sesión Exitoso</h1>
                    </div>
                    <div class="content">
                        <p>Hola <strong>$userName</strong>,</p>
                        <p>Se ha detectado un nuevo inicio de sesión en tu cuenta.</p>
                        <p><strong>Fecha y hora:</strong> $loginTime</p>
                        <p>Si no has sido tú, por favor cambia tu contraseña inmediatamente.</p>
                    </div>
                    <div class="footer">
                        <p>Este es un mensaje automático de Calendario App</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Plantilla de bienvenida para nuevos usuarios registrados.
     */
    fun getRegistrationTemplate(userName: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>👋 ¡Bienvenido a Calendario App!</h1>
                    </div>
                    <div class="content">
                        <p>Hola <strong>$userName</strong>,</p>
                        <p>Tu cuenta ha sido creada exitosamente. Estamos encantados de tenerte con nosotros.</p>
                        <p>Ahora puedes empezar a organizar tus eventos, crear grupos y gestionar tus etiquetas de forma eficiente.</p>
                    </div>
                    <div class="footer">
                        <p>Este es un mensaje automático de Calendario App</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Plantilla para notificar que la contraseña ha sido cambiada (Seguridad).
     */
    fun getPasswordChangedTemplate(userName: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #f44336; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Contraseña Cambiada</h1>
                    </div>
                    <div class="content">
                        <p>Hola <strong>$userName</strong>,</p>
                        <p>Te informamos que la contraseña de tu cuenta de Calendario App ha sido cambiada recientemente.</p>
                        <p>Si has sido tú, no necesitas realizar ninguna acción adicional.</p>
                        <p><strong>Si no has realizado este cambio</strong>, por favor ponte en contacto con nosotros inmediatamente para asegurar tu cuenta.</p>
                    </div>
                    <div class="footer">
                        <p>Este es un mensaje automático de Calendario App</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
