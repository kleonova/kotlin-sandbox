package lev.learn.sandbox.auth.service.database

import org.flywaydb.core.Flyway
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig

object DatabaseFactory {
    fun createDataSource(config: ApplicationConfig): HikariDataSource {
        val dbConfig = config.config("database")

        val config = HikariConfig().apply {
            driverClassName = getConfig(dbConfig, "driver")
            jdbcUrl = getConfig(dbConfig, "url")
            username = getConfig(dbConfig, "user")
            password = getConfig(dbConfig, "password")
            maximumPoolSize = 20
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }

    fun migrateDatabase(config: ApplicationConfig) {
        val dataSource = createDataSource(config)
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
        flyway.migrate()
    }

    private fun getConfig(config: ApplicationConfig, path: String): String {
        return config.property(path).getString()
    }
}