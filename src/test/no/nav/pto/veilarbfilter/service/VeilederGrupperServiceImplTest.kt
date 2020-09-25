package no.nav.pto.veilarbfilter.service


import io.ktor.server.engine.*
import kotlinx.coroutines.runBlocking
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.mainTest
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.model.PortefoljeFilter
import org.junit.Assert
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mock
import org.mockito.Mockito
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VeilederGrupperServiceImplTest {
    lateinit var postgresqlContainer: PostgreSQLContainer<Nothing>;
    lateinit var applicationEngine: ApplicationEngine;

    @Mock
    val veilarbveilederClient: VeilarbveilederClient = Mockito.mock(VeilarbveilederClient::class.java)
    var veilederGrupperServiceImpl: VeilederGrupperServiceImpl = VeilederGrupperServiceImpl(veilarbveilederClient)

    @BeforeAll
    internal fun setUp() {
        Mockito.`when`(veilarbveilederClient.hentVeilederePaEnheten("1")).thenReturn(listOf("1", "2", "3"))

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
    fun testSlettInaktiveVeiledere() = runBlocking<Unit> {
        veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("1", "2", "3")))
        veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("1", "2", "6")))
        veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("10", "12", "13")))

        veilederGrupperServiceImpl.slettVeiledereSomIkkeErAktivePaEnheten("1")

        val filterList = veilederGrupperServiceImpl.finnFilterForFilterBruker("1")

        Assert.assertTrue(filterList.size === 3)
        Assert.assertTrue(filterList.get(0).filterValg.veiledere.containsAll(listOf("1", "2", "3")))
        Assert.assertTrue(filterList.get(1).filterValg.veiledere.containsAll(listOf("1", "2")))
        Assert.assertTrue(filterList.get(2).filterValg.veiledere.isEmpty())
    }


    fun getRandomFilter(veiledereList: List<String>): NyttFilterModel {
        val filterId = Random.nextInt(1, 1000)
        return NyttFilterModel(
            "Filter " + filterId,
            PortefoljeFilter(veiledere = veiledereList)
        )
    }
}