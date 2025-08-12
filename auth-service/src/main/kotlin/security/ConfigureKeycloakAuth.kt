package lev.learn.sandbox.auth.service.security

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.net.URL
import java.util.concurrent.TimeUnit

fun Application.configureKeycloakAuth(settings: KeycloakSettings) {
    val jwkProvider = JwkProviderBuilder(URL("${settings.issuerUrl}/protocol/openid-connect/certs"))
        .cached(10, 24, TimeUnit.HOURS)
        .build()

    install(Authentication) {
        jwt("keycloak-jwt") {
            realm = settings.realm
            verifier(jwkProvider) {
                withIssuer(settings.issuerUrl)
                withAudience(settings.clientId)
            }
            validate { credential ->
                if (credential.payload.audience.contains(settings.clientId)) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}