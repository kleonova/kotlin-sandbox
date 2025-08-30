package lev.learn.sandbox.notification.service.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    val to: String,
    val subject: String,
    val body: String
)
