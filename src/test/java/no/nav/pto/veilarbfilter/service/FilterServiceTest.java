package no.nav.pto.veilarbfilter.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.domene.value.UtdaterteRegistreringstyper;
import no.nav.pto.veilarbfilter.repository.FilterRepository;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.nav.pto.veilarbfilter.domene.value.ArenaHovedmal.*;
import static no.nav.pto.veilarbfilter.domene.value.Hovedmal.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@WebMvcTest
@ActiveProfiles({"test"})
public class FilterServiceTest extends AbstractTest {
    @Autowired
    private MineLagredeFilterService mineLagredeFilterService;
    @Autowired
    private MigrerFilterService migrerFilterService;
    @Autowired
    private MineLagredeFilterRepository mineLagredeFilterRepository;
    @Autowired
    private FilterRepository filterRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String hovedmalfilterArena = FilterRepository.ARENA_HOVEDMAL_FILTERVALG_JSON_KEY;
    private String hovedmalfilterGjeldendeVedtak = FilterRepository.GJELDENDE_VEDTAK_HOVEDMAL_FILTERVALG_JSON_KEY;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE filter CASCADE");
    }

    @Test
    public void erstattArenahovedmalMedHovedmalGjeldendeVedtak14a() throws JsonProcessingException {
        // given
        String veilederId = "01010111111";
        List<String> arenaHovedmaFilter = List.of(OKEDELT.name(), BEHOLDEA.name(), SKAFFEA.name());
        PortefoljeFilter filterMedHovedmal = new PortefoljeFilter();
        filterMedHovedmal.setHovedmal(arenaHovedmaFilter);

        FilterModel lagretFilter = mineLagredeFilterService.lagreFilter(veilederId, new NyttFilterModel("filter med hovedmal", filterMedHovedmal)).orElseThrow();

        // when
        migrerFilterService.erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalg(lagretFilter);

        // then
        List<String> forventHovedmalGjeldendeVedtakListeSortertAlfabetisk = List.of(BEHOLDE_ARBEID.name(), OKE_DELTAKELSE.name(), SKAFFE_ARBEID.name());

        // TODO burde vi bruke service til å sjekke desse tinga i staden for repo?
        List<FilterModel> filterMedHovedmalGjeldendeVedtak14a = filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterGjeldendeVedtak);
        Assertions.assertEquals(1, filterMedHovedmalGjeldendeVedtak14a.size());

        List<String> gjeldendeVedtakFiltervalg = filterMedHovedmalGjeldendeVedtak14a.getFirst().getFilterValg().getHovedmalGjeldendeVedtak14a();
        Assertions.assertEquals(forventHovedmalGjeldendeVedtakListeSortertAlfabetisk, gjeldendeVedtakFiltervalg);

        Assertions.assertEquals(0, filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena));
    }


    @Test
    public void erstattingAvHovedmalSkalTaMedSegEksisterendeFilterForHovdemalGjeldendeVedtak14a() throws JsonProcessingException {
        // given
        String veilederId = "01010111111";
        List<String> arenaHovedmaFilter = List.of(BEHOLDEA.name());
        List<String> gjeldendeVedtak14aHovedmaFilter = List.of(SKAFFE_ARBEID.name());
        PortefoljeFilter filterMedHovedmal = new PortefoljeFilter();
        filterMedHovedmal.setHovedmal(arenaHovedmaFilter);
        filterMedHovedmal.setHovedmalGjeldendeVedtak14a(gjeldendeVedtak14aHovedmaFilter);

        FilterModel lagretFilter = mineLagredeFilterService.lagreFilter(veilederId, new NyttFilterModel("filter med hovedmal", filterMedHovedmal)).orElseThrow();

        // when
        migrerFilterService.erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalg(lagretFilter);

        // then
        List<String> forventHovedmalGjeldendeVedtakListe = List.of(BEHOLDE_ARBEID.name(), SKAFFE_ARBEID.name());

        // TODO burde vi bruke service til å sjekke desse tinga i staden for repo?
        List<FilterModel> filterMedHovedmalGjeldendeVedtak14a = filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterGjeldendeVedtak);
        Assertions.assertEquals(1, filterMedHovedmalGjeldendeVedtak14a.size());

        List<String> gjeldendeVedtakFiltervalg = filterMedHovedmalGjeldendeVedtak14a.getFirst().getFilterValg().getHovedmalGjeldendeVedtak14a();
        Assertions.assertEquals(forventHovedmalGjeldendeVedtakListe, gjeldendeVedtakFiltervalg);

        Assertions.assertEquals(0, filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena));
    }

    @Test
    public void erstattingAvHovedmalSkalFungereVedOverlapp() throws JsonProcessingException {
        // given
        String veilederId = "01010111111";
        List<String> arenaHovedmaFilter = List.of(BEHOLDEA.name());
        List<String> gjeldendeVedtak14aHovedmaFilter = List.of(BEHOLDE_ARBEID.name(), SKAFFE_ARBEID.name());
        PortefoljeFilter filterMedHovedmal = new PortefoljeFilter();
        filterMedHovedmal.setHovedmal(arenaHovedmaFilter);
        filterMedHovedmal.setHovedmalGjeldendeVedtak14a(gjeldendeVedtak14aHovedmaFilter);

        FilterModel lagretFilter = mineLagredeFilterService.lagreFilter(veilederId, new NyttFilterModel("filter med hovedmal", filterMedHovedmal)).orElseThrow();

        // when
        migrerFilterService.erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalg(lagretFilter);

        // then
        List<String> forventHovedmalGjeldendeVedtakListe = List.of(BEHOLDE_ARBEID.name(), SKAFFE_ARBEID.name());

        // TODO burde vi bruke service til å sjekke desse tinga i staden for repo?
        List<FilterModel> filterMedHovedmalGjeldendeVedtak14a = filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterGjeldendeVedtak);
        Assertions.assertEquals(1, filterMedHovedmalGjeldendeVedtak14a.size());

        List<String> gjeldendeVedtakFiltervalg = filterMedHovedmalGjeldendeVedtak14a.getFirst().getFilterValg().getHovedmalGjeldendeVedtak14a();
        Assertions.assertEquals(forventHovedmalGjeldendeVedtakListe, gjeldendeVedtakFiltervalg);

        Assertions.assertEquals(0, filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena));
    }

    @Test
    void skal_få_ut_samme_registreringstyper_som_i_databasen_ved_henting() {
        // Given
        String veilederId = "Z111111";

        PortefoljeFilter filterMedRegistreringstyper = new PortefoljeFilter();
        filterMedRegistreringstyper.setRegistreringstype(List.of(
                UtdaterteRegistreringstyper.MISTET_JOBBEN.name(),
                UtdaterteRegistreringstyper.JOBB_OVER_2_AAR.name(),
                UtdaterteRegistreringstyper.VIL_FORTSETTE_I_JOBB.name()
        ));


        int filterId = mineLagredeFilterRepository.lagreFilter(veilederId, new NyttFilterModel("Filter med registreringstyper", filterMedRegistreringstyper)).get().getFilterId();

        // When
        FilterModel hentetFilter = mineLagredeFilterRepository.hentFilter(filterId, false).get();

        // Then
        assertThat(hentetFilter.getFilterValg().getRegistreringstype().size()).isEqualTo(3);

        assertThat(hentetFilter.getFilterValg().getRegistreringstype()).isEqualTo(filterMedRegistreringstyper.getRegistreringstype());
        assertThat(hentetFilter.getFilterValg()).isEqualTo(filterMedRegistreringstyper);
    }
}
