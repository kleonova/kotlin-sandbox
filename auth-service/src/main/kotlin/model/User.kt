package lev.learn.sandbox.auth.service.model

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
