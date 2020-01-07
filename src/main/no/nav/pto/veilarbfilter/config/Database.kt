package no.nav.pto.veilarbfilter.config

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.flywaydb.core.Flyway
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil

class Database (configuration: Configuration) {
    private val APPLICATION_NAME = "veilarbfilter"

    private val dbUrl = configuration.database.url
    private val mountPath = configuration.database.vaultMountPath
    private val naisClustername = configuration.clustername

    init {
        when (naisClustername) {
            "" -> initLocal()
            else -> initRemote()
        }
    }

    fun initLocal() {

        val embeddedPostgres = EmbeddedPostgres.builder().start()
        val pg = "postgres"
        val jdbcUrl = embeddedPostgres.getJdbcUrl(pg, pg)

        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = jdbcUrl
        config.username = pg
        config.password = pg
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        Database.connect(HikariDataSource(config))

        val flyway = Flyway.configure().dataSource(jdbcUrl, pg, pg).load()
        flyway.migrate()
    }

    fun initRemote () {
        val adminDataSource = dataSource("admin")
        migrateDatabase(adminDataSource)
        adminDataSource.close()
        Database.connect(dataSource("user"))
    }

    fun migrateDatabase(dataSource: HikariDataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .initSql(String.format("SET ROLE \"%s\"", dbRole("admin")))
            .load()
            .migrate()
    }

    private fun dataSource(user: String): HikariDataSource {
        val config = HikariConfig()
        config.jdbcUrl = dbUrl
        config.maximumPoolSize = 3
        config.minimumIdle = 1
        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, mountPath, dbRole(user))
    }

    private fun dbRole(role: String): String {
        return arrayOf(APPLICATION_NAME, role).joinToString("-")
    }

}

suspend fun <T> dbQuery(block: () -> T): T =
    withContext(Dispatchers.IO) {
        transaction { block() }
    }
