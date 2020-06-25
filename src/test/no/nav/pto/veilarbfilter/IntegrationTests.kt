package no.nav.pto.veilarbfilter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import no.nav.common.utils.NaisUtils
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.MineLagredeFilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.model.PortefoljeFilter
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
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
class IntegrationTests {
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
            serviceUser = NaisUtils.Credentials("foo", "bar"),
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

        main(configuration)
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

    @Test
    fun testSavingmineLagredeFilter() {
        val nyttFilterModel =
            NyttFilterModel(
                filterNavn = "Test",
                filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K")
            )

        addMineLagredeFilter(nyttFilterModel)
        updateMineLagredeFilter(
            1,
            FilterModel(
                filterId = 1,
                filterNavn = "New name",
                filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K"),
                opprettetDato = null
            )
        )
        val mineLagredeFilter = getmineLagredeFilter()

        assertTrue(mineLagredeFilter.get(0).filterId == 1)
        assertTrue(mineLagredeFilter.get(0).filterNavn == "Test")
        assertFalse(mineLagredeFilter.get(0).filterValg.ferdigfilterListe.isEmpty())
    }

    private fun getmineLagredeFilter(): List<MineLagredeFilterModel> {
        val request: HttpUriRequest = HttpGet("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/")
        val httpResponse = HttpClientBuilder.create().build().execute(request)
        val responseString = BasicResponseHandler().handleResponse(httpResponse)

        val listType =
            object : TypeToken<List<MineLagredeFilterModel?>?>() {}.type;
        return Gson().fromJson(
            responseString,
            listType
        )
    }

    private fun addMineLagredeFilter(valgteFilter: NyttFilterModel): Boolean {
        val httpclient = HttpClients.createDefault()
        val httpPost = HttpPost("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/")
        var valgteFilterModelJson = Gson().toJson(valgteFilter)
        val stringEntity: HttpEntity = StringEntity(valgteFilterModelJson, ContentType.APPLICATION_JSON)
        httpPost.entity = stringEntity
        val response = httpclient.execute(httpPost)
        return response.statusLine.statusCode == 200;
    }

    private fun updateMineLagredeFilter(filterId: Int, filterModel: FilterModel): Boolean {
        val httpclient = HttpClients.createDefault()
        val httpPut = HttpPut("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/" + filterId)
        var filterModelJson = Gson().toJson(filterModel)
        val stringEntity: HttpEntity = StringEntity(filterModelJson, ContentType.APPLICATION_JSON)
        httpPut.entity = stringEntity
        val response = httpclient.execute(httpPut)
        return response.statusLine.statusCode == 200;
    }


}
