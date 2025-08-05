package lev.learn.sandbox.harbor.connector.connector

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.request.get
import io.ktor.client.request.head
import kotlinx.coroutines.runBlocking
import lev.learn.sandbox.harbor.connector.model.HarborConnectorRequest
import lev.learn.sandbox.harbor.connector.model.HarborConnectorResponse
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

    fun requestHead(req: HarborConnectorRequest.Head): HarborConnectorResponse = runBlocking {
        val response = client.head("$harborUrl/v2/${req.path}")
        logger.info("Connector: HEAD ${req.path} | response ${response.status}")
        HarborConnectorResponse(response)
    }

    fun requestManifest(req: HarborConnectorRequest.Manifest): HarborConnectorResponse = runBlocking {
        val response = client.get("$harborUrl/v2/${req.path}")
        logger.info("Connector: GET manifest ${req.path} | status: ${response.status}")
        HarborConnectorResponse(response)
    }

    fun requestBlob(req: HarborConnectorRequest.Blob): HarborConnectorResponse = runBlocking {
        logger.info("Connector: start GET blob ${req.path}")
        try {
            val response = client.get("$harborUrl/v2/${req.path}")
            logger.info("Connector finish: GET blob ${req.path} | status: ${response.status}")
            HarborConnectorResponse(response, stream = true)
        } catch (e: Exception) {
            logger.error("Connector error: GET blob ${req.path} failed", e)
            throw e
        }
    }
}