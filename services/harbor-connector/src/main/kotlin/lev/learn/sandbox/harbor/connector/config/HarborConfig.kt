package lev.learn.sandbox.harbor.connector.config

import io.ktor.client.plugins.HttpTimeoutConfig

data class HarborConfig(
    val baseUrl: String,
    val user: String,
    val password: String,
    val requestTimeoutMs: Long? = HttpTimeoutConfig.INFINITE_TIMEOUT_MS,
    val delayBetweenRetriesMs: Long = DEFAULT_DELAY_BETWEEN_RETRIES_MS,
    val maxRetries: Int = DEFAULT_MAX_RETRIES
) {
    companion object {
        const val DEFAULT_DELAY_BETWEEN_RETRIES_MS = 1000L
        const val DEFAULT_MAX_RETRIES = 3
    }
}
