package lev.learn.sandbox.auth.service.service

import lev.learn.sandbox.auth.service.model.User
import lev.learn.sandbox.auth.service.repository.UserRepository
import org.slf4j.LoggerFactory

class UserService() {
    private val userRepository: UserRepository = UserRepository()
    private val logger = LoggerFactory.getLogger("UserService")

    fun registerOrAuthenticate(username: String, email: String?, firstName: String?, lastName: String?): User {
        logger.info("User service handle user: $username, $email, $firstName, $lastName")

        val existing = userRepository.findByUsername(username)

        if (existing != null) {
            // Обновим данные, если изменились
            val updated = existing.copy(
                email = email ?: existing.email,
                firstName = firstName ?: existing.firstName,
                lastName = lastName ?: existing.lastName,
                updatedAt = java.time.LocalDateTime.now()
            )
            userRepository.save(updated)
            return updated
        } else {
            val newUser = User(
                username = username,
                email = email,
                firstName = firstName,
                lastName = lastName
            )
            return userRepository.save(newUser)
        }
    }
}