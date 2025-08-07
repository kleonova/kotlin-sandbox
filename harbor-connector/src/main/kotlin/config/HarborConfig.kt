package lev.learn.sandbox.harbor.connector.config

import io.ktor.client.plugins.HttpTimeout

data class HarborConfig(
    val baseUrl: String,
    val user: String,
    val password: String,
    val requestTimeoutMs: Long? = HttpTimeout.INFINITE_TIMEOUT_MS,
    val delayBetweenRetriesMs: Long,
    val maxRetries: Int
) {
    companion object {
        val DEFAULT_DELAY_BETWEEN_RETRIES_MS = 1000L
        val DEFAULT_MAX_RETRIES = 3
    }
}
