package lev.learn.sandbox.notification.controller

import lev.learn.sandbox.notification.model.EmailNotification
import lev.learn.sandbox.notification.model.NotificationRequest
import lev.learn.sandbox.notification.service.EmailService

class NotificationController(
    private val service: EmailService
) {
    fun sendEmail(request: NotificationRequest) {
        service.sendEmail(
            EmailNotification(
                to = request.to,
                subject = request.subject,
                body = request.body
            )
        )
    }
}
