package no.nav.pto.veilarbfilter.service;

import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.domene.value.NyeRegistreringstyper;
import no.nav.pto.veilarbfilter.domene.value.UtdaterteRegistreringstyper;
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
import static no.nav.pto.veilarbfilter.domene.value.ArenaInnsatsgruppe.*;
import static no.nav.pto.veilarbfilter.domene.value.Hovedmal.*;
import static no.nav.pto.veilarbfilter.domene.value.Innsatsgruppe.*;
import static no.nav.pto.veilarbfilter.repository.FilterRepository.*;
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
        migrerFilterService.migrerFilterMedFiltertype(BATCH_STORRELSE_ALLE, ARENA_HOVEDMAL_FILTERVALG_JSON_KEY);

        // Then
        int filterMedGammeltHovedmål = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(ARENA_HOVEDMAL_FILTERVALG_JSON_KEY);
        assertThat(filterMedGammeltHovedmål).isEqualTo(0);

        int filterMedNyeHovedmål = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(FilterRepository.GJELDENDE_VEDTAK_HOVEDMAL_FILTERVALG_JSON_KEY);
        assertThat(filterMedNyeHovedmål).isEqualTo(4);

        PortefoljeFilter forventetMigrertfilterMedKunHovedmal = new PortefoljeFilter();
        forventetMigrertfilterMedKunHovedmal.setHovedmalGjeldendeVedtak14a(List.of(SKAFFE_ARBEID.name()));
        PortefoljeFilter forventetMigrertfilterUtenHovedmal = filterUtenHovedmal;
        PortefoljeFilter forventetMigrertFilterMedMangeHovedmal = new PortefoljeFilter();
        forventetMigrertFilterMedMangeHovedmal.setHovedmalGjeldendeVedtak14a(List.of(BEHOLDE_ARBEID.name(), OKE_DELTAKELSE.name(), SKAFFE_ARBEID.name()));
        assertThat(mineLagredeFilterRepository.hentFilter(filterId1, false).get().getFilterValg()).isEqualTo(forventetMigrertfilterMedKunHovedmal);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId2, false).get().getFilterValg()).isEqualTo(forventetMigrertfilterUtenHovedmal);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId3, false).get().getFilterValg()).isEqualTo(forventetMigrertFilterMedMangeHovedmal);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId4, false).get().getFilterValg()).isEqualTo(forventetMigrertfilterMedKunHovedmal);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId5, false).get().getFilterValg()).isEqualTo(forventetMigrertFilterMedMangeHovedmal);
    }

    @Test
    void migrer_filter_skal_migrere_filter_riktig_når_bruker_kun_har_arena_innsatsgruppe_fra_før() {
        // Given
        // Kun folk som har filter som er migreringsverdige skal migrerast
        String veilederId1 = "01010111111";
        String veilederId2 = "02020222222";

        PortefoljeFilter filterMedInnsatsgruppe = new PortefoljeFilter();
        filterMedInnsatsgruppe.setInnsatsgruppe(List.of(BFORM.name()));

        PortefoljeFilter filterUtenInnsatsgruppe = new PortefoljeFilter();
        filterUtenInnsatsgruppe.setHovedmal(List.of(SKAFFEA.name()));

        PortefoljeFilter filterMedMangeInnsatsgruppe = new PortefoljeFilter();
        filterMedMangeInnsatsgruppe.setInnsatsgruppe(List.of(BATT.name(), BFORM.name(), IKVAL.name(), VARIG.name()));

        int filterId1 = mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter", filterMedInnsatsgruppe)).get().getFilterId();
        int filterId2 = mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter uten innsatsgruppe", filterUtenInnsatsgruppe)).get().getFilterId();
        int filterId3 = mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter med mange innsatsgruppe", filterMedMangeInnsatsgruppe)).get().getFilterId();
        int filterId4 = mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter", filterMedInnsatsgruppe)).get().getFilterId();
        int filterId5 = mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter med mange innsatsgruppe", filterMedMangeInnsatsgruppe)).get().getFilterId();

        // When
        migrerFilterService.migrerFilterMedFiltertype(BATCH_STORRELSE_ALLE, ARENA_INNSATSGRUPPE_FILTERVALG_JSON_KEY);

        // Then
        int filterMedGammelInnsatsgruppe = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(ARENA_INNSATSGRUPPE_FILTERVALG_JSON_KEY);
        assertThat(filterMedGammelInnsatsgruppe).isEqualTo(0);

        int filterMedNyeInnsatsgruppe = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(FilterRepository.GJELDENDE_VEDTAK_INNSATSGRUPPE_FILTERVALG_JSON_KEY);
        assertThat(filterMedNyeInnsatsgruppe).isEqualTo(4);

        PortefoljeFilter forventetMigrertfilterMedKunInnsatsgruppe = new PortefoljeFilter();
        forventetMigrertfilterMedKunInnsatsgruppe.setInnsatsgruppeGjeldendeVedtak14a(List.of(SITUASJONSBESTEMT_INNSATS.name()));
        PortefoljeFilter forventetMigrertfilterUtenInnsatsgruppe = filterUtenInnsatsgruppe;
        PortefoljeFilter forventetMigrertFilterMedMangeInnsatsgruppe = new PortefoljeFilter();
        forventetMigrertFilterMedMangeInnsatsgruppe.setInnsatsgruppeGjeldendeVedtak14a(List.of(GRADERT_VARIG_TILPASSET_INNSATS.name(), SITUASJONSBESTEMT_INNSATS.name(), SPESIELT_TILPASSET_INNSATS.name(), STANDARD_INNSATS.name(), VARIG_TILPASSET_INNSATS.name()));
        assertThat(mineLagredeFilterRepository.hentFilter(filterId1, false).get().getFilterValg()).isEqualTo(forventetMigrertfilterMedKunInnsatsgruppe);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId2, false).get().getFilterValg()).isEqualTo(forventetMigrertfilterUtenInnsatsgruppe);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId3, false).get().getFilterValg()).isEqualTo(forventetMigrertFilterMedMangeInnsatsgruppe);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId4, false).get().getFilterValg()).isEqualTo(forventetMigrertfilterMedKunInnsatsgruppe);
        assertThat(mineLagredeFilterRepository.hentFilter(filterId5, false).get().getFilterValg()).isEqualTo(forventetMigrertFilterMedMangeInnsatsgruppe);
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
        migrerFilterService.migrerFilterMedFiltertype(BATCH_STORRELSE_ALLE, ARENA_HOVEDMAL_FILTERVALG_JSON_KEY);

        // Then
        int filterMedGammeltHovedmål = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(ARENA_HOVEDMAL_FILTERVALG_JSON_KEY);
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
        migrerFilterService.migrerFilterMedFiltertype(BATCH_STORRELSE_ALLE, ARENA_HOVEDMAL_FILTERVALG_JSON_KEY);

        // Then
        int filterMedGammeltHovedmål = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(ARENA_HOVEDMAL_FILTERVALG_JSON_KEY);
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

    @Test
    void test_for_når_vi_vil_se_logging() {
        // Given
        String veilederId1 = "01010111111";
        String veilederId2 = "02020222222";

        PortefoljeFilter filterMedHovedmal = new PortefoljeFilter();
        filterMedHovedmal.setHovedmal(List.of(SKAFFEA.name()));

        PortefoljeFilter filterMedMangeHovedmal = new PortefoljeFilter();
        filterMedMangeHovedmal.setHovedmal(List.of(SKAFFEA.name(), BEHOLDEA.name(), OKEDELT.name()));

        PortefoljeFilter filterMedInnsatsgruppe = new PortefoljeFilter();
        filterMedInnsatsgruppe.setInnsatsgruppe(List.of(BFORM.name()));

        PortefoljeFilter filterMedMangeInnsatsgrupper = new PortefoljeFilter();
        filterMedMangeInnsatsgrupper.setInnsatsgruppe(List.of(BATT.name(), BFORM.name(), IKVAL.name(), VARIG.name()));

        mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter med hovedmål, veileder 1", filterMedHovedmal)).get().getFilterId();
        mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter med mange hovedmål, veileder 1", filterMedMangeHovedmal)).get().getFilterId();
        mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter med hovedmål, veileder 2", filterMedHovedmal)).get().getFilterId();
        mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter med mange hovedmål, veileder 2", filterMedMangeHovedmal)).get().getFilterId();
        mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter med innsatsgruppe, veileder 1", filterMedInnsatsgruppe)).get().getFilterId();
        mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter med mange innsatsgrupper, veileder 1", filterMedMangeInnsatsgrupper)).get().getFilterId();
        mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter med innsatsgruppe, veileder 2", filterMedInnsatsgruppe)).get().getFilterId();
        mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter med mange innsatsgrupper, veileder 2", filterMedMangeInnsatsgrupper)).get().getFilterId();

        // When
        Optional<MigrerFilterService.FilterMigreringResultat> resultatAvMigrering = migrerFilterService.migrerFilter(100);

        // Then
        assertThat(resultatAvMigrering.get().hovedmal().get().totalt()).isEqualTo(4);
        assertThat(resultatAvMigrering.get().hovedmal().get().forsoktMigrert()).isEqualTo(4);
        assertThat(resultatAvMigrering.get().hovedmal().get().faktiskMigrert()).isEqualTo(4);
        assertThat(resultatAvMigrering.get().innsatsgruppe().get().totalt()).isEqualTo(4);
        assertThat(resultatAvMigrering.get().innsatsgruppe().get().forsoktMigrert()).isEqualTo(4);
        assertThat(resultatAvMigrering.get().innsatsgruppe().get().faktiskMigrert()).isEqualTo(4);
    }

    @Test
    void migrer_filter_skal_migrere_registreringstypefilter_riktig_når_det_også_er_nye_registreringstyper_i_filteret() {
        // Given
        String veilederId = "Z111111";

        PortefoljeFilter filterMedRegistreringstyper = new PortefoljeFilter();
        filterMedRegistreringstyper.setRegistreringstype(List.of(
                UtdaterteRegistreringstyper.MISTET_JOBBEN.name(),
                UtdaterteRegistreringstyper.JOBB_OVER_2_AAR.name(),
                UtdaterteRegistreringstyper.VIL_FORTSETTE_I_JOBB.name(),
                NyeRegistreringstyper.HAR_BLITT_SAGT_OPP.name(),
                NyeRegistreringstyper.IKKE_VAERT_I_JOBB_SISTE_2_AAR.name(),
                NyeRegistreringstyper.ANNET.name()));

        int filterId = mineLagredeFilterRepository.lagreFilter(veilederId, new NyttFilterModel("Filter med registreringstyper", filterMedRegistreringstyper)).get().getFilterId();

        // When
        migrerFilterService.migrerFilterMedFiltertype(BATCH_STORRELSE_ALLE, REGISTRERINGSTYPE_FILTERVALG_JSON_KEY);

        // Then
//        int  filterMedUtdaterteRegistreringstyperEtterMigrering = filterRepository.tellMineFilterSomInneholderUtdaterteRegistreringstyper();
//        assertThat(filterMedUtdaterteRegistreringstyperEtterMigrering).isEqualTo(0);

        int filterMedRegistreringstyperEtterMigrering = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(REGISTRERINGSTYPE_FILTERVALG_JSON_KEY);
        assertThat(filterMedRegistreringstyperEtterMigrering).isEqualTo(1);

        PortefoljeFilter forventetFilterMedRegistreringstyper = new PortefoljeFilter();
        forventetFilterMedRegistreringstyper.setRegistreringstype(List.of(NyeRegistreringstyper.ANNET.name(), NyeRegistreringstyper.HAR_BLITT_SAGT_OPP.name(), NyeRegistreringstyper.IKKE_VAERT_I_JOBB_SISTE_2_AAR.name()));
        PortefoljeFilter faktiskMigrertFilter = mineLagredeFilterRepository.hentFilter(filterId, false).get().getFilterValg();

        assertThat(faktiskMigrertFilter.getRegistreringstype().size()).isEqualTo(forventetFilterMedRegistreringstyper.getRegistreringstype().size());
        assertThat(faktiskMigrertFilter.getRegistreringstype()).isEqualTo(forventetFilterMedRegistreringstyper.getRegistreringstype());
        assertThat(faktiskMigrertFilter).isEqualTo(forventetFilterMedRegistreringstyper);
    }

    @Test
    void migrer_filter_skal_migrere_registreringstypefilter_riktig() {
        // Given
        String veilederId = "Z111111";

        PortefoljeFilter filterMistetJobben = new PortefoljeFilter();
        filterMistetJobben.setRegistreringstype(List.of(UtdaterteRegistreringstyper.MISTET_JOBBEN.name()));
        PortefoljeFilter filterJobbOver2Aar = new PortefoljeFilter();
        filterJobbOver2Aar.setRegistreringstype(List.of(UtdaterteRegistreringstyper.JOBB_OVER_2_AAR.name()));
        PortefoljeFilter filterVilFortsetteIJobb = new PortefoljeFilter();
        filterVilFortsetteIJobb.setRegistreringstype(List.of(UtdaterteRegistreringstyper.VIL_FORTSETTE_I_JOBB.name()));

        int filterIdMistetJobben = mineLagredeFilterRepository.lagreFilter(veilederId, new NyttFilterModel("Filter 1", filterMistetJobben)).get().getFilterId();
        int filterIdJobbOver2Aar = mineLagredeFilterRepository.lagreFilter(veilederId, new NyttFilterModel("Filter 2", filterJobbOver2Aar)).get().getFilterId();
        int filterIdVilFortsetteIJobb = mineLagredeFilterRepository.lagreFilter(veilederId, new NyttFilterModel("Filter 3", filterVilFortsetteIJobb)).get().getFilterId();

        // When
        migrerFilterService.migrerFilterMedFiltertype(BATCH_STORRELSE_ALLE, REGISTRERINGSTYPE_FILTERVALG_JSON_KEY);

        // Then
//        int  filterMedUtdaterteRegistreringstyperEtterMigrering = filterRepository.tellMineFilterSomInneholderUtdaterteRegistreringstyper();
//        assertThat(filterMedUtdaterteRegistreringstyperEtterMigrering).isEqualTo(0);

        int filterMedRegistreringstyperEtterMigrering = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(REGISTRERINGSTYPE_FILTERVALG_JSON_KEY);
        assertThat(filterMedRegistreringstyperEtterMigrering).isEqualTo(3);

        // Alle skal ha blitt migrert til rett verdi
        PortefoljeFilter forventetFilterMistetJobben = new PortefoljeFilter();
        forventetFilterMistetJobben.setRegistreringstype(List.of(NyeRegistreringstyper.HAR_BLITT_SAGT_OPP.name()));
        PortefoljeFilter forventetFilterJobbOver2Aar = new PortefoljeFilter();
        forventetFilterJobbOver2Aar.setRegistreringstype(List.of(NyeRegistreringstyper.IKKE_VAERT_I_JOBB_SISTE_2_AAR.name()));
        PortefoljeFilter forventetFilterVilFortsetteIJobb = new PortefoljeFilter();
        forventetFilterVilFortsetteIJobb.setRegistreringstype(List.of(NyeRegistreringstyper.ANNET.name()));

        PortefoljeFilter faktiskMigrertFilterMistetJobben = mineLagredeFilterRepository.hentFilter(filterIdMistetJobben, false).get().getFilterValg();
        PortefoljeFilter faktiskMigrertFilterJobbOver2Aar = mineLagredeFilterRepository.hentFilter(filterIdJobbOver2Aar, false).get().getFilterValg();
        PortefoljeFilter faktiskMigrertFilterVilFortsetteIJobb = mineLagredeFilterRepository.hentFilter(filterIdVilFortsetteIJobb, false).get().getFilterValg();

        assertThat(faktiskMigrertFilterMistetJobben.getRegistreringstype()).isEqualTo(forventetFilterMistetJobben.getRegistreringstype());
        assertThat(faktiskMigrertFilterJobbOver2Aar.getRegistreringstype()).isEqualTo(forventetFilterJobbOver2Aar.getRegistreringstype());
        assertThat(faktiskMigrertFilterVilFortsetteIJobb.getRegistreringstype()).isEqualTo(forventetFilterVilFortsetteIJobb.getRegistreringstype());
    }

}