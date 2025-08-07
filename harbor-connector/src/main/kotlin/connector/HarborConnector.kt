package lev.learn.sandbox.harbor.connector.connector

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import lev.learn.sandbox.harbor.connector.config.ConfigLoader
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.model.DockerRequestHeader
import lev.learn.sandbox.harbor.connector.response.DockerResponse
import lev.learn.sandbox.harbor.connector.response.DockerResponseBase
import org.slf4j.LoggerFactory

class HarborConnector {
    private companion object {
        private val config by lazy { ConfigLoader.loadHarborConfig() }

        private val harborUrl = config.baseUrl
        private val harborLogin = config.user
        private val harborPassword = config.password
        private val requestTimeout = config.requestTimeoutMs
        private val maxRetries = config.maxRetries
        private val delayBetweenRetriesMs: Long = config.delayBetweenRetriesMs
    }


    private val logger = LoggerFactory.getLogger("HarborConnector")
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = requestTimeout // 0 = бесконечно, HttpTimeout.INFINITE_TIMEOUT_MS
            connectTimeoutMillis = 30_000 // подключение 30 сек
            socketTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS // читаем сколько угодно
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

                if (status in listOf(400, 401, 403, 404)) {
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

    fun requestHead(req: DockerRequest.Head): DockerResponse = runBlocking {
        executeRequest(req, HttpMethod.Head, "HEAD") {
            withHeaders(req.headers)
        }
    }

    fun requestManifest(req: DockerRequest.Manifest): DockerResponse = runBlocking {
        executeRequest(req, HttpMethod.Get, "GET manifest") {
            withHeaders(req.headers)
        }
    }

    fun requestBlob(req: DockerRequest.Blob): DockerResponse = runBlocking {
        val headers = req.headers
            .filterNot { it.key == HttpHeaders.AcceptEncoding }

        executeRequest(req.copy(headers = headers), HttpMethod.Get, "GET blob") {
            withHeaders(req.headers)
        }
    }

    private fun shouldRetry(e: Throwable): Boolean {
        return e is IOException || e is TimeoutCancellationException
    }
}
