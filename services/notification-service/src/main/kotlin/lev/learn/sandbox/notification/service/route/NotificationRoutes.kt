package lev.learn.sandbox.notification.service.route

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.serialization.json.Json
import lev.learn.sandbox.notification.service.model.NotificationRequest
import java.util.Properties

fun Application.configureNotificationRouting() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        }) // Подключаем поддержку JSON
    }

    routing {
        post("/notify") {
            val request = call.receive<NotificationRequest>()

            try {
                sendEmail(
                    to = request.to,
                    subject = request.subject,
                    body = request.body
                )
                call.respond(HttpStatusCode.Accepted, "Email sent to ${request.to}")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to send email: ${e.message}")
            }
        }
    }
}

// Простая реализация отправки через javax.mail
private fun sendEmail(to: String, subject: String, body: String) {
    val props = Properties().apply {
        put("mail.smtp.host", "localhost")
        put("mail.smtp.port", "1025") // MailHog SMTP-порт
        put("mail.smtp.connectiontimeout", "5000")
        put("mail.smtp.timeout", "5000")
    }

    val session = Session.getInstance(props)
    val message = MimeMessage(session).apply {
        setFrom(InternetAddress("no-reply@ksb.dev"))
        setRecipients(Message.RecipientType.TO, to)
        setSubject(subject)
        setText(body)
    }

    Transport.send(message)
}
