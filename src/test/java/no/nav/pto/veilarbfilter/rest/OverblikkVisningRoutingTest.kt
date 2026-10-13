package no.nav.pto.veilarbfilter.rest

import io.getunleash.DefaultUnleash
import no.nav.pto.veilarbfilter.auth.AuthUtils
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.config.FeatureToggle
import no.nav.pto.veilarbfilter.domene.OverblikkVisning
import no.nav.pto.veilarbfilter.domene.OverblikkVisningRequest
import no.nav.pto.veilarbfilter.domene.OverblikkVisningResponse
import no.nav.pto.veilarbfilter.domene.value.VeilederId
import no.nav.pto.veilarbfilter.service.OverblikkVisningService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.*

class OverblikkVisningRoutingTest {
    private val tjeneste = mock(OverblikkVisningService::class.java)
    private val veilarbveilederKlient = mock(VeilarbveilederClient::class.java)
    private val funksjonsbryter = mock(DefaultUnleash::class.java)
    private val kontroller = OverblikkVisningController(tjeneste, veilarbveilederKlient, funksjonsbryter)

    @BeforeEach
    fun settOpp() {
        `when`(funksjonsbryter.isEnabled(FeatureToggle.OVERBLIKK_VISNING_I_VEILARBVEILEDER))
            .thenReturn(true)
    }

    @Test
    fun `skal rute lesing lagring og sletting til veilarbveileder naar funksjonsbryter er pa`() {
        val forventetSvar = OverblikkVisningResponse(listOf("CV"))
        `when`(veilarbveilederKlient.hent()).thenReturn(ResponseEntity.ok(forventetSvar))
        `when`(veilarbveilederKlient.lagre(OverblikkVisningRequest(listOf("CV"))))
            .thenReturn(ResponseEntity.noContent().build())
        `when`(veilarbveilederKlient.slett()).thenReturn(ResponseEntity.noContent().build())

        assertThat(kontroller.hentOverblikkvisningForInnloggetVeileder().body).isEqualTo(forventetSvar)
        assertThat(
            kontroller.lagreOverblikkvisningForInnloggetVeileder(
                OverblikkVisningRequest(listOf("CV"))
            ).statusCode
        ).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(kontroller.slettOverblikkvisningForInnloggetVeileder().statusCode)
            .isEqualTo(HttpStatus.NO_CONTENT)

        verify(veilarbveilederKlient).hent()
        verify(veilarbveilederKlient).lagre(OverblikkVisningRequest(listOf("CV")))
        verify(veilarbveilederKlient).slett()
        verifyNoInteractions(tjeneste)
    }

    @Test
    fun `skal gi like svar og opprett oppdater slett flyt med og uten funksjonsbryter`() {
        doReturn(false).`when`(funksjonsbryter)
            .isEnabled(FeatureToggle.OVERBLIKK_VISNING_I_VEILARBVEILEDER)

        var lokalVisning: List<String>? = null
        var visningIVeilarbveileder: List<String>? = null

        `when`(tjeneste.hentOverblikkVisning(VeilederId.of("Z999999"))).thenAnswer {
            Optional.ofNullable(lokalVisning)
                .map { visning -> OverblikkVisning(UUID.randomUUID(), visning) }
        }
        doAnswer {
            lokalVisning = listOf("CV", "Personalia")
            null
        }.`when`(tjeneste).lagreOverblikkVisning(
            VeilederId.of("Z999999"),
            listOf("CV", "Personalia")
        )
        doAnswer {
            lokalVisning = listOf("CV")
            null
        }.`when`(tjeneste).lagreOverblikkVisning(VeilederId.of("Z999999"), listOf("CV"))
        doAnswer {
            lokalVisning = null
            null
        }.`when`(tjeneste).slettOverblikkVisning(VeilederId.of("Z999999"))

        `when`(veilarbveilederKlient.hent()).thenAnswer {
            ResponseEntity.ok(OverblikkVisningResponse(visningIVeilarbveileder?.toList().orEmpty()))
        }
        `when`(
            veilarbveilederKlient.lagre(OverblikkVisningRequest(listOf("CV", "Personalia")))
        ).thenAnswer {
            visningIVeilarbveileder = listOf("CV", "Personalia")
            ResponseEntity.noContent().build<Any>()
        }
        `when`(veilarbveilederKlient.lagre(OverblikkVisningRequest(listOf("CV")))).thenAnswer {
            visningIVeilarbveileder = listOf("CV")
            ResponseEntity.noContent().build<Any>()
        }
        `when`(veilarbveilederKlient.slett()).thenAnswer {
            visningIVeilarbveileder = null
            ResponseEntity.noContent().build<Any>()
        }

        mockStatic(AuthUtils::class.java).use { autentisering ->
            autentisering.`when`<VeilederId> { AuthUtils.getInnloggetVeilederIdent() }
                .thenReturn(VeilederId.of("Z999999"))

            val lokaleSvar = kjorOpprettOppdaterSlettFlyt()
            doReturn(true).`when`(funksjonsbryter)
                .isEnabled(FeatureToggle.OVERBLIKK_VISNING_I_VEILARBVEILEDER)
            val svarFraVeilarbveileder = kjorOpprettOppdaterSlettFlyt()

            assertThat(svarFraVeilarbveileder).isEqualTo(lokaleSvar)
            assertThat(lokaleSvar.statuskoder).containsExactly(200, 204, 200, 204, 200, 204, 200)
            assertThat(lokaleSvar.visninger).containsExactly(
                emptyList<String>(),
                listOf("CV", "Personalia"),
                listOf("CV"),
                emptyList<String>()
            )
        }
    }

    private fun kjorOpprettOppdaterSlettFlyt(): Flytresultat {
        val forOppretting = kontroller.hentOverblikkvisningForInnloggetVeileder()
        val oppretting = kontroller.lagreOverblikkvisningForInnloggetVeileder(
            OverblikkVisningRequest(listOf("CV", "Personalia"))
        )
        val etterOppretting = kontroller.hentOverblikkvisningForInnloggetVeileder()
        val oppdatering = kontroller.lagreOverblikkvisningForInnloggetVeileder(
            OverblikkVisningRequest(listOf("CV"))
        )
        val etterOppdatering = kontroller.hentOverblikkvisningForInnloggetVeileder()
        val sletting = kontroller.slettOverblikkvisningForInnloggetVeileder()
        val etterSletting = kontroller.hentOverblikkvisningForInnloggetVeileder()

        return Flytresultat(
            statuskoder = listOf(
                forOppretting.statusCode.value(),
                oppretting.statusCode.value(),
                etterOppretting.statusCode.value(),
                oppdatering.statusCode.value(),
                etterOppdatering.statusCode.value(),
                sletting.statusCode.value(),
                etterSletting.statusCode.value()
            ),
            visninger = listOf(
                forOppretting.body!!.overblikkVisning(),
                etterOppretting.body!!.overblikkVisning(),
                etterOppdatering.body!!.overblikkVisning(),
                etterSletting.body!!.overblikkVisning()
            )
        )
    }

    private data class Flytresultat(
        val statuskoder: List<Int>,
        val visninger: List<List<String>>
    )
}
