package no.nav.pto.veilarbfilter.rest

import io.getunleash.DefaultUnleash
import io.getunleash.UnleashContext
import no.nav.pto.veilarbfilter.AbstractTest
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

class OverblikkVisningRoutingTest : AbstractTest() {
    private val tjeneste = mock(OverblikkVisningService::class.java)
    private val veilarbveilederKlient = mock(VeilarbveilederClient::class.java)
    private val funksjonsbryter = mock(DefaultUnleash::class.java)
    private val kontroller = OverblikkVisningController(tjeneste, veilarbveilederKlient, funksjonsbryter)

    @BeforeEach
    fun settOpp() {
        `when`(
            funksjonsbryter.isEnabled(
                eq(FeatureToggle.OVERBLIKK_VISNING_I_VEILARBVEILEDER),
                any(UnleashContext::class.java)
            )
        )
            .thenReturn(true)
    }

    @Test
    fun `skal rute lesing lagring og sletting til veilarbveileder naar funksjonsbryter er pa`() {
        val forventetSvar = OverblikkVisningResponse(listOf("CV"))
        `when`(veilarbveilederKlient.hent()).thenReturn(ResponseEntity.ok(forventetSvar))
        `when`(veilarbveilederKlient.lagre(OverblikkVisningRequest(listOf("CV"))))
            .thenReturn(ResponseEntity.noContent().build())
        `when`(veilarbveilederKlient.slett()).thenReturn(ResponseEntity.noContent().build())

        assertThat(kontroller.hentOverblikkvisningForInnloggetVeileder().body)
            .isEqualTo(forventetSvar)
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
        val kontekster = ArgumentCaptor.forClass(UnleashContext::class.java)
        verify(funksjonsbryter, times(3)).isEnabled(
            eq(FeatureToggle.OVERBLIKK_VISNING_I_VEILARBVEILEDER),
            kontekster.capture()
        )
        assertThat(kontekster.allValues).allSatisfy {
            assertThat(it.userId).hasValue("Z999999")
        }
        verifyNoInteractions(tjeneste)
    }

    @Test
    fun `skal gi like svar og opprett oppdater slett flyt med og uten funksjonsbryter`() {
        doReturn(false).`when`(funksjonsbryter).isEnabled(
            eq(FeatureToggle.OVERBLIKK_VISNING_I_VEILARBVEILEDER),
            any(UnleashContext::class.java)
        )

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

        val lokaleSvar = kjorOpprettOppdaterSlettFlyt()
        doReturn(true).`when`(funksjonsbryter).isEnabled(
            eq(FeatureToggle.OVERBLIKK_VISNING_I_VEILARBVEILEDER),
            any(UnleashContext::class.java)
        )
        val svarFraVeilarbveileder = kjorOpprettOppdaterSlettFlyt()

        assertThat(svarFraVeilarbveileder).isEqualTo(lokaleSvar)
        assertThat(lokaleSvar.statuskoder).containsExactly(200, 204, 200, 204, 200, 204, 200)
        assertThat(lokaleSvar.visninger).containsExactly(
            emptyList<String>(),
            listOf("CV", "Personalia"),
            listOf("CV"),
            emptyList<String>()
        )
        val kontekster = ArgumentCaptor.forClass(UnleashContext::class.java)
        verify(funksjonsbryter, times(14)).isEnabled(
            eq(FeatureToggle.OVERBLIKK_VISNING_I_VEILARBVEILEDER),
            kontekster.capture()
        )
        assertThat(kontekster.allValues).allSatisfy {
            assertThat(it.userId).hasValue("Z999999")
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `skal sperre lagring oppdatering og sletting under migrering`(rutingAktivert: Boolean) {
        `when`(funksjonsbryter.isEnabled(FeatureToggle.OVERBLIKK_VISNING_SKRIVESPERRE))
            .thenReturn(true)
        doReturn(rutingAktivert).`when`(funksjonsbryter).isEnabled(
            eq(FeatureToggle.OVERBLIKK_VISNING_I_VEILARBVEILEDER),
            any(UnleashContext::class.java)
        )
        val mvc = MockMvcBuilders.standaloneSetup(kontroller).build()
        val foresporsler = listOf(
            post("/api/overblikkvisning").content("""{"overblikkVisning":["CV","Personalia"]}"""),
            post("/api/overblikkvisning").content("""{"overblikkVisning":["CV"]}"""),
            delete("/api/overblikkvisning")
        )

        foresporsler.forEach {
            mvc.perform(it.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.detail").value(
                    "Lagring, oppdatering og sletting av overblikkvisning er midlertidig sperret under datamigrering."
                ))
        }

        verifyNoInteractions(tjeneste, veilarbveilederKlient)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `skal tillate lesing mens skrivesperren er aktiv`(rutingAktivert: Boolean) {
        `when`(funksjonsbryter.isEnabled(FeatureToggle.OVERBLIKK_VISNING_SKRIVESPERRE))
            .thenReturn(true)
        doReturn(rutingAktivert).`when`(funksjonsbryter).isEnabled(
            eq(FeatureToggle.OVERBLIKK_VISNING_I_VEILARBVEILEDER),
            any(UnleashContext::class.java)
        )
        if (rutingAktivert) {
            `when`(veilarbveilederKlient.hent())
                .thenReturn(ResponseEntity.ok(OverblikkVisningResponse(listOf("CV"))))
        } else {
            `when`(tjeneste.hentOverblikkVisning(VeilederId.of("Z999999")))
                .thenReturn(Optional.of(OverblikkVisning(UUID.randomUUID(), listOf("CV"))))
        }

        MockMvcBuilders.standaloneSetup(kontroller).build()
            .perform(get("/api/overblikkvisning").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.overblikkVisning[0]").value("CV"))

        if (rutingAktivert) {
            verify(veilarbveilederKlient).hent()
            verifyNoInteractions(tjeneste)
        } else {
            verify(tjeneste).hentOverblikkVisning(VeilederId.of("Z999999"))
            verifyNoInteractions(veilarbveilederKlient)
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
