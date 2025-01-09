package no.nav.pto.veilarbfilter.domene;

import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.value.ArenaHovedmal;
import no.nav.pto.veilarbfilter.domene.value.Hovedmal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@WebMvcTest
@ActiveProfiles({"test"})
public class ArenaHovedmalTest extends AbstractTest {
    @Test
    public void testMapTilHovedmalGjeldendeVedtak14a() {
        // given
        ArenaHovedmal skaffeArbeidArena = ArenaHovedmal.SKAFFEA;
        ArenaHovedmal beholdeArbeidArena = ArenaHovedmal.BEHOLDEA;
        ArenaHovedmal okeDeltakelseArbeidArena = ArenaHovedmal.OKEDELT;

        // when
        Hovedmal skaffeArbeidMappingresultat = ArenaHovedmal.mapTilHovedmalGjeldendeVedtak14a(skaffeArbeidArena);
        Hovedmal beholdeArbeidMappingresultat = ArenaHovedmal.mapTilHovedmalGjeldendeVedtak14a(beholdeArbeidArena);
        Hovedmal okeDeltakelseMappingresultat = ArenaHovedmal.mapTilHovedmalGjeldendeVedtak14a(okeDeltakelseArbeidArena);

        // then
        Hovedmal skaffeArbeidForventet = Hovedmal.SKAFFE_ARBEID;
        Hovedmal beholdeArbeidForventet = Hovedmal.BEHOLDE_ARBEID;
        Hovedmal okeDeltakelseForventet = Hovedmal.OKE_DELTAKELSE;
        Assertions.assertEquals(skaffeArbeidForventet, skaffeArbeidMappingresultat);
        Assertions.assertEquals(beholdeArbeidForventet, beholdeArbeidMappingresultat);
        Assertions.assertEquals(okeDeltakelseForventet, okeDeltakelseMappingresultat);
    }

    @Test
    public void testMapListeTilGjeldendeVedtak14aHovedmal() {
        // given
        List<ArenaHovedmal> arenaHovedmalListe = List.of(
                ArenaHovedmal.BEHOLDEA,
                ArenaHovedmal.SKAFFEA,
                ArenaHovedmal.OKEDELT
        );

        // when
        List<Hovedmal> result = arenaHovedmalListe.stream().map(it -> ArenaHovedmal.mapTilHovedmalGjeldendeVedtak14a(it)).toList();

        // then
        List<Hovedmal> expected = List.of(
                Hovedmal.BEHOLDE_ARBEID,
                Hovedmal.SKAFFE_ARBEID,
                Hovedmal.OKE_DELTAKELSE
        );
        Assertions.assertEquals(expected, result);
    }
}
