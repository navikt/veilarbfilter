package no.nav.pto.veilarbfilter;


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
import org.junit.Assert
import org.junit.Assert.fail
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import java.time.LocalDateTime
import java.util.Arrays.asList
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTestsVeilederGrupper {
    lateinit var postgresqlContainer: PostgreSQLContainer<Nothing>
    lateinit var applicationEngine: ApplicationEngine
    var randomGenerator = Random

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

    @Test
    fun `Lagring av nytt filter`() {
        val mineLagredeFilterResponse = getFilterGrupper("1")

        if (mineLagredeFilterResponse.responseValue == null) {
            fail()
            return
        }

        lagreNyttFilterRespons("1",getRandomFilter(asList("1","2")))
        val mineLagredeFilterNyResponsEtterLagring = getFilterGrupper("1")

        if (mineLagredeFilterNyResponsEtterLagring.responseValue == null) {
            fail()
            return
        }

        assertTrue(mineLagredeFilterResponse.responseValue.size < mineLagredeFilterNyResponsEtterLagring.responseValue.size)
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

    private fun getRandomFilter(veiledereList: List<String>): NyttFilterModel {
        val filterId = randomGenerator.nextInt(1, 1000)
        return NyttFilterModel(
            "Filter $filterId",
            PortefoljeFilter(veiledere = veiledereList)
        )
    }
}
