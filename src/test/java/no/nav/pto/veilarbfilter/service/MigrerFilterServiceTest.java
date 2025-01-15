package no.nav.pto.veilarbfilter.service;

import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.domene.value.ArenaInnsatsgruppe;
import no.nav.pto.veilarbfilter.domene.value.Hovedmal;
import no.nav.pto.veilarbfilter.repository.FilterRepository;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.jdbc.core.JdbcTemplate;

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
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE filter CASCADE");
    }

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
        assertThat(mineLagredeFilterRepository.hentFilter(filterId1, false).get().getFilterValg()).isEqualTo(forventetMigrertfilterMedKunHovedmal);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId2, false).get().getFilterValg()).isEqualTo(forventetMigrertfilterUtenHovedmal);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId3, false).get().getFilterValg()).isEqualTo(forventetMigrertFilterMedMangeHovedmal);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId4, false).get().getFilterValg()).isEqualTo(forventetMigrertfilterMedKunHovedmal);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId5, false).get().getFilterValg()).isEqualTo(forventetMigrertFilterMedMangeHovedmal);
    }

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
        PortefoljeFilter faktiskMigrertFilter = mineLagredeFilterRepository.hentFilter(filterId, false).get().getFilterValg();
        assertThat(faktiskMigrertFilter).isEqualTo(forventetMigrertFilterMedMangeHovedmal);
    }

    @Test
    void migrer_filter_skal_migrere_filter_riktig_når_bruker_har_mange_filter_valg_satt_inkludert_hovedmål() {
        // Given
        String veilederId = "01010111111";

        PortefoljeFilter filterMedMangeFiltervalgInkludertHovedmål = new PortefoljeFilter();
        filterMedMangeFiltervalgInkludertHovedmål.setHovedmal(List.of(SKAFFEA.name(), BEHOLDEA.name(), OKEDELT.name()));
        filterMedMangeFiltervalgInkludertHovedmål.setFerdigfilterListe(List.of("ER_SYKMELDT_MED_ARBEIDSGIVER", "UFORDELTE_BRUKERE"));
        filterMedMangeFiltervalgInkludertHovedmål.setAlder(List.of("25-29"));
        filterMedMangeFiltervalgInkludertHovedmål.setKjonn("M");
        filterMedMangeFiltervalgInkludertHovedmål.setFodselsdagIMnd(List.of("17"));
        filterMedMangeFiltervalgInkludertHovedmål.setFormidlingsgruppe(List.of("IARBS"));
        filterMedMangeFiltervalgInkludertHovedmål.setServicegruppe(List.of("OPPFI"));
        filterMedMangeFiltervalgInkludertHovedmål.setYtelse("DAGPENGER_MED_PERMITTERING");
        filterMedMangeFiltervalgInkludertHovedmål.setAktiviteterForenklet(List.of("STILLING"));
        filterMedMangeFiltervalgInkludertHovedmål.setRettighetsgruppe(List.of("AAP"));
        filterMedMangeFiltervalgInkludertHovedmål.setRegistreringstype(List.of("SAGT_OPP", "MISTET_JOBBEN", "INGEN_SVAR"));
        filterMedMangeFiltervalgInkludertHovedmål.setCvJobbprofil("HAR_DELT_CV");
        filterMedMangeFiltervalgInkludertHovedmål.setUtdanning(List.of("VIDEREGAENDE_GRUNNUTDANNING"));
        filterMedMangeFiltervalgInkludertHovedmål.setUtdanningGodkjent(List.of("NEI", "VET_IKKE"));
        filterMedMangeFiltervalgInkludertHovedmål.setUtdanningBestatt(List.of("INGEN_DATA"));
        filterMedMangeFiltervalgInkludertHovedmål.setTolkebehov(List.of("TEGNSPRAAKTOLK"));
        filterMedMangeFiltervalgInkludertHovedmål.setVisGeografiskBosted(List.of("1"));

        int filterId = mineLagredeFilterRepository.lagreFilter(veilederId, new NyttFilterModel("filter med mange filtervalg inkludert hovedmål", filterMedMangeFiltervalgInkludertHovedmål)).get().getFilterId();

        // When
        migrerFilterService.migrerFilter(BATCH_STORRELSE_ALLE);

        // Then
        int filterMedGammeltHovedmål = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(FilterRepository.ARENA_HOVEDMAL_FILTERVALG_JSON_KEY);
        assertThat(filterMedGammeltHovedmål).isEqualTo(0);

        int filterMedNyeHovedmål = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(FilterRepository.GJELDENDE_VEDTAK_HOVEDMAL_FILTERVALG_JSON_KEY);
        assertThat(filterMedNyeHovedmål).isEqualTo(1);

        PortefoljeFilter forventetMigrertFilterMedMangeHovedmal = new PortefoljeFilter();
        forventetMigrertFilterMedMangeHovedmal.setHovedmalGjeldendeVedtak14a(List.of(BEHOLDE_ARBEID.name(), OKE_DELTAKELSE.name(), SKAFFE_ARBEID.name()));
        forventetMigrertFilterMedMangeHovedmal.setFerdigfilterListe(List.of("ER_SYKMELDT_MED_ARBEIDSGIVER", "UFORDELTE_BRUKERE"));
        forventetMigrertFilterMedMangeHovedmal.setAlder(List.of("25-29"));
        forventetMigrertFilterMedMangeHovedmal.setKjonn("M");
        forventetMigrertFilterMedMangeHovedmal.setFodselsdagIMnd(List.of("17"));
        forventetMigrertFilterMedMangeHovedmal.setFormidlingsgruppe(List.of("IARBS"));
        forventetMigrertFilterMedMangeHovedmal.setServicegruppe(List.of("OPPFI"));
        forventetMigrertFilterMedMangeHovedmal.setYtelse("DAGPENGER_MED_PERMITTERING");
        forventetMigrertFilterMedMangeHovedmal.setAktiviteterForenklet(List.of("STILLING"));
        forventetMigrertFilterMedMangeHovedmal.setRettighetsgruppe(List.of("AAP"));
        forventetMigrertFilterMedMangeHovedmal.setRegistreringstype(List.of("SAGT_OPP", "MISTET_JOBBEN", "INGEN_SVAR"));
        forventetMigrertFilterMedMangeHovedmal.setCvJobbprofil("HAR_DELT_CV");
        forventetMigrertFilterMedMangeHovedmal.setUtdanning(List.of("VIDEREGAENDE_GRUNNUTDANNING"));
        forventetMigrertFilterMedMangeHovedmal.setUtdanningGodkjent(List.of("NEI", "VET_IKKE"));
        forventetMigrertFilterMedMangeHovedmal.setUtdanningBestatt(List.of("INGEN_DATA"));
        forventetMigrertFilterMedMangeHovedmal.setTolkebehov(List.of("TEGNSPRAAKTOLK"));
        forventetMigrertFilterMedMangeHovedmal.setVisGeografiskBosted(List.of("1"));
        PortefoljeFilter faktiskMigrertFilter = mineLagredeFilterRepository.hentFilter(filterId, false).get().getFilterValg();
        assertThat(faktiskMigrertFilter).isEqualTo(forventetMigrertFilterMedMangeHovedmal);
    }
}