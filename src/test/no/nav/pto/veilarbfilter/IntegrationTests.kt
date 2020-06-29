package no.nav.pto.veilarbfilter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import no.nav.common.utils.NaisUtils
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.model.*
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
import java.time.LocalDateTime


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

        val newMineLagredeFilter = addMineLagredeFilter(nyttFilterModel)
        updateMineLagredeFilter(
            newMineLagredeFilter.filterId,
            FilterModel(
                filterId = newMineLagredeFilter.filterId,
                filterNavn = "New name",
                filterValg = newMineLagredeFilter.filterValg,
                opprettetDato = newMineLagredeFilter.opprettetDato
            )
        )
        val mineLagredeFilter = getMineLagredeFilter()

        assertTrue(mineLagredeFilter.get(0).filterId == 1)
        assertTrue(mineLagredeFilter.get(0).filterNavn == "New name")
        assertFalse(mineLagredeFilter.get(0).filterValg.ferdigfilterListe.isEmpty())
    }

    private fun getMineLagredeFilter(): List<MineLagredeFilterModel> {
        val request: HttpUriRequest = HttpGet("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/")
        val httpResponse = HttpClientBuilder.create().build().execute(request)
        val responseString = BasicResponseHandler().handleResponse(httpResponse)
        return deserializeLagredeFilterModels(responseString)
    }

    private fun addMineLagredeFilter(valgteFilter: NyttFilterModel): MineLagredeFilterModel {
        val httpclient = HttpClients.createDefault()
        val httpPost = HttpPost("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/")
        var valgteFilterModelJson = Gson().toJson(valgteFilter)
        httpPost.entity = StringEntity(valgteFilterModelJson, ContentType.APPLICATION_JSON)
        val httpResponse = httpclient.execute(httpPost)
        return deserializeLagredeFilterModel(BasicResponseHandler().handleResponse(httpResponse))
    }

    private fun updateMineLagredeFilter(filterId: Int, filterModel: FilterModel): MineLagredeFilterModel {
        val httpclient = HttpClients.createDefault()
        val httpPut = HttpPut("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/$filterId")
        var filterModelJson = serializeLagredeFilterModel(filterModel)
        httpPut.entity = StringEntity(filterModelJson, ContentType.APPLICATION_JSON)
        val httpResponse = httpclient.execute(httpPut)
        return deserializeLagredeFilterModel(BasicResponseHandler().handleResponse(httpResponse))
    }

    private fun deserializeLagredeFilterModels(inputJson: String): List<MineLagredeFilterModel> {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, DateSerializer())
            .create()
        return gson.fromJson(inputJson, Array<MineLagredeFilterModel>::class.java).toList()
    }

    private fun deserializeLagredeFilterModel(inputJson: String): MineLagredeFilterModel {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, DateSerializer())
            .create()
        return gson.fromJson(inputJson, MineLagredeFilterModel::class.java)
    }

    private fun serializeLagredeFilterModel(filterModel: FilterModel): String {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, DateSerializer())
            .create()
        return gson.toJson(filterModel)
    }


}
