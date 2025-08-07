package lev.learn.sandbox.harbor.connector.config

import com.typesafe.config.ConfigFactory

object ConfigLoader {
    private val config = ConfigFactory.load()

    fun loadHarborConfig(): HarborConfig {
        val harborConfig = config.getConfig("harbor")

        return HarborConfig(
            baseUrl = harborConfig.getString("baseUrl"),
            user = harborConfig.getString("user"),
            password = harborConfig.getString("password"),
            requestTimeoutMs = if (harborConfig.hasPath("requestTimeoutMs")) {
                harborConfig.getLong("requestTimeoutMs")
            } else {
                null // используем значение по умолчанию из data class
            }
        )
    }
    
    fun loadChunkConfig(): ChunkConfig {
        val chunkConfig = config.getConfig("chunk")
        
        return ChunkConfig(
            prefetchCount = chunkConfig.getInt("prefetchCount"),
            sizeBytes = chunkConfig.getLong("sizeBytes")
        )
    }
}