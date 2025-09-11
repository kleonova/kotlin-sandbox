package lev.learn.sandbox.kafkaconnector

import ch.qos.logback.classic.Logger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread

class KafkaConnector(
    private val bootstrapServers: String,
    private val groupId: String
) {
    private val mapper = jacksonObjectMapper()

    companion object {
        val logger: Logger = LoggerFactory.getLogger(KafkaConnector::class.java) as Logger
    }


    private fun producerProps(): Properties = Properties().apply {
        put("bootstrap.servers", bootstrapServers)
        // как сериализовать ключ сообщения: Kafka хранит всё как байты, поэтому нужно превратить String → ByteArray
        put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    }

    private fun consumerProps(): Properties = Properties().apply {
        put("bootstrap.servers", bootstrapServers)
        put("group.id", groupId)
        put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        put("auto.offset.reset", "earliest") // читать сначала
    }

    private val producer by lazy { KafkaProducer<String, String>(producerProps()) }

    fun <T> send(topic: String, key: String? = null, message: T) {
        val json = mapper.writeValueAsString(message)
        val record = ProducerRecord(topic, key, json)
        producer.send(record)
        logger.info("SEND message to `$topic` topic with key=`$key` : ${message.toString()}")
    }

    fun <T> subscribe(topic: String, clazz: Class<T>, handler: (T) -> Unit) {
        val consumer = KafkaConsumer<String, String>(consumerProps())
        consumer.subscribe(listOf(topic))

        thread(start = true) {
            while (true) {
                val records = consumer.poll(Duration.ofMillis(500))
                for (record in records) {
                    val value = mapper.readValue(record.value(), clazz)
                    handler(value)
                }
            }
        }
    }
}
