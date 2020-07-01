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
import org.apache.http.util.EntityUtils
import org.junit.Assert
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTestsMineFilter {
    private
    lateinit var postgresqlContainer: PostgreSQLContainer<Nothing>;

    val filterModel =
        NyttFilterModel(
            filterNavn = "Test",
            filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K")
        )

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

    /** TESTER RELATERT TIL GYLDIGHET FOR LAGRING AV NYTT FILTER **/
    @Test
    fun testLagreLagredeFilterGyldig() {
        val mineLagredeFilterResponse = getMineLagredeFilter()

        lagreNyttFilterRespons(filterModel)
        val mineLagredeFilterNyResponsEtterLagring = getMineLagredeFilter()

        Assert.assertTrue(mineLagredeFilterResponse.responseValue.size < mineLagredeFilterNyResponsEtterLagring.responseValue.size)
    }

    /** TESTER RELATERT TIL UGYLDIGHET FOR LAGRING AV NYTT FILTER **/
    @Test
    fun testLagretFilterNavnEksistererForNyttFilter() {
        lagreNyttFilterRespons(filterModel)
        val mineLagredeFilterResponse = getMineLagredeFilter()

        val nyttFilterModelEksisterendeNavn =
            NyttFilterModel(
                filterNavn = filterModel.filterNavn,
                filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "M")
            )

        lagreNyttFilterRespons(nyttFilterModelEksisterendeNavn)
        val mineLagredeFilterResponseEtterFeilLagring = getMineLagredeFilter()

        Assert.assertTrue(mineLagredeFilterResponse.responseValue.size == mineLagredeFilterResponseEtterFeilLagring.responseValue.size)
    }

    @Test
    fun testLagretFilterValgEksistererForNyttFilter() {
        lagreNyttFilterRespons(filterModel)
        val mineLagredeFilterResponse = getMineLagredeFilter()

        val nyttFilterModelEksisterendeFilter =
            NyttFilterModel(
                filterNavn = "Blablaba",
                filterValg = filterModel.filterValg
            )

        lagreNyttFilterRespons(nyttFilterModelEksisterendeFilter)
        val mineLagredeFilterResponseEtterFeilLagring = getMineLagredeFilter()

        Assert.assertTrue(mineLagredeFilterResponse.responseValue.size == mineLagredeFilterResponseEtterFeilLagring.responseValue.size)
    }

    @Test
    fun testTomtNavnForNyttLagretFilter() {
        val nyttFilterModel =
            NyttFilterModel(
                filterNavn = "",
                filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K")
            )

        val newMineLagredeFilter = lagreNyttFilterRespons(nyttFilterModel)
        assertTrue(newMineLagredeFilter.responseCode == 400)
        //TODO: add validation for saving filter, and expect exception in case when name  is empty
    }

    @Test
    fun testTomtFilterValgForNyttLagretFilter() {
        val nyttFilterModel =
            NyttFilterModel(
                filterNavn = "Nytt filter",
                filterValg = PortefoljeFilter()
            )

        val newMineLagredeFilter = lagreNyttFilterRespons(nyttFilterModel)
        //TODO: add validation for saving filter, and expect exception in case when valgt filter are empty
    }

    /** TESTER RELATERT TIL GYLDIGHET FOR OPPDATERING AV EKSISTERENDE FILTER**/
    @Test
    fun testOppdaterLagredeFilterGyldig() {
        var nyttFilter = lagreNyttFilterVerdi(filterModel)

        if (nyttFilter == null) {
            Assert.fail()
            return;
        }

        nyttFilter.filterNavn = "New name"
        nyttFilter.filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"))

        val oppdaterMineLagredeFilterResponse = oppdaterMineLagredeFilter(nyttFilter)

        Assert.assertTrue(oppdaterMineLagredeFilterResponse.responseCode == 200)

        val mineLagredeFilterResponse = getMineLagredeFilter()
        Assert.assertTrue(mineLagredeFilterResponse.responseCode == 200)
        val mineLagredeFilterList = mineLagredeFilterResponse.responseValue
        val oppdatertFilter =
            mineLagredeFilterList.filter { x -> x.filterId == nyttFilter.filterId }.first()

        assertTrue(oppdatertFilter.filterNavn == "New name")
        assertTrue(oppdatertFilter.filterValg == nyttFilter.filterValg)
    }

    @Test
    fun testSlettLagretFilterGyldig() {
        val lagretMineLagredeFilterResponse = lagreNyttFilterVerdi(filterModel)

        if (lagretMineLagredeFilterResponse == null) {
            Assert.fail()
            return
        }

        val responseCode = deleteMineLagredeFilter(
            lagretMineLagredeFilterResponse.filterId,
            lagretMineLagredeFilterResponse.veilederId
        )
        Assert.assertTrue(responseCode == 200 || responseCode == 204)

        val mineLagredeFilterResponse = getMineLagredeFilter()
        val mineLagredeFilter = mineLagredeFilterResponse.responseValue
        Assert.assertTrue(mineLagredeFilter.filter { x -> x.filterId == lagretMineLagredeFilterResponse.filterId }
            .count() == 0)
    }

    /** TESTER RELATERT TIL UGYLDIGHET FOR OPPDATERING AV EKSISTERENDE FILTER **/
    @Test
    fun testLangtNavnForOppdatertLagretFilter() {
        val endepunktRespons =
            lagreNyttFilterRespons(
                NyttFilterModel(
                    filterNavn = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                    filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K")
                )
            )

        //TODO make exception to fail
        assertTrue(endepunktRespons.responseCode != 200)
    }

    @Test
    fun testTomtNavnForOppdatertLagretFilter() {
        var nyttFilter = lagreNyttFilterRespons(filterModel).responseValue
        nyttFilter?.filterNavn = ""

        val endepunktRespons = nyttFilter?.let { oppdaterMineLagredeFilter(it) }

        //TODO give meaningful error for empty name
        assertTrue(endepunktRespons?.responseCode == 200)
    }

    @Test
    fun testTomtFilterValgForOppdatertLagretFilter() {
        var nyttFilter = lagreNyttFilterRespons(filterModel).responseValue
        nyttFilter?.filterValg = PortefoljeFilter()

        val endepunktRespons = nyttFilter?.let { oppdaterMineLagredeFilter(it) }

        //TODO give meaningful error for empty filters
        assertTrue(endepunktRespons?.responseCode == 200)
    }

    private fun testSlettLagretFilterUgyldig() {
        //todo: try to delete filter that doesnt belong to veileder, check error code
    }

    /** DATABASE **/
    @Test
    fun testLagreFilterNårDatabaseErNede() {
        val mineLagredeFilterResponse = getMineLagredeFilter()

        postgresqlContainer.stop()
//        postgresqlContainer.start()
        setUp()

        //TODO: add timeout logic
        val statusKode = lagreNyttFilterRespons(filterModel).responseCode;
    }


    /** TESTER RELATERT TIL GYLDIGHET FOR BÅDE LAGRING OG OPPDATERING **/
    @Test
    fun testNorskeBokstaverINavnForLagretFilter() {
        val endepunktRespons =
            lagreNyttFilterRespons(
                NyttFilterModel(
                    filterNavn = "æøåöäáâò",
                    filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K")
                )
            )
        assertTrue(endepunktRespons.responseCode == 200)
        assertTrue(endepunktRespons.responseValue?.filterNavn == "æøåöäáâò")
    }

    /** HJELPEFUNKSJONER  **/
    private fun getMineLagredeFilter(): ApiResponse<List<MineLagredeFilterModel>> {
        val request: HttpUriRequest = HttpGet("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/")
        val httpResponse = HttpClientBuilder.create().build().execute(request)
        val responseString = BasicResponseHandler().handleResponse(httpResponse)
        return ApiResponse(httpResponse.statusLine.statusCode, deserializeLagredeFilterModels(responseString))
    }

    private fun lagreNyttFilterRespons(valgteFilter: NyttFilterModel): ApiResponse<MineLagredeFilterModel?> {
        val httpclient = HttpClients.createDefault()
        val httpPost = HttpPost("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/")
        var valgteFilterModelJson = Gson().toJson(valgteFilter)
        httpPost.entity = StringEntity(valgteFilterModelJson, ContentType.APPLICATION_JSON)
        val httpResponse = httpclient.execute(httpPost)

        print(EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8))

        if (httpResponse.statusLine.statusCode == 200) {
            return ApiResponse(
                httpResponse.statusLine.statusCode,
                deserializeLagredeFilterModel(
                    BasicResponseHandler().handleResponse(
                        httpResponse
                    )
                )
            )
        } else {
            return ApiResponse(httpResponse.statusLine.statusCode, null)
        }
    }

    private fun oppdaterMineLagredeFilter(
        filterModel: FilterModel
    ): ApiResponse<MineLagredeFilterModel> {
        val httpclient = HttpClients.createDefault()
        val httpPut = HttpPut("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/${filterModel.filterId}")
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
        val httpDelete =
            HttpDelete("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/$veilederId/filter/$filterId")
        val httpResponse = httpclient.execute(httpDelete)
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

    private fun lagreNyttFilterVerdi(filterModel: NyttFilterModel): MineLagredeFilterModel? {
        val lagretMineLagredeFilterResponse = lagreNyttFilterRespons(filterModel)

        Assert.assertTrue(lagretMineLagredeFilterResponse.responseCode == 200)
        return lagretMineLagredeFilterResponse.responseValue
    }
}
