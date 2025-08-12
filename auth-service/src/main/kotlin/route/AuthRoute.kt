package lev.learn.sandbox.auth.service.route

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import io.ktor.serialization.jackson.jackson
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json
import lev.learn.sandbox.auth.service.model.LoginRequest
import lev.learn.sandbox.auth.service.model.TokenResponse
import lev.learn.sandbox.auth.service.security.KeycloakSettings
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("AuthRoute")

val httpClient = HttpClient(CIO) {
    install(ClientContentNegotiation) {  // Используем переименованный импорт
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

fun Route.authRoutes(keycloak: KeycloakSettings) {
    install(ContentNegotiation) {
        jackson()
        json(Json {
            ignoreUnknownKeys = true
        }) // Подключаем поддержку JSON
    }

    // Получение токена от Keycloak
    post("/login") {
        val req = call.receive<LoginRequest>()

        val response: HttpResponse = httpClient.submitForm(
            url = "${keycloak.issuerUrl}/protocol/openid-connect/token",
            formParameters = parameters {
                append("client_id", keycloak.clientId)
                append("username", req.username)
                append("password", req.password)
                append("grant_type", "password")
                if (keycloak.clientSecret.isNotEmpty()) {
                    append("client_secret", keycloak.clientSecret)
                }
            }
        )

        if (response.status == HttpStatusCode.OK) {
            val token: TokenResponse = response.body()
            call.respond(token)
        } else {
            call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
        }
    }

    // Logout (инвалидируем refresh токен в Keycloak)
    post("/logout") {
        val params = call.receiveParameters()
        val refreshToken = params["refresh_token"] ?: return@post call.respond(HttpStatusCode.BadRequest)

        httpClient.submitForm(
            url = "${keycloak.issuerUrl}/protocol/openid-connect/logout",
            formParameters = parameters {
                append("client_id", keycloak.clientId)
                append("refresh_token", refreshToken)
                if (keycloak.clientSecret.isNotEmpty()) {
                    append("client_secret", keycloak.clientSecret)
                }
            }
        )
        call.respondText("Logged out")
    }

    // Достаём email из токена
    authenticate("keycloak-jwt") {
        get("/me") {
            val principal = call.principal<JWTPrincipal>()
            val email = principal!!.payload.getClaim("email").asString()
                ?: principal.payload.getClaim("preferred_username").asString()
            call.respondText("Your email is $email")
        }
    }
}