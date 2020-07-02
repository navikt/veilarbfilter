package no.nav.pto.veilarbfilter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import no.nav.common.utils.NaisUtils
import no.nav.pto.veilarbfilter.config.Configuration
import no.nav.pto.veilarbfilter.model.*
import org.apache.http.client.fluent.Request
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.junit.Assert.fail
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
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

    private val defaultFilterModel =
        NyttFilterModel(
            filterNavn = "Test",
            filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K")
        )

    private val gyldigFilterModel =
        NyttFilterModel(
            filterNavn = "Heiheiheihei",
            filterValg = PortefoljeFilter(kjonn = "M")
        )

    private val gyldigFilterModelOppdatering =
        NyttFilterModel(
            filterNavn = "Hei Drago",
            filterValg = PortefoljeFilter(kjonn = "K")
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

        if (mineLagredeFilterResponse.responseValue == null) {
            fail()
            return
        }

        lagreNyttFilterRespons(gyldigFilterModel)
        val mineLagredeFilterNyResponsEtterLagring = getMineLagredeFilter()

        if (mineLagredeFilterNyResponsEtterLagring.responseValue == null) {
            fail()
            return
        }

        assertTrue(mineLagredeFilterResponse.responseValue.size < mineLagredeFilterNyResponsEtterLagring.responseValue.size)
    }

    /** TESTER RELATERT TIL UGYLDIGHET FOR LAGRING AV NYTT FILTER **/
    @Test
    fun testLagretFilterNavnEksistererForNyttFilter() {
        lagreNyttFilterRespons(defaultFilterModel)
        val mineLagredeFilterResponse = getMineLagredeFilter()

        val nyttFilterModelEksisterendeNavn =
            NyttFilterModel(
                filterNavn = defaultFilterModel.filterNavn,
                filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "M")
            )

        val lagreNyttFilterMedEksisterendeNavn = lagreNyttFilterRespons(nyttFilterModelEksisterendeNavn)
        val mineLagredeFilterResponsEtterFeilLagring = getMineLagredeFilter()

        if (mineLagredeFilterResponse.responseValue == null || mineLagredeFilterResponsEtterFeilLagring.responseValue == null) {
            fail()
            return
        }

        assertEquals(lagreNyttFilterMedEksisterendeNavn.errorMessage, "Navn eksisterer i et annet lagret filter")
        assertTrue(mineLagredeFilterResponse.responseValue.size == mineLagredeFilterResponsEtterFeilLagring.responseValue.size)
    }

    @Test
    fun testLagretFilterValgEksistererForNyttFilter() {
        lagreNyttFilterRespons(defaultFilterModel)
        val mineLagredeFilterResponse = getMineLagredeFilter()

        val nyttFilterModelEksisterendeFilter =
            NyttFilterModel(
                filterNavn = "Team Voff",
                filterValg = defaultFilterModel.filterValg
            )

        val lagreNyttFilterMedEksisterendeFilterKombinasjon = lagreNyttFilterRespons(nyttFilterModelEksisterendeFilter)
        val mineLagredeFilterResponseEtterFeilLagring = getMineLagredeFilter()

        if (mineLagredeFilterResponse.responseValue == null || mineLagredeFilterResponseEtterFeilLagring.responseValue == null) {
            fail()
            return
        }

        assertEquals(
            lagreNyttFilterMedEksisterendeFilterKombinasjon.errorMessage,
            "Filterkombinasjon eksisterer i et annet lagret filter"
        )
        assertTrue(lagreNyttFilterMedEksisterendeFilterKombinasjon.responseCode == 400)
        assertTrue(mineLagredeFilterResponse.responseValue.size == mineLagredeFilterResponseEtterFeilLagring.responseValue.size)
    }

    @Test
    fun testTomtNavnForNyttLagretFilter() {
        val nyttFilterModel =
            NyttFilterModel(
                filterNavn = "",
                filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K")
            )

        val lagreNyttFilterMedTomtFilterNavn = lagreNyttFilterRespons(nyttFilterModel)

        assertTrue(lagreNyttFilterMedTomtFilterNavn.responseCode == 400)
        assertEquals(lagreNyttFilterMedTomtFilterNavn.errorMessage, "Navn kan ikke være tomt")
    }

    @Test
    fun testTomtFilterValgForNyttLagretFilter() {
        val nyttFilterModel =
            NyttFilterModel(
                filterNavn = "Nytt filter",
                filterValg = PortefoljeFilter()
            )

        val lagreNyttFilterMedTomFilterKombinasjon = lagreNyttFilterRespons(nyttFilterModel)
        assertTrue(lagreNyttFilterMedTomFilterKombinasjon.responseCode == 400)
        assertEquals(lagreNyttFilterMedTomFilterKombinasjon.errorMessage, "Filtervalg kan ikke være tomt")
    }

    /** TESTER RELATERT TIL GYLDIGHET FOR OPPDATERING AV EKSISTERENDE FILTER**/
    @Test
    fun testOppdaterLagredeFilterGyldig() {
        var nyttFilter = lagreNyttFilterVerdi(gyldigFilterModelOppdatering)

        if (nyttFilter == null) {
            fail()
            return
        }

        nyttFilter.filterNavn = "New name"
        nyttFilter.filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"))

        oppdaterMineLagredeFilter(nyttFilter)

        val mineLagredeFilterResponse = getMineLagredeFilter()

        val mineLagredeFilterList = mineLagredeFilterResponse.responseValue

        if (mineLagredeFilterList == null) {
            fail()
            return
        }

        val oppdatertFilter =
            mineLagredeFilterList.filter { elem -> elem.filterId == nyttFilter.filterId }.first()

        assertTrue(oppdatertFilter.filterNavn == nyttFilter.filterNavn)
        assertTrue(oppdatertFilter.filterValg == nyttFilter.filterValg)
    }

    @Test
    fun testSlettLagretFilterGyldig() {
        val lagretMineLagredeFilterResponse = lagreNyttFilterVerdi(defaultFilterModel)

        if (lagretMineLagredeFilterResponse == null) {
            fail()
            return
        }

        val responseCode = deleteMineLagredeFilter(
            lagretMineLagredeFilterResponse.filterId,
            lagretMineLagredeFilterResponse.veilederId
        )
        assertTrue(responseCode == 200 || responseCode == 204)

        val mineLagredeFilterResponse = getMineLagredeFilter()

        if (mineLagredeFilterResponse.responseValue == null) {
            fail()
            return
        }

        val mineLagredeFilter = mineLagredeFilterResponse.responseValue

        assertTrue(mineLagredeFilter.filter { elem -> elem.filterId == lagretMineLagredeFilterResponse.filterId }
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

        assertEquals(endepunktRespons.errorMessage, "Lengden på navnet kan ikke være mer enn 255 karakterer")
        assertTrue(endepunktRespons.responseCode != 200)
    }

    @Test
    fun testTomtNavnForOppdatertLagretFilter() {
        var nyttFilter = lagreNyttFilterRespons(defaultFilterModel).responseValue

        if (nyttFilter == null) {
            fail()
            return
        }

        nyttFilter.filterNavn = ""

        val endepunktRespons = oppdaterMineLagredeFilter(nyttFilter)

        assertEquals(endepunktRespons.errorMessage, "Navn kan ikke være tomt")
        assertTrue(endepunktRespons.responseCode == 400)
    }

    @Test
    fun testTomtFilterValgForOppdatertLagretFilter() {
        var nyttFilter = lagreNyttFilterRespons(defaultFilterModel).responseValue

        if (nyttFilter == null) {
            fail()
            return
        }

        nyttFilter.filterValg = PortefoljeFilter()

        val endepunktRespons = oppdaterMineLagredeFilter(nyttFilter)

        assertEquals(endepunktRespons.errorMessage, "Filtervalg kan ikke være tomt")
        assertTrue(endepunktRespons.responseCode == 400)
    }

    private fun testSlettLagretFilterUgyldig() {
        //todo: try to delete filter that doesnt belong to veileder, check error code
    }

    //TODO: write test for when database is down
//    /** DATABASE **/
//    @Test
//    fun testLagreFilterNårDatabaseErNede() {
//        val mineLagredeFilterResponse = getMineLagredeFilter()
//
//        postgresqlContainer.stop()
////        postgresqlContainer.start()
//        setUp()
//
//        //TODO: add timeout logic
//        val statusKode = lagreNyttFilterRespons(filterModel).responseCode;
//    }


    /** TESTER RELATERT TIL GYLDIGHET FOR BÅDE LAGRING OG OPPDATERING **/
    @Test
    fun testNorskeBokstaverINavnForLagretFilter() {
        val endepunktRespons =
            lagreNyttFilterRespons(
                NyttFilterModel(
                    filterNavn = "æøåöäáâò",
                    filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "M")
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

    private fun lagreNyttFilterRespons(valgteFilter: NyttFilterModel): ApiResponse<MineLagredeFilterModel> {
        val response = Request.Post("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/")
            .bodyString(Gson().toJson(valgteFilter), ContentType.APPLICATION_JSON)
            .connectTimeout(1000)
            .execute()
            .returnResponse()

        val statusCode = response.statusLine.statusCode
        val responseContent = EntityUtils.toString(response.entity)

        if (statusCode == 200) return ApiResponse(
            responseCode = statusCode,
            responseValue = deserializeLagredeFilterModel(
                responseContent
            )
        ) else return ApiResponse(responseCode = statusCode, errorMessage = responseContent)
    }

    private fun oppdaterMineLagredeFilter(
        filterModel: FilterModel
    ): ApiResponse<MineLagredeFilterModel?> {
        val response = Request.Put("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/${filterModel.filterId}")
            .bodyString(serializeLagredeFilterModel(filterModel), ContentType.APPLICATION_JSON)
            .connectTimeout(1000)
            .execute()
            .returnResponse()

        val statusCode = response.statusLine.statusCode
        val responseContent = EntityUtils.toString(response.entity)

        if (statusCode == 200) return ApiResponse(
            responseCode = statusCode,
            responseValue = deserializeLagredeFilterModel(
                responseContent
            )
        ) else return ApiResponse(responseCode = statusCode, errorMessage = responseContent)
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

        assertTrue(lagretMineLagredeFilterResponse.responseCode == 200)
        return lagretMineLagredeFilterResponse.responseValue
    }
}
