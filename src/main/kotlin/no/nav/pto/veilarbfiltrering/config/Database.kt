package no.nav.pto.veilarbfiltrering.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.flywaydb.core.Flyway
import no.nav.pto.veilarbfiltrering.config.Configuration

class Database (private val configuration: Configuration) {

    private val dbUrl = configuration.database.url
    private val dbUser = configuration.database.username
    private val dbPassword = configuration.database.password;

    fun initLocal() {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = dbUrl
        config.username = dbUser
        config.password = dbPassword
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        Database.connect(HikariDataSource(config))
        val flyway = Flyway.configure().dataSource(dbUrl, dbUser, dbPassword).load()
        flyway.migrate()
    }

    fun initRemote() {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = dbUrl
        config.username = dbUser
        config.password = dbPassword
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        Database.connect(HikariDataSource(config))
        val flyway = Flyway.configure().dataSource(dbUrl, dbUser, dbPassword).load()
        flyway.migrate()
    }

}

suspend fun <T> dbQuery(block: () -> T): T =
    withContext(Dispatchers.IO) {
        transaction { block() }
    }