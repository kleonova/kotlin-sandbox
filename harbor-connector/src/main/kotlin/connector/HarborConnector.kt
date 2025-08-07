package lev.learn.sandbox.harbor.connector.connector

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
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
    // todo доработать механизм обработки ошибок с Retry
    //  если запрос выполнен с ошибкой 400, 401, 403, 404 - записать лог и отдать ошибку
    //  если запрос выполнен с прерываением, то сделать повторный запрос - ввести переменную ограничивающую число повторов
    private suspend fun <T : DockerRequest> executeRequest(
        request: T,
        method: HttpMethod,
        actionName: String,
        configure: HttpRequestBuilder.() -> Unit = {}
    ): DockerResponse {
        val fullPath = "$harborUrl/v2/${request.path}"
        val logPrefix = "Connector: $actionName $fullPath"

        return try {
            logger.info("$logPrefix | initiating request")

            val response: HttpResponse = when (method) {
                HttpMethod.Get -> client.get(fullPath, configure)
                HttpMethod.Head -> client.head(fullPath, configure)
                else -> throw IllegalArgumentException("Unsupported HTTP method: $method")
            }

            logger.info("$logPrefix | finished with status: ${response.status}")

            response.headers.forEach { key, values ->
                logger.debug("Response header: $key - ${values.joinToString(",")}")
            }


            DockerResponseBase(response, stream = (request is DockerRequest.Blob))
        } catch (e: Exception) {
            logger.error("$logPrefix failed", e)
            throw e
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
}
