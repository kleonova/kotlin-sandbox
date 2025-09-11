package lev.learn.sandbox.notification.model

data class EmailNotification(
    val from: String = "no-reply@ksb.dev",
    val to: String,
    val subject: String,
    val body: String
)
