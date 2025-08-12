package lev.learn.sandbox.auth.service.database

import org.flywaydb.core.Flyway
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {

    private var dataSource: HikariDataSource? = null

    // Создаём или возвращаем существующий DataSource
    fun getDataSource(config: ApplicationConfig): HikariDataSource {
        if (dataSource == null) {
            dataSource = createDataSource(config)
        }
        return dataSource!!
    }

    // Подключаем Exposed к HikariCP
    fun init(config: ApplicationConfig) {
        val hikariDataSource = getDataSource(config)
        Database.connect(hikariDataSource)

        // Запускаем миграции
        runMigrations(config)
    }

    fun close() {
        dataSource?.close()
        dataSource = null
    }

    private fun createDataSource(config: ApplicationConfig): HikariDataSource {
        val dbConfig = config.config("database")

        return HikariConfig().apply {
            driverClassName = getConfig(dbConfig, "driver")
            jdbcUrl = getConfig(dbConfig, "url")
            username = getConfig(dbConfig, "user")
            password = getConfig(dbConfig, "password")
            maximumPoolSize = dbConfig.propertyOrNull("maximumPoolSize")?.getString()?.toIntOrNull() ?: 20
            isAutoCommit = dbConfig.propertyOrNull("autoCommit")?.getString()?.toBoolean() ?: false
            transactionIsolation = getConfig(dbConfig, "transactionIsolation")
            validate()
        }.let(::HikariDataSource)
    }

    private fun runMigrations(config: ApplicationConfig) {
        val hikariDataSource = getDataSource(config)
        val flyway = Flyway.configure()
            .dataSource(hikariDataSource)
            .locations("classpath:db/migration")
            .load()
        flyway.migrate()
    }

    private fun getConfig(config: ApplicationConfig, path: String): String {
        return config.property(path).getString()
    }
}