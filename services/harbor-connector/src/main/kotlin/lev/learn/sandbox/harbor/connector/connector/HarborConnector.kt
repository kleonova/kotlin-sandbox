package lev.learn.sandbox.harbor.connector.connector

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import lev.learn.sandbox.harbor.connector.config.ConfigLoader
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.model.DockerRequestHeader
import lev.learn.sandbox.harbor.connector.response.ConnectorResponse
import lev.learn.sandbox.harbor.connector.response.DockerResponse
import lev.learn.sandbox.harbor.connector.response.DockerResponseBase
import org.slf4j.LoggerFactory

class HarborConnector {
    private companion object {
        const val CONNECTION_TIMEOUT_MS = 30_000L
        private val config by lazy { ConfigLoader.loadHarborConfig() }

        private val harborUrl = config.baseUrl
        private val harborLogin = config.user
        private val harborPassword = config.password
        private val requestTimeout = config.requestTimeoutMs
        private val maxRetries = config.maxRetries
        private val delayBetweenRetriesMs: Long = config.delayBetweenRetriesMs

        val FORBIDDEN_HTTP_STATUSES = listOf(
            HttpStatusCode.BadRequest.value,
            HttpStatusCode.Unauthorized.value,
            HttpStatusCode.Forbidden.value,
            HttpStatusCode.NotFound.value
        )
    }

    private val logger = LoggerFactory.getLogger("HarborConnector")
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = requestTimeout // 0 = бесконечно, HttpTimeout.INFINITE_TIMEOUT_MS
            connectTimeoutMillis = CONNECTION_TIMEOUT_MS // подключение 30 сек
            socketTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS // читаем сколько угодно
        }

        install(Auth) {
            basic {
                sendWithoutRequest { true }
                credentials {
                    BasicAuthCredentials(harborLogin, harborPassword)
                }
            }
        }
    }

    // Обобщённый метод для выполнения запроса
    private suspend fun <T : DockerRequest> executeRequest(
        request: T,
        method: HttpMethod,
        actionName: String,
        configure: HttpRequestBuilder.() -> Unit = {}
    ): DockerResponse {
        val fullPath = "$harborUrl/v2/${request.path}"
        val logPrefix = "Connector: $actionName $fullPath"

        var attempt = 0

        while (true) {
            try {
                logger.info("$logPrefix | attempt ${attempt + 1}")

                val response: HttpResponse = when (method) {
                    HttpMethod.Get -> client.get(fullPath, configure)
                    HttpMethod.Head -> client.head(fullPath, configure)
                    else -> throw IllegalArgumentException("Unsupported HTTP method: $method")
                }

                val status = response.status.value
                logger.info("$logPrefix | finished with status: $status")

                response.headers.forEach { key, values ->
                    logger.debug("Response header: $key - ${values.joinToString(",")}")
                }

                if (status in FORBIDDEN_HTTP_STATUSES) {
                    logger.error("$logPrefix | client error: $status")
                    throw ClientRequestException(response, "") // выбрасываем дальше
                }

                return DockerResponseBase(response, stream = (request is DockerRequest.Blob))

            } catch (e: ClientRequestException) {
                // Ошибки клиента — не повторяем
                logger.error("$logPrefix | client error (won't retry)", e)
                throw e
            } catch (e: Exception) {
                if (!shouldRetry(e) || attempt >= maxRetries) {
                    logger.error("$logPrefix | failed after ${attempt + 1} attempts", e)
                    throw e
                }

                logger.warn("$logPrefix | transient error, retrying in ${delayBetweenRetriesMs}ms...", e)
                delay(delayBetweenRetriesMs)
                attempt++
            }
        }
    }

    // Настройка заголовков + логирование
    private fun HttpRequestBuilder.withHeaders(headers: List<DockerRequestHeader>) {
        headers.forEach { header ->
            val logMsg = "${header.key} - ${header.value}"
            when (header.key.lowercase()) {
                "authorization" -> logger.debug("Request header (hidden): ${header.key} - ***")
                else -> logger.debug("Request header: $logMsg")
            }
            header(header.key, header.value)
        }
    }

    fun requestHead(request: DockerRequest.Head): DockerResponse = runBlocking {
        executeRequest(request, HttpMethod.Head, "HEAD") {
            withHeaders(request.headers)
        }
    }

    fun requestManifest(request: DockerRequest.Manifest): DockerResponse = runBlocking {
        executeRequest(request, HttpMethod.Get, "GET manifest") {
            withHeaders(request.headers)
        }
    }

    fun requestBlob(request: DockerRequest.Blob): DockerResponse = runBlocking {
        val headers = request.headers
            .filterNot { it.key == HttpHeaders.AcceptEncoding }

        executeRequest(request.copy(headers = headers), HttpMethod.Get, "GET blob") {
            withHeaders(request.headers)
        }
    }

    suspend fun getRange(request: DockerRequest.Blob): ConnectorResponse {
        logger.info("Connector → запрос ${request.path}")

        val fullPath = "$harborUrl/v2/${request.path}"

        val response: HttpResponse = client.get(fullPath) {
            withHeaders(request.headers)
        }

        logger.info("Connector → статус=${response.status}, path=${request.path}")
        response.headers.forEach { key, values ->
            logger.debug("Response header: $key - ${values.joinToString(",")}")
        }

        return ConnectorResponse(
            channel = response.bodyAsChannel(),
            headers = response.headers
        )
    }

    private fun shouldRetry(e: Throwable): Boolean {
        return e is IOException || e is TimeoutCancellationException
    }
}
