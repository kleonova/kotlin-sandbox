package lev.learn.sandbox.harbor.connector.config

import io.ktor.client.plugins.HttpTimeout

data class HarborConfig(
    val baseUrl: String,
    val user: String,
    val password: String,
    val requestTimeoutMs: Long? = HttpTimeout.INFINITE_TIMEOUT_MS
)
