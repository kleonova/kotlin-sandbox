package lev.learn.sandbox.notification.service

import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import lev.learn.sandbox.notification.model.EmailNotification
import java.util.Properties

class EmailService {
    // Простая реализация отправки через javax.mail
    fun sendEmail(emailNotification: EmailNotification) {
        val props = Properties().apply {
            put("mail.smtp.host", "localhost")
            put("mail.smtp.port", "1025") // MailHog SMTP-порт
            put("mail.smtp.connectiontimeout", "5000")
            put("mail.smtp.timeout", "5000")
        }

        val session = Session.getInstance(props)
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(emailNotification.from))
            setRecipients(Message.RecipientType.TO, emailNotification.to)
            setSubject(emailNotification.subject)
            setText(emailNotification.body)
        }

        Transport.send(message)
    }
}
