package no.nav.pto.veilarbfilter.service;

import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.domene.value.ArenaInnsatsgruppe;
import no.nav.pto.veilarbfilter.domene.value.Hovedmal;
import no.nav.pto.veilarbfilter.repository.FilterRepository;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.List;
import java.util.Optional;

import static no.nav.pto.veilarbfilter.domene.value.ArenaHovedmal.*;
import static no.nav.pto.veilarbfilter.domene.value.ArenaInnsatsgruppe.BATT;
import static no.nav.pto.veilarbfilter.domene.value.Hovedmal.*;
import static no.nav.pto.veilarbfilter.service.MigrerFilterService.BATCH_STORRELSE_ALLE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@WebMvcTest
class MigrerFilterServiceTest extends AbstractTest {

    @Autowired
    private MineLagredeFilterRepository mineLagredeFilterRepository;
    @Autowired
    private MigrerFilterService migrerFilterService;
    @Autowired
    private FilterRepository filterRepository;

    @Test
    void migrer_filter_skal_migrere_filter_riktig_når_bruker_kun_har_arena_hovedmal_fra_før() {
        // Given
        // Kun folk som har filter som er migreringsverdige skal migrerast
        String veilederId1 = "01010111111";
        String veilederId2 = "02020222222";

        PortefoljeFilter filterMedHovedmal = new PortefoljeFilter();
        filterMedHovedmal.setHovedmal(List.of(SKAFFEA.name()));

        PortefoljeFilter filterUtenHovedmal = new PortefoljeFilter();
        filterUtenHovedmal.setInnsatsgruppe(List.of(BATT.name()));

        PortefoljeFilter filterMedMangeHovedmal = new PortefoljeFilter();
        filterMedMangeHovedmal.setHovedmal(List.of(SKAFFEA.name(), BEHOLDEA.name(), OKEDELT.name()));

        int filterId1 = mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter", filterMedHovedmal)).get().getFilterId();
        int filterId2 = mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter uten hovedmål", filterUtenHovedmal)).get().getFilterId();
        int filterId3 = mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter med mange hovedmål", filterMedMangeHovedmal)).get().getFilterId();
        int filterId4 = mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter", filterMedHovedmal)).get().getFilterId();
        int filterId5 = mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter med mange hovedmål", filterMedMangeHovedmal)).get().getFilterId();

        // When
        migrerFilterService.migrerFilter(BATCH_STORRELSE_ALLE);

        // Then
        int filterMedGammeltHovedmål = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(FilterRepository.ARENA_HOVEDMAL_FILTERVALG_JSON_KEY);
        assertThat(filterMedGammeltHovedmål).isEqualTo(0);

        int filterMedNyeHovedmål = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(FilterRepository.GJELDENDE_VEDTAK_HOVEDMAL_FILTERVALG_JSON_KEY);
        assertThat(filterMedNyeHovedmål).isEqualTo(4);

        PortefoljeFilter forventetMigrertfilterMedKunHovedmal = new PortefoljeFilter();
        forventetMigrertfilterMedKunHovedmal.setHovedmalGjeldendeVedtak14a(List.of(SKAFFE_ARBEID.name()));
        PortefoljeFilter forventetMigrertfilterUtenHovedmal = new PortefoljeFilter();
        forventetMigrertfilterUtenHovedmal.setInnsatsgruppe(List.of(BATT.name()));
        PortefoljeFilter forventetMigrertFilterMedMangeHovedmal = new PortefoljeFilter();
        forventetMigrertFilterMedMangeHovedmal.setHovedmalGjeldendeVedtak14a(List.of(BEHOLDE_ARBEID.name(), OKE_DELTAKELSE.name(), SKAFFE_ARBEID.name()));
        assertThat(mineLagredeFilterRepository.hentFilter(filterId1).get().getFilterValg()).isEqualTo(forventetMigrertfilterMedKunHovedmal);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId2).get().getFilterValg()).isEqualTo(forventetMigrertfilterUtenHovedmal);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId3).get().getFilterValg()).isEqualTo(forventetMigrertFilterMedMangeHovedmal);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId4).get().getFilterValg()).isEqualTo(forventetMigrertfilterMedKunHovedmal);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId5).get().getFilterValg()).isEqualTo(forventetMigrertFilterMedMangeHovedmal);
    }

    // TODO: Ein test der det finnst gjeldande vedtak hovedmål OG arena hovedmål frå før, og som verifiserer at vi "slår saman" og migrerer riktig.
    @Test
    void migrer_filter_skal_migrere_filter_riktig_når_bruker_har_arena_hovedmal_og_gjeldende_vedtak_hovedmål_fra_før() {
        // Given
        // Kun folk som har filter som er migreringsverdige skal migrerast
        String veilederId = "01010111111";

        PortefoljeFilter filterMedMangeArenaHovedmalOgGjeldendeVedtakHovedmal = new PortefoljeFilter();
        filterMedMangeArenaHovedmalOgGjeldendeVedtakHovedmal.setHovedmal(List.of(SKAFFEA.name(), BEHOLDEA.name(), OKEDELT.name()));
        filterMedMangeArenaHovedmalOgGjeldendeVedtakHovedmal.setHovedmalGjeldendeVedtak14a(List.of(SKAFFE_ARBEID.name(), BEHOLDE_ARBEID.name()));

        int filterId = mineLagredeFilterRepository.lagreFilter(veilederId, new NyttFilterModel("filter med arena hovedmål og gjeldende vedtak hovedmål", filterMedMangeArenaHovedmalOgGjeldendeVedtakHovedmal)).get().getFilterId();

        // When
        migrerFilterService.migrerFilter(BATCH_STORRELSE_ALLE);

        // Then
        int filterMedGammeltHovedmål = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(FilterRepository.ARENA_HOVEDMAL_FILTERVALG_JSON_KEY);
        assertThat(filterMedGammeltHovedmål).isEqualTo(0);

        int filterMedNyeHovedmål = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(FilterRepository.GJELDENDE_VEDTAK_HOVEDMAL_FILTERVALG_JSON_KEY);
        assertThat(filterMedNyeHovedmål).isEqualTo(1);

        PortefoljeFilter forventetMigrertFilterMedMangeHovedmal = new PortefoljeFilter();
        forventetMigrertFilterMedMangeHovedmal.setHovedmalGjeldendeVedtak14a(List.of(BEHOLDE_ARBEID.name(), OKE_DELTAKELSE.name(), SKAFFE_ARBEID.name()));
        PortefoljeFilter faktiskMigrertFilter = mineLagredeFilterRepository.hentFilter(filterId).get().getFilterValg();
        assertThat(faktiskMigrertFilter).isEqualTo(forventetMigrertFilterMedMangeHovedmal);
    }

    // TODO: Ein test som har data i dei andre lagra felta også, som verifiserer at vi ikkje migrerer andre data enn berre dei vi skal. NB: Spesielt viktig å verifisere at vi ikkje overskriver data for registreringstyper (FilterRepository#128).
}