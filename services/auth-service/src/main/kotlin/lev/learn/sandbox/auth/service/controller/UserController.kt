package lev.learn.sandbox.auth.service.controller

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import lev.learn.sandbox.auth.service.service.UserService
import org.slf4j.LoggerFactory
import java.util.Base64

class UserController {
    val userService = UserService()
    private val logger = LoggerFactory.getLogger("UserController")
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true  // разрешает парсить нестрогий JSON
    }

    fun registerOrAuthenticate(token: String?) {
        logger.info("UserController handle user: $token")
        if (token != null) {
            val claims = parseJwtClaims(token)
            val username = claims["preferred_username"]
                ?: throw IllegalArgumentException("No 'preferred_username' in token")
            val email = claims["email"]
            val firstName = claims["given_name"]
            val lastName = claims["family_name"]

            userService.registerOrAuthenticate(username, email, firstName, lastName)
        }
    }

    private fun parseJwtClaims(token: String): Map<String, String?> {
        try {
            val parts = token.split(".")
            require(parts.size == 3) { "Invalid JWT token: must have 3 parts" }

            val payload = parts[1]
            val decoded = Base64.getDecoder().decode(payload)
            val jsonString = String(decoded, Charsets.UTF_8)

            val jsonObject = json.parseToJsonElement(jsonString).jsonObject

            // Конвертируем в Map<String, String?> для удобства
            return jsonObject.mapValues { entry ->
                when (val value = entry.value) {
                    is JsonPrimitive -> value.content
                    is JsonObject -> value.toString()
                    is JsonArray -> value.toString()
                }
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse JWT payload: ${e.message}", e)
        }
    }
}
