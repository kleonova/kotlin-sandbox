package lev.learn.sandbox.harbor.connector.model

import io.ktor.utils.io.*
import kotlinx.coroutines.*
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.path
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondOutputStream
import io.ktor.utils.io.jvm.javaio.copyTo
import lev.learn.sandbox.harbor.connector.connector.HarborConnector

class DockerResponseChunked(
    private val firstResponse: DockerResponse,
    private val ranges: List<String>,
    private val baseRequest: DockerRequest.Blob,
    private val connector: HarborConnector
) : DockerResponse() {

    override fun statusCode(): Int = 206 // Partial Content

    override fun contentRangeOrNull(): Triple<Long, Long, Long>? = null

    override suspend fun bodyAsChannel(): ByteReadChannel {
        error("Unsupported")
    }

    override suspend fun body(): ByteReadChannel {
        // Создаем ByteChannel — канал для потоковой передачи байт
        val channel = ByteChannel(autoFlush = true)

        // Запускаем корутину для последовательного запроса чанков и записи их в канал
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (range in ranges) {
                    println("!! from body new request with $range")
                    // Копируем базовый запрос и добавляем Range-заголовок
                    val reqWithRange = baseRequest.copy(
                        headers = baseRequest.headers + DockerRequestHeader(HttpHeaders.Range, range)
                    )

                    val response = connector.requestBlob(reqWithRange)

                    if (response !is DockerResponseSimple) {
                        channel.close(IllegalStateException("Expected DockerResponseSimple"))
                        return@launch
                    }

                    if (response.statusCode() != HttpStatusCode.PartialContent.value) {
                        channel.close(IllegalStateException("Expected 206 Partial Content, got ${response.statusCode()}"))
                        return@launch
                    }

                    // Копируем поток из ответа в наш канал
                    response.body().copyTo(channel)

                    // Освобождаем ресурсы ответа
                    response.discard()
                }
                channel.close() // Успешно завершаем канал
            } catch (e: Throwable) {
                channel.close(e) // Закрываем канал с ошибкой
            }
        }

        return channel
    }

    override suspend fun respondTo(call: ApplicationCall) {
        call.respond(object : OutgoingContent.WriteChannelContent() {
            override val contentType = ContentType.Application.OctetStream
            override val status = HttpStatusCode.PartialContent

            override suspend fun writeTo(channel: ByteWriteChannel) {
                // 1. Отправляем первый чанк
                val firstBody = firstResponse.body()
                try {
                    firstBody.copyTo(channel)
                } finally {
                    firstBody.cancel() // освобождаем ресурсы
                }

                // 2. Дозагружаем остальные чанки
                for (range in ranges) {
                    println("!! DockerResponseChunked request $range for ${baseRequest.path}")

                    val reqWithRange = baseRequest.copy(
                        headers = baseRequest.headers + DockerRequestHeader(HttpHeaders.Range, range)
                    )

                    val response = connector.requestBlob(reqWithRange) as? DockerResponseSimple
                        ?: error("Expected DockerResponseSimple")

                    if (response.statusCode() != HttpStatusCode.PartialContent.value) {
                        error("Expected 206 Partial Content, got ${response.statusCode()}")
                    }

                    val chunkBody = response.body()
                    try {
                        chunkBody.copyTo(channel)
                    } finally {
                        chunkBody.cancel()
                        response.discard()
                    }
                }
            }
        })
    }
}
