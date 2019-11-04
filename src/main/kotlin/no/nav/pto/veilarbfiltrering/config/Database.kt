package no.nav.pto.veilarbfiltrering.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.flywaydb.core.Flyway
import io.ktor.config.HoconApplicationConfig
import com.typesafe.config.ConfigFactory
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil

object Database {

    private val appConfig = HoconApplicationConfig(ConfigFactory.load())
    private val dbUrl = appConfig.property("db.jdbcUrl").getString()
    private val dbUser = appConfig.property("db.dbUser").getString()
    private val dbPassword = appConfig.property("db.dbPassword").getString()

    fun init() {
        Database.connect(hikari())
        val flyway = Flyway.configure().dataSource(dbUrl, dbUser, dbPassword).initSql(String.format("SET ROLE \"%s\"", dbRole("admin"))).load()
        flyway.migrate()
    }

    private fun hikari(user: String): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = dbUrl
        config.minimumIdle = 1
        config.maximumPoolSize = 3
        val mountPath = if (getEnvironmentClass() === P) "postgresql/prod-fss" else "postgresql/preprod-fss"
        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, mountPath, dbRole(user))
    }

    private fun dbRole(role: String): String {
        return if (getEnvironmentClass() === P)
            arrayOf(APPLICATION_NAME, role).joinToString("-")
        else
            arrayOf(APPLICATION_NAME, requireEnvironmentName(), role).joinToString("-")
    }


    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }
}
