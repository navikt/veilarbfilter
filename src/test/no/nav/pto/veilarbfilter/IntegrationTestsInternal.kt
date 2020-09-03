package no.nav.pto.veilarbfilter

import no.nav.common.utils.Credentials
import no.nav.pto.veilarbfilter.config.Configuration
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTestsInternal {
    private
    lateinit var postgresqlContainer: PostgreSQLContainer<Nothing>;

    @BeforeAll
    internal fun setUp() {
        postgresqlContainer = PostgreSQLContainer<Nothing>("postgres:12-alpine").apply {
            withDatabaseName("veilarbfilter")
            withUsername("user")
            withPassword("password")
        }
        postgresqlContainer.start()

        val configuration = Configuration(
                clustername = "",
                serviceUser = Credentials("foo", "bar"),
                abac = Configuration.Abac(""),
                veilarbveilederConfig = Configuration.VeilarbveilederConfig(""),
                database = Configuration.DB(
                        url = postgresqlContainer.jdbcUrl,
                        username = postgresqlContainer.username,
                        password = postgresqlContainer.password
                ),
                httpServerWait = false,
                useAuthentication = false
        )

        mainTest(configuration)
    }

    @AfterAll
    fun tearDown() {
        postgresqlContainer.stop()
    }

    @Test
    fun testDatabaseConnection() {
        val conn: Connection = DriverManager
                .getConnection(postgresqlContainer.jdbcUrl, postgresqlContainer.username, postgresqlContainer.password)
        val resultSet: ResultSet = conn.createStatement().executeQuery("SELECT 1")
        resultSet.next()
        val result = resultSet.getInt(1)

        assertEquals(1, result)
    }

    @Test
    fun testIsAlive() {
        val request: HttpUriRequest = HttpGet("http://0.0.0.0:8080/veilarbfilter/internal/isAlive")
        val httpResponse = HttpClientBuilder.create().build().execute(request)
        val responseString = BasicResponseHandler().handleResponse(httpResponse)
        assertTrue(httpResponse.statusLine.statusCode == 200)
        assertTrue(responseString.equals("Alive"))
    }

    @Test
    fun testIsReady() {
        val request: HttpUriRequest = HttpGet("http://0.0.0.0:8080/veilarbfilter/internal/isReady")
        val httpResponse = HttpClientBuilder.create().build().execute(request)
        val responseString = BasicResponseHandler().handleResponse(httpResponse)
        assertTrue(httpResponse.statusLine.statusCode == 200)
        assertTrue(responseString.equals("Ready"))
    }

    @Test
    fun testMetrics() {
        val request: HttpUriRequest = HttpGet("http://0.0.0.0:8080/veilarbfilter/internal/metrics")
        val httpResponse = HttpClientBuilder.create().build().execute(request)
        val responseString = BasicResponseHandler().handleResponse(httpResponse)
        assertTrue(httpResponse.statusLine.statusCode == 200)
        assertFalse(responseString.isEmpty())
    }

}
