package no.nav.pto.veilarbfilter

import junit.framework.Assert.assertEquals
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.HttpClientBuilder
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet


class IntegrationTests {
    private var postgresqlContainer: PostgreSQLContainer<Nothing>;
    private var testApplication: MainTest

    constructor() {
        postgresqlContainer = PostgreSQLContainer<Nothing>("postgres:12-alpine").apply {
            withDatabaseName("veilarbfilter")
            withUsername("user")
            withPassword("password")
        }
        postgresqlContainer.start()

        testApplication =
            MainTest(postgresqlContainer.jdbcUrl, postgresqlContainer.username, postgresqlContainer.password);
        testApplication.start();
    }

    @After
    fun tearDown() {
        postgresqlContainer.stop()
        testApplication.stop()
    }

    @Test(timeout = 2000)
    fun testDatabaseConnection() {
        val conn: Connection = DriverManager
            .getConnection(postgresqlContainer.jdbcUrl, postgresqlContainer.username, postgresqlContainer.password)
        val resultSet: ResultSet = conn.createStatement().executeQuery("SELECT 1")
        resultSet.next()
        val result = resultSet.getInt(1)

        assertEquals(1, result)
    }

    @Test(timeout = 2000)
    fun testSavingMineFilter() {
        val request: HttpUriRequest = HttpGet("http://0.0.0.0:8080/veilarbfilter/internal/isAlive")
        val httpResponse = HttpClientBuilder.create().build().execute(request)
        Assert.assertTrue(httpResponse.statusLine.statusCode == 200)
    }

}
