package no.nav.pto.veilarbfilter;


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.server.engine.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import java.time.LocalDateTime
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTestsVeilederGrupper {
    lateinit var postgresqlContainer: PostgreSQLContainer<Nothing>
    lateinit var applicationEngine: ApplicationEngine
    var randomGenerator = Random

    @BeforeAll
    internal fun setUp() = runBlocking {
        postgresqlContainer = PostgreSQLContainer<Nothing>("postgres:12-alpine").apply {
            withDatabaseName("veilarbfilter")
            withUsername("user")
            withPassword("password")
        }
        postgresqlContainer.start()
        applicationEngine =
            mainTestWithMock(postgresqlContainer.jdbcUrl, postgresqlContainer.username, postgresqlContainer.password)
    }

    @AfterAll
    fun tearDown() {
        postgresqlContainer.stop()
        applicationEngine.stop(0, 0)
    }

    @Test
    fun `Lagring av ny veileder filter`() {
        val mineLagredeFilterResponse = getFilterGrupper("1")

        if (mineLagredeFilterResponse.responseValue == null) {
            fail()
        } else {
            lagreNyttFilterRespons("1", getRandomFilter(listOf("1")))
            val mineLagredeFilterNyResponsEtterLagring = getFilterGrupper("1")

            if (mineLagredeFilterNyResponsEtterLagring.responseValue == null || mineLagredeFilterNyResponsEtterLagring.responseValue.isEmpty()) {
                fail()
            } else {
                assertTrue(mineLagredeFilterResponse.responseValue.size < mineLagredeFilterNyResponsEtterLagring.responseValue.size)
            }
        }
    }

    @Test
    fun `CleanupVeilederGrupper fjerner ugyldige veildere`() = runBlocking<Unit> {
        val mineLagredeFilterResponse = getFilterGrupper("1")

        if (mineLagredeFilterResponse.responseValue == null) {
            fail()
        } else {
            val responsLagring =
                lagreNyttFilterRespons("1", getRandomFilter(listOf("1", "2", "23546576"))).responseValue
            if (responsLagring == null) {
                fail()
            } else {
                delay(1000)   // Wait for clean up
                val filterePaEnhet = getFilterGrupper("1").responseValue

                if (filterePaEnhet == null || filterePaEnhet.isEmpty()) {
                    fail()
                } else {
                    assertTrue(filterePaEnhet.find { it.filterId == responsLagring.filterId }
                        ?.let { it.filterValg.veiledere == listOf("1", "2") } ?: false)
                }
            }
        }
    }

    @Test
    fun `Oppdater filter fra 3 til 2 veiledere`() {
        val veildederGruppeFor = listOf("1", "2", "3")
        val veildederGruppeEtter = listOf("1", "2")
        val responsLagring = lagreNyttFilterRespons("1", getRandomFilter(veildederGruppeFor)).responseValue

        if (responsLagring == null) {
            fail()
        } else {
            responsLagring.filterValg = PortefoljeFilter(veiledere = veildederGruppeEtter);
            val responsOppdater = oppdaterFilterRespons("1", responsLagring).responseValue

            val alleFilterePaEnhet = getFilterGrupper("1")

            if (responsOppdater == null || alleFilterePaEnhet.responseValue == null || alleFilterePaEnhet.responseValue.isEmpty()) {
                fail()
            } else {
                val filterePaEnhet = alleFilterePaEnhet.responseValue;
                assertTrue(filterePaEnhet.find { it.filterId == responsLagring.filterId }
                    ?.let { it.filterValg.veiledere == veildederGruppeEtter } ?: false)
                assertTrue(responsOppdater.filterValg.veiledere == veildederGruppeEtter)
            }
        }
    }


    @Test
    fun `Slette veiledergruppe`() {
        val veildederGruppe = listOf("1", "2", "3")
        val responsLagring = lagreNyttFilterRespons("1", getRandomFilter(veildederGruppe)).responseValue

        if (responsLagring == null) {
            fail()
        } else {
            val allefilterePaEnhetForSlett = getFilterGrupper("1").responseValue
            if (allefilterePaEnhetForSlett == null) {
                fail()
            } else {
                assertTrue(allefilterePaEnhetForSlett.find { it.filterId == responsLagring.filterId }
                    ?.let { it.filterValg.veiledere == veildederGruppe } ?: false)

                slettFilter("1", responsLagring.filterId)
                val allefilterePaEnhetEtterSlett = getFilterGrupper("1").responseValue
                if (allefilterePaEnhetEtterSlett == null) {
                    fail()
                } else {
                    assertNull(allefilterePaEnhetEtterSlett.find { it.filterId == responsLagring.filterId })
                }
            }
        }
    }


    /** HJELPEFUNKSJONER  **/
    private fun getFilterGrupper(enhet: String): ApiResponse<List<FilterModel>> {
        val request: HttpUriRequest = HttpGet("http://0.0.0.0:8080/veilarbfilter/api/enhet/$enhet")
        val httpResponse = HttpClientBuilder.create().build().execute(request)
        val responseString = BasicResponseHandler().handleResponse(httpResponse)
        return ApiResponse(httpResponse.statusLine.statusCode, deserializeLagredeFilterModels(responseString))
    }

    private fun lagreNyttFilterRespons(enhet: String, valgteFilter: NyttFilterModel): ApiResponse<FilterModel> {
        val response = Request.Post("http://0.0.0.0:8080/veilarbfilter/api/enhet/$enhet")
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

    private fun oppdaterFilterRespons(
        enhet: String,
        filterModel: FilterModel
    ): ApiResponse<MineLagredeFilterModel?> {
        val response =
            Request.Put("http://0.0.0.0:8080/veilarbfilter/api/enhet/$enhet")
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

    private fun slettFilter(enhet: String, filterId: Int): Int {
        val httpclient = HttpClients.createDefault()
        val httpDelete =
            HttpDelete("http://0.0.0.0:8080/veilarbfilter/api/enhet/$enhet/filter/$filterId")
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

    private fun getRandomFilter(veiledereList: List<String>): NyttFilterModel {
        val filterId = randomGenerator.nextInt(1, 1000)
        return NyttFilterModel(
            "Filter $filterId",
            PortefoljeFilter(veiledere = veiledereList)
        )
    }
}
