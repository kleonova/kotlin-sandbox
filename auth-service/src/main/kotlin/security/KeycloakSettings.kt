package lev.learn.sandbox.auth.service.security

import io.ktor.server.config.ApplicationConfig

data class KeycloakSettings(
    val realm: String,
    val issuerUrl: String,
    val clientId: String,
    val clientSecret: String
) {
    companion object {
        fun create(config: ApplicationConfig):KeycloakSettings {
            val keycloakConfig = config.config("keycloak")

            return KeycloakSettings(
                realm = getConfig(keycloakConfig, "realm"),
                issuerUrl = getConfig(keycloakConfig, "issuerUrl"),
                clientId = getConfig(keycloakConfig, "clientId"),
                clientSecret = getConfig(keycloakConfig, "clientSecret")
            )
        }

        private fun getConfig(config: ApplicationConfig, path: String): String {
            return config.property(path).getString()
        }
    }
}