package org.example.calendario_app;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class Mail {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "2909015test@gmail.com";
    private static final String APP_PASSWORD = "iort yfqe xmgl hdsp";

    public static void sendGroupInvitation(String recipientEmail, String userName, String addedByName, String groupName,
            String role) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("🎉 Te han agregado a un grupo");

            String htmlContent = getHtmlTemplate(userName, addedByName, groupName, role);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Correo enviado exitosamente a " + recipientEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }
    }

    private static String getHtmlTemplate(String userName, String addedByName, String groupName, String role) {
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
                """
                .replace("$userName", userName)
                .replace("$addedByName", addedByName)
                .replace("$groupName", groupName)
                .replace("$role", role);
    }

    public static void sendPasswordChangeNotification(String recipientEmail, String userName) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("🔒 Alerta de seguridad: Contraseña cambiada");

            String htmlContent = getPasswordChangeHtmlTemplate(userName);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Correo de cambio de contraseña enviado exitosamente a " + recipientEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error al enviar el correo de cambio de contraseña: " + e.getMessage());
        }
    }

    private static String getPasswordChangeHtmlTemplate(String userName) {
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
                """
                .replace("$userName", userName);
    }

    public static void sendLoginNotification(String recipientEmail, String userName) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("🔑 Nuevo inicio de sesión detectado");

            String htmlContent = getLoginHtmlTemplate(userName);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Correo de alerta de inicio de sesión enviado a " + recipientEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error al enviar el correo de login: " + e.getMessage());
        }
    }

    public static void sendRegistrationWelcome(String recipientEmail, String userName) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("👋 Bienvenido a Calendario App");

            String htmlContent = getRegistrationHtmlTemplate(userName);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Correo de bienvenida enviado a " + recipientEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error al enviar el correo de bienvenida: " + e.getMessage());
        }
    }

    private static String getLoginHtmlTemplate(String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔑 Nuevo Inicio de Sesión</h1>
                        </div>
                        <div class="content">
                            <p>Hola <strong>$userName</strong>,</p>
                            <p>Se ha detectado un nuevo inicio de sesión en tu cuenta de Calendario App.</p>
                            <p>Si has sido tú, puedes ignorar este mensaje.</p>
                            <p><strong>Si no reconoces esta actividad</strong>, te recomendamos cambiar tu contraseña inmediatamente y contactar con soporte.</p>
                        </div>
                        <div class="footer">
                            <p>Este es un mensaje automático de Calendario App</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .replace("$userName", userName);
    }

    private static String getRegistrationHtmlTemplate(String userName) {
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
                            <p>Estamos encantados de tenerte con nosotros. Tu cuenta ha sido creada exitosamente.</p>
                            <p>Ahora puedes comenzar a organizar tus eventos, crear grupos y compartir calendarios con tus amigos y compañeros.</p>
                            <p>¡Esperamos que disfrutes de la experiencia!</p>
                        </div>
                        <div class="footer">
                            <p>Este es un mensaje automático de Calendario App</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .replace("$userName", userName);
    }
}
