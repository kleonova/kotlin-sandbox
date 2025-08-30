package lev.learn.sandbox.harbor.connector.response

import io.ktor.utils.io.*
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import lev.learn.sandbox.harbor.connector.config.ConfigLoader
import lev.learn.sandbox.harbor.connector.connector.HarborConnector
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.model.DockerRequestHeader
import org.slf4j.LoggerFactory
import kotlin.collections.plus

class DockerResponseChunked(
    private val firstResponse: DockerResponse,
    private val ranges: List<String>,
    private val baseRequest: DockerRequest.Blob,
    private val connector: HarborConnector
) : DockerResponse() {
    private val logger = LoggerFactory.getLogger("DockerResponseChunked")

    private companion object {
        private val config by lazy { ConfigLoader.loadChunkConfig() }
        private val prefetchCount = config.prefetchCount
    }

    override fun statusCode(): Int = 206 // Partial Content

    override fun contentRangeOrNull(): Triple<Long, Long, Long>? = null

    override suspend fun body(): ByteReadChannel {
        error("Unsupported")
    }

    override suspend fun respondTo(call: ApplicationCall) {
        call.respond(object : OutgoingContent.WriteChannelContent() {
            override val contentType = ContentType.Application.OctetStream
            override val status = HttpStatusCode.PartialContent

            override suspend fun writeTo(channel: ByteWriteChannel) {
                coroutineScope {
                    // Отправляем первую порцию данных (синхронно)
                    val firstBody = firstResponse.body()
                    try {
                        firstBody.copyTo(channel)
                    } finally {
                        firstBody.cancel() // Освобождаем ресурсы канала
                    }

                    // Создаём поток порций (асинхронно)
                    val chunkFlow = flow {
                        ranges.forEach { range ->
                            logger.debug("Запрашиваем порцию: $range для ${baseRequest.path}")
                            val reqWithRange = baseRequest.copy(
                                headers = baseRequest.headers + DockerRequestHeader(HttpHeaders.Range, range)
                            )

                            // Запускаем запрос асинхронно
                            val deferred = async {
                                val response = connector.requestBlob(reqWithRange) as? DockerResponseBase
                                    ?: error("Expected DockerResponseBase")

                                if (response.statusCode() != HttpStatusCode.PartialContent.value) {
                                    error("Expected 206 Partial Content, got ${response.statusCode()}")
                                }

                                Pair(range, response)
                            }

                            emit(deferred)
                        }
                    }
                        .buffer(prefetchCount)
                        .mapNotNull { deferred ->
                            runCatching {
                                deferred.await().let { (range, response) ->
                                    logger.debug("Порция получена: $range для ${baseRequest.path}")
                                    response to response.body()
                                }
                            }.onFailure { ex ->
                                logger.error("Ошибка при загрузке порции для ${baseRequest.path}: $ex")
                                // todo Можно попробовать повторить или прервать
                                throw ex
                            }.getOrNull()
                        }

                    // Потребляем и пишем
                    chunkFlow.cancellable().collect { (response, body) ->
                        try {
                            body.copyTo(channel)
                        } finally {
                            body.cancel()
                            response.discard()
                        }
                        logger.debug("Порция для ${baseRequest.path} записана, ресурсы освобождены")
                    }
                }
            }
        })
    }
}
