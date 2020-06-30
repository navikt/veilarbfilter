package no.nav.pto.veilarbfilter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import no.nav.common.utils.NaisUtils
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.model.*
import org.apache.http.client.methods.*
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.junit.Assert
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import java.time.LocalDateTime


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTestsMineFilter {
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
    fun testLagreLagredeFilterGylig() {

    }

    @Test
    fun testLagreLagredeFilterUgylig() {

    }

    @Test
    fun testOppdaterLagredeFilterGyldig() {
        //add new filter
        val nyttFilterModel =
            NyttFilterModel(
                filterNavn = "Test",
                filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K")
            )

        val lagretMineLagredeFilterResponse = lagreMineLagredeFilter(nyttFilterModel)

        Assert.assertTrue(lagretMineLagredeFilterResponse.responseCode == 200)
        val lagretMineLagredeFilter = lagretMineLagredeFilterResponse.responseValue

        //update filter
        val oppdaterMineLagredeFilterResponse = oppdaterMineLagredeFilter(
            lagretMineLagredeFilter.filterId,
            FilterModel(
                filterId = lagretMineLagredeFilter.filterId,
                filterNavn = "New name",
                filterValg = lagretMineLagredeFilter.filterValg,
                opprettetDato = lagretMineLagredeFilter.opprettetDato
            )
        )

        Assert.assertTrue(oppdaterMineLagredeFilterResponse.responseCode == 200)

        //get all saved filters
        val mineLagredeFilterResponse = getMineLagredeFilter()

        Assert.assertTrue(mineLagredeFilterResponse.responseCode == 200)
        val mineLagredeFilterList = mineLagredeFilterResponse.responseValue
        val oppdatertFilter =
            mineLagredeFilterList.filter { x -> x.filterId == lagretMineLagredeFilter.filterId }.first()

        assertTrue(oppdatertFilter.filterNavn == "New name")
        assertFalse(oppdatertFilter.filterValg.ferdigfilterListe.isEmpty())
    }

    fun testOppdaterLagredeFilterUgyldig() {

    }

    @Test
    fun testTomtNavn() {
        val nyttFilterModel =
            NyttFilterModel(
                filterNavn = "",
                filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K")
            )

        val newMineLagredeFilter = lagreMineLagredeFilter(nyttFilterModel)
        //TODO: add validation for saving filter, and expect exception in case when name  is empty
    }

    @Test
    fun testTomtFilterValg() {
        val nyttFilterModel =
            NyttFilterModel(
                filterNavn = "Nytt filter",
                filterValg = PortefoljeFilter()
            )

        val newMineLagredeFilter = lagreMineLagredeFilter(nyttFilterModel)
        //TODO: add validation for saving filter, and expect exception in case when valgt filter are empty
    }

    @Test
    fun testSlettLagretFilterGyldig() {
        val nyttFilterModel =
            NyttFilterModel(
                filterNavn = "Test",
                filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K")
            )

        val lagretMineLagredeFilterResponse = lagreMineLagredeFilter(nyttFilterModel)
        Assert.assertTrue(lagretMineLagredeFilterResponse.responseCode == 200)
        val lagretMineLagredeFilter = lagretMineLagredeFilterResponse.responseValue

        val responseCode = deleteMineLagredeFilter(lagretMineLagredeFilter.filterId, lagretMineLagredeFilter.veilederId)
        Assert.assertTrue(responseCode == 200)

        val mineLagredeFilterResponse = getMineLagredeFilter()
        val mineLagredeFilter = mineLagredeFilterResponse.responseValue
        Assert.assertTrue(mineLagredeFilter.filter { x -> x.filterId == lagretMineLagredeFilter.filterId }.count() == 0)
    }

    private fun testSlettLagretFilterUgyldig() {
        //todo: try to delete filter that doesnt belong to veileder, check error code
    }

    private fun getMineLagredeFilter(): ApiResponse<List<MineLagredeFilterModel>> {
        val request: HttpUriRequest = HttpGet("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/")
        val httpResponse = HttpClientBuilder.create().build().execute(request)
        val responseString = BasicResponseHandler().handleResponse(httpResponse)
        return ApiResponse(httpResponse.statusLine.statusCode, deserializeLagredeFilterModels(responseString))
    }

    private fun lagreMineLagredeFilter(valgteFilter: NyttFilterModel): ApiResponse<MineLagredeFilterModel> {
        val httpclient = HttpClients.createDefault()
        val httpPost = HttpPost("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/")
        var valgteFilterModelJson = Gson().toJson(valgteFilter)
        httpPost.entity = StringEntity(valgteFilterModelJson, ContentType.APPLICATION_JSON)
        val httpResponse = httpclient.execute(httpPost)
        return ApiResponse(
            httpResponse.statusLine.statusCode,
            deserializeLagredeFilterModel(BasicResponseHandler().handleResponse(httpResponse))
        )
    }

    private fun oppdaterMineLagredeFilter(
        filterId: Int,
        filterModel: FilterModel
    ): ApiResponse<MineLagredeFilterModel> {
        val httpclient = HttpClients.createDefault()
        val httpPut = HttpPut("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/$filterId")
        var filterModelJson = serializeLagredeFilterModel(filterModel)
        httpPut.entity = StringEntity(filterModelJson, ContentType.APPLICATION_JSON)
        val httpResponse = httpclient.execute(httpPut)
        return ApiResponse(
            httpResponse.statusLine.statusCode,
            deserializeLagredeFilterModel(BasicResponseHandler().handleResponse(httpResponse))
        )
    }

    private fun deleteMineLagredeFilter(filterId: Int, veilederId: String): Int {
        val httpclient = HttpClients.createDefault()
        val httpPut = HttpDelete("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/$veilederId/filter/$filterId")
        val httpResponse = httpclient.execute(httpPut)
        return httpResponse.statusLine.statusCode
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
