package no.nav.pto.veilarbfilter.service

import io.ktor.server.engine.*
import kotlinx.coroutines.runBlocking
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.mainTest
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.model.PortefoljeFilter
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mock
import org.mockito.Mockito
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VeilederGrupperServiceImplTest {
    lateinit var postgresqlContainer: PostgreSQLContainer<Nothing>;
    lateinit var applicationEngine: ApplicationEngine;
    var randomGenerator = Random

    @Mock
    val veilarbveilederClient: VeilarbveilederClient = Mockito.mock(VeilarbveilederClient::class.java)

    @Mock
    val mineLagredeFilterService: MineLagredeFilterServiceImpl = Mockito.mock(MineLagredeFilterServiceImpl::class.java)
    var veilederGrupperServiceImpl: VeilederGrupperServiceImpl =
        VeilederGrupperServiceImpl(veilarbveilederClient, mineLagredeFilterService)

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

    @BeforeEach
    fun wipeAllGroups() = runBlocking<Unit> {
        veilederGrupperServiceImpl.finnFilterForFilterBruker("1")
            .forEach { veilederGrupperServiceImpl.slettFilter(it.filterId, "1") }
    }


    @Test
    fun testSlettInaktiveVeiledere() = runBlocking<Unit> {
        val veildederGruppeId1 =
            veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("1", "2", "3")))?.filterId ?: -1
        val veildederGruppeId2 =
            veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("1", "2", "6")))?.filterId ?: -1
        val veildederGruppeId3 =
            veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("10", "12", "13")))?.filterId ?: -1

        veilederGrupperServiceImpl.slettVeiledereSomIkkeErAktivePaEnheten("1")

        val filterListSize = veilederGrupperServiceImpl.finnFilterForFilterBruker("1").size
        val filterList = veilederGrupperServiceImpl.finnFilterForFilterBruker("1")

        assertEquals(filterListSize, 2)
        assertTrue(finnVeilederDB(veildederGruppeId1, filterList) {
            it.filterValg.veiledere.containsAll(
                listOf(
                    "1",
                    "2",
                    "3"
                )
            )
        })
        assertTrue(finnVeilederDB(veildederGruppeId2, filterList) {
            it.filterValg.veiledere.containsAll(
                listOf(
                    "1",
                    "2"
                )
            )
        })
        assertNull(filterList.find { gruppe -> gruppe.filterId == veildederGruppeId3 })

        assertTrue(finnVeilederDB(veildederGruppeId1, filterList) { it.filterCleanup == 0 })
        assertTrue(finnVeilederDB(veildederGruppeId2, filterList) { it.filterCleanup == 1 })
    }

    @Test
    fun `Single veileder in a group`() = runBlocking<Unit> {
        val veildederGruppeId =
            veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("1")))?.filterId ?: -1
        val filterList = veilederGrupperServiceImpl.finnFilterForFilterBruker("1")

        assertTrue(finnVeilederDB(veildederGruppeId, filterList) { it.filterValg.veiledere.containsAll(listOf("1")) })
    }

    @Test
    fun `Retrive veiledergruppe`() = runBlocking<Unit> {
        val veildederGruppe = veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("1")))

        assertNotNull(veildederGruppe)
        if (veildederGruppe != null) {
            val filterFromService = veilederGrupperServiceImpl.hentFilter(veildederGruppe.filterId)
            if (filterFromService != null) {
                assertEquals(filterFromService.filterId, veildederGruppe.filterId)
                assertEquals(filterFromService.filterNavn, veildederGruppe.filterNavn)
                assertEquals(filterFromService.filterValg, veildederGruppe.filterValg)
            } else {
                fail("Filter was not in DB")
            }
        }
    }

    @Test
    fun `Slett veiledergruppe`() = runBlocking<Unit> {
        val veildederGruppe = veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("1", "2")))
        assertNotNull(veildederGruppe)
        if (veildederGruppe != null) {
            veilederGrupperServiceImpl.slettFilter(veildederGruppe.filterId, "1");
            val filterFromService = veilederGrupperServiceImpl.hentFilter(veildederGruppe.filterId)
            assertNull(filterFromService)
        }
    }

    @Test
    fun `Inactive veileder removed from active group`() = runBlocking<Unit> {
        val veildederGruppeId =
            veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("1", "5")))?.filterId ?: -1
        val filterList = veilederGrupperServiceImpl.finnFilterForFilterBruker("1")

        assertTrue(finnVeilederDB(veildederGruppeId, filterList) { it.filterValg.veiledere.containsAll(listOf("1")) })
    }

    @Test
    fun `Update filter`() = runBlocking<Unit> {
        val veildederGruppe1 = veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("1")))
        val veildederGruppe2 = veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("1", "2", "3")))

        assertNotNull(veildederGruppe1)
        if (veildederGruppe1 != null) {
            veildederGruppe1.filterValg = PortefoljeFilter(veiledere = listOf("1", "2"))
            veilederGrupperServiceImpl.oppdaterFilter("1", veildederGruppe1)
            val filterList = veilederGrupperServiceImpl.finnFilterForFilterBruker("1")

            assertTrue(finnVeilederDB(veildederGruppe1.filterId, filterList) {
                it.filterValg.veiledere.containsAll(
                    listOf("1", "2")
                )
            })
        }

        assertNotNull(veildederGruppe2)
        if (veildederGruppe2 != null) {
            veildederGruppe2.filterValg = PortefoljeFilter(veiledere = listOf("1"))
            veilederGrupperServiceImpl.oppdaterFilter("1", veildederGruppe2)
            val filterList = veilederGrupperServiceImpl.finnFilterForFilterBruker("1")

            assertTrue(finnVeilederDB(veildederGruppe2.filterId, filterList) {
                it.filterValg.veiledere.containsAll(
                    listOf("1")
                )
            })
        }
    }

    @Test
    fun testSlettTomtVeiledereGruppeEtterCleanup() = runBlocking<Unit> {
        veilederGrupperServiceImpl.lagreFilter("1", getRandomFilter(listOf("10", "12", "13")))

        veilederGrupperServiceImpl.slettVeiledereSomIkkeErAktivePaEnheten("1")

        val filterList = veilederGrupperServiceImpl.finnFilterForFilterBruker("1")

        assertTrue(filterList.isEmpty())
    }

    private fun finnVeilederDB(gruppeID: Int, filterList: List<FilterModel>, func: (FilterModel) -> Boolean): Boolean {
        return filterList.find { gruppe -> gruppe.filterId == gruppeID }?.let { func(it) } ?: false
    }

    private fun getRandomFilter(veiledereList: List<String>): NyttFilterModel {
        val filterId = randomGenerator.nextInt(1, 1000)
        return NyttFilterModel(
            "Filter $filterId",
            PortefoljeFilter(veiledere = veiledereList)
        )
    }
}