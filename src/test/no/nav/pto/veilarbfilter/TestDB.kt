package no.nav.pto.veilarbfilter

import junit.framework.Assert.assertEquals
import org.flywaydb.core.Flyway
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet


class DBTest {
    private lateinit var postgresqlContainer: PostgreSQLContainer<Nothing>;

    @Before
    fun setUp() {
        postgresqlContainer = PostgreSQLContainer<Nothing>("postgres:12-alpine")
        postgresqlContainer.start()
        val flyway =
            Flyway.configure()
                .dataSource(postgresqlContainer.jdbcUrl, postgresqlContainer.username, postgresqlContainer.password)
                .load()
        flyway.migrate()
    }

    @After
    fun tearDown() {
        postgresqlContainer.stop()
    }

    @Test
    fun someTestMethod() {
        val conn: Connection = DriverManager
            .getConnection(postgresqlContainer.jdbcUrl, postgresqlContainer.username, postgresqlContainer.password)
        val resultSet: ResultSet = conn.createStatement().executeQuery("SELECT 1")
        resultSet.next()
        val result = resultSet.getInt(1)

        assertEquals(1, result)
    }
}
