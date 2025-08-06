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
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.model.DockerRequestHeader
import lev.learn.sandbox.harbor.connector.model.DockerResponse
import lev.learn.sandbox.harbor.connector.model.DockerResponseSimple
import org.slf4j.LoggerFactory

class HarborConnector {
    private val harborUrl = "http://harbor.local:8088"
    private val harborLogin = "admin"
    private val harborPassword = "Harbor12345"

    private val logger = LoggerFactory.getLogger("DockerConnector")
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS // 0 = бесконечно
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


            DockerResponseSimple(response, stream = (request is DockerRequest.Blob))
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
