package no.nav.pto.veilarbfilter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.server.engine.*
import no.nav.pto.veilarbfilter.model.*
import no.nav.pto.veilarbfilter.service.LagredeFilterFeilmeldinger
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
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTestsMineFilter {
    lateinit var postgresqlContainer: PostgreSQLContainer<Nothing>;
    lateinit var applicationEngine: ApplicationEngine;

    @BeforeAll
    internal fun setUp() {
        postgresqlContainer = PostgreSQLContainer<Nothing>("postgres:12-alpine").apply {
            withDatabaseName("veilarbfilter")
            withUsername("user")
            withPassword("password")
        }
        postgresqlContainer.start()
        applicationEngine =
            mainTest(postgresqlContainer.jdbcUrl, postgresqlContainer.username, postgresqlContainer.password)
    }

    @AfterAll
    fun tearDown() {
        postgresqlContainer.stop()
        applicationEngine.stop(0, 0)
    }

    /** TESTER RELATERT TIL GYLDIGHET FOR LAGRING AV NYTT FILTER **/
    @Test
    fun `Lagring av nytt filter er gyldig`() {
        val mineLagredeFilterResponse = getMineLagredeFilter()

        if (mineLagredeFilterResponse.responseValue == null) {
            fail()
            return
        }

        lagreNyttFilterRespons(getRandomNyttFilter())
        val mineLagredeFilterNyResponsEtterLagring = getMineLagredeFilter()

        if (mineLagredeFilterNyResponsEtterLagring.responseValue == null) {
            fail()
            return
        }

        assertTrue(mineLagredeFilterResponse.responseValue.size < mineLagredeFilterNyResponsEtterLagring.responseValue.size)
    }

    /** TESTER RELATERT TIL UGYLDIGHET FOR LAGRING AV NYTT FILTER **/
    @Test
    fun `Filternavn eksisterer allerede for nytt filter`() {
        val randomNyttFilter = getRandomNyttFilter()

        lagreNyttFilterRespons(randomNyttFilter)
        val mineLagredeFilterResponse = getMineLagredeFilter()

        val nyttFilterModelEksisterendeNavn =
            NyttFilterModel(
                filterNavn = randomNyttFilter.filterNavn,
                filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "M")
            )

        val lagreNyttFilterMedEksisterendeNavn = lagreNyttFilterRespons(nyttFilterModelEksisterendeNavn)
        val mineLagredeFilterResponsEtterFeilLagring = getMineLagredeFilter()

        if (mineLagredeFilterResponse.responseValue == null || mineLagredeFilterResponsEtterFeilLagring.responseValue == null) {
            fail()
            return
        }

        assertEquals(
            lagreNyttFilterMedEksisterendeNavn.errorMessage,
            LagredeFilterFeilmeldinger.NAVN_EKSISTERER.message
        )
        assertTrue(mineLagredeFilterResponse.responseValue.size == mineLagredeFilterResponsEtterFeilLagring.responseValue.size)
    }

    @Test
    fun `Filtervalg eksisterer allerede for nytt filter`() {
        val randomNyttFilter = getRandomNyttFilter()

        lagreNyttFilterRespons(randomNyttFilter)
        val mineLagredeFilterResponse = getMineLagredeFilter()

        val nyttFilterModelEksisterendeFilter =
            NyttFilterModel(
                filterNavn = "Team Voff",
                filterValg = randomNyttFilter.filterValg
            )

        val lagreNyttFilterMedEksisterendeFilterKombinasjon =
            lagreNyttFilterRespons(nyttFilterModelEksisterendeFilter)
        val mineLagredeFilterResponseEtterFeilLagring = getMineLagredeFilter()

        if (mineLagredeFilterResponse.responseValue == null || mineLagredeFilterResponseEtterFeilLagring.responseValue == null) {
            fail()
            return
        }

        assertEquals(
            lagreNyttFilterMedEksisterendeFilterKombinasjon.errorMessage,
            LagredeFilterFeilmeldinger.FILTERVALG_EKSISTERER.message
        )
        assertTrue(lagreNyttFilterMedEksisterendeFilterKombinasjon.responseCode == 400)
        assertTrue(mineLagredeFilterResponse.responseValue.size == mineLagredeFilterResponseEtterFeilLagring.responseValue.size)
    }

    @Test
    fun `Tomt navn er ugyldig for nytt filter`() {
        val nyttFilterModel =
            NyttFilterModel(
                filterNavn = "",
                filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K")
            )

        val lagreNyttFilterMedTomtFilterNavn = lagreNyttFilterRespons(nyttFilterModel)

        assertTrue(lagreNyttFilterMedTomtFilterNavn.responseCode == 400)
        assertEquals(lagreNyttFilterMedTomtFilterNavn.errorMessage, LagredeFilterFeilmeldinger.NAVN_TOMT.message)
    }

    @Test
    fun `Tomt filtervalg er ugyldig for nytt filter`() {
        val nyttFilterModel =
            NyttFilterModel(
                filterNavn = "Nytt filter",
                filterValg = PortefoljeFilter()
            )

        val lagreNyttFilterMedTomFilterKombinasjon = lagreNyttFilterRespons(nyttFilterModel)
        assertTrue(lagreNyttFilterMedTomFilterKombinasjon.responseCode == 400)
        assertEquals(
            lagreNyttFilterMedTomFilterKombinasjon.errorMessage,
            LagredeFilterFeilmeldinger.FILTERVALG_TOMT.message
        )
    }

    /** TESTER RELATERT TIL GYLDIGHET FOR OPPDATERING AV EKSISTERENDE FILTER**/
    @Test
    fun `Oppdatering av filter er gyldig`() {
        val nyttFilter = lagreNyttFilterVerdi(getRandomNyttFilter())

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
    fun `Sletting av filter er gyldig`() {
        val lagretMineLagredeFilterResponse = lagreNyttFilterVerdi(getRandomNyttFilter())

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
    fun `For langt navn er ugyldig`() {
        val endepunktRespons =
            lagreNyttFilterRespons(
                NyttFilterModel(
                    filterNavn = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                    filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "K")
                )
            )

        assertEquals(endepunktRespons.errorMessage, LagredeFilterFeilmeldinger.NAVN_FOR_LANGT.message)
        assertTrue(endepunktRespons.responseCode != 200)
    }

    @Test
    fun `Tomt navn er ugyldig for oppdatering av filter`() {
        val nyttFilter = lagreNyttFilterRespons(getRandomNyttFilter()).responseValue

        if (nyttFilter == null) {
            fail()
            return
        }

        nyttFilter.filterNavn = ""

        val endepunktRespons = oppdaterMineLagredeFilter(nyttFilter)

        assertEquals(endepunktRespons.errorMessage, LagredeFilterFeilmeldinger.NAVN_TOMT.message)
        assertTrue(endepunktRespons.responseCode == 400)
    }

    @Test
    fun `Tomt filtervalg er ugyldig for oppdatert filter`() {
        val nyttFilter = lagreNyttFilterRespons(getRandomNyttFilter()).responseValue

        if (nyttFilter == null) {
            fail()
            return
        }

        nyttFilter.filterValg = PortefoljeFilter()

        val endepunktRespons = oppdaterMineLagredeFilter(nyttFilter)

        assertEquals(endepunktRespons.errorMessage, LagredeFilterFeilmeldinger.FILTERVALG_TOMT.message)
        assertTrue(endepunktRespons.responseCode == 400)
    }

    fun `test at sletting av andre veileders filtere er ugyldig`() {
        //todo: try to delete filter that doesnt belong to veileder, check error code
    }

    /** TESTER RELATERT TIL GYLDIGHET FOR BÅDE LAGRING OG OPPDATERING **/
    @Test
    fun `Spesialbokstaver fungerer`() {
        val spesialbokstaverFilterNavn = "æøåöäáâò"
        val endepunktRespons =
            lagreNyttFilterRespons(
                NyttFilterModel(
                    filterNavn = spesialbokstaverFilterNavn,
                    filterValg = PortefoljeFilter(ferdigfilterListe = listOf("UFORDELTE_BRUKERE"), kjonn = "M")
                )
            )
        assertTrue(endepunktRespons.responseCode == 200)
        assertTrue(endepunktRespons.responseValue?.filterNavn == spesialbokstaverFilterNavn)
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
        val response =
            Request.Put("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/")
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
            HttpDelete("http://0.0.0.0:8080/veilarbfilter/api/minelagredefilter/$filterId")
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

    private fun getRandomNyttFilter(): NyttFilterModel {
        val alderVelg = listOf("19-og-under", "20-24", "25-29", "30-39", "40-49", "50-59", "60-66", "67-70")

        return NyttFilterModel(
            filterNavn = "Filter navn " + Random.nextInt(10, 1000),
            filterValg = PortefoljeFilter(
                kjonn = "K",
                fodselsdagIMnd = listOf(Random.nextInt(1, 31).toString(), Random.nextInt(1, 31).toString()),
                alder = listOf(alderVelg.random())
            )
        )
    }
}
