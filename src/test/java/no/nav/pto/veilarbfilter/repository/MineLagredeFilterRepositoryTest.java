package no.nav.pto.veilarbfilter.repository;


import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.nav.pto.veilarbfilter.domene.value.ArenaHovedmal.*;
import static no.nav.pto.veilarbfilter.domene.value.ArenaInnsatsgruppe.BATT;
import static no.nav.pto.veilarbfilter.domene.value.Hovedmal.SKAFFE_ARBEID;
import static no.nav.pto.veilarbfilter.domene.value.Innsatsgruppe.STANDARD_INNSATS;

@WebMvcTest
@ActiveProfiles({"test"})
public class MineLagredeFilterRepositoryTest extends AbstractTest {
    @Autowired
    private MineLagredeFilterRepository mineLagredeFilterRepository;
    @Autowired
    private FilterRepository filterRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String hovedmalfilterArena = "hovedmal";
    private String hovedmalfilterGjeldendeVedtak = "hovedmalGjeldendeVedtak14a";
    private String innsatsgruppefilterArena = "innsatsgruppe";
    private String innsatsgruppefilterGjeldendeVedtak = "innsatsgruppeGjeldendeVedtak14a";

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE filter CASCADE");
    }

    @Test
    public void kanLagreOgHenteUtFilterForVeileder() {
        String veilederId = "11223312345";
        PortefoljeFilter portefoljeFilter = new PortefoljeFilter();
        List<String> alleHovedmalfilter = List.of(OKEDELT.name(), BEHOLDEA.name(), SKAFFEA.name());
        portefoljeFilter.setHovedmal(alleHovedmalfilter);
        NyttFilterModel nyttFilter = new NyttFilterModel("Navn på nytt filter", portefoljeFilter);

        mineLagredeFilterRepository.lagreFilter(veilederId, nyttFilter);

        List<FilterModel> result = mineLagredeFilterRepository.finnFilterForFilterBruker(veilederId);
        List<String> hovedmalfilter = result.getFirst().getFilterValg().getHovedmal();
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(hovedmalfilter.size() == 3);
        Assertions.assertTrue(hovedmalfilter.containsAll(alleHovedmalfilter));
    }

    @Test
    public void kanTelleAlleFilterMedGammeltHovedmalfiltervalg() {
        // Får rett når ingen filter er lagra
        Integer antallFilterForInserts = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena);
        Assertions.assertEquals(0, antallFilterForInserts);

        // Får rett ved eitt lagra filter med filtervalet vi ser etter
        String veilederId = "11223312345";
        List<String> alleHovedmalfilter = List.of(OKEDELT.name(), BEHOLDEA.name(), SKAFFEA.name());
        PortefoljeFilter filterMedHovedmal = new PortefoljeFilter();
        filterMedHovedmal.setHovedmal(alleHovedmalfilter);

        mineLagredeFilterRepository.lagreFilter(veilederId, new NyttFilterModel("filter med hovedmal", filterMedHovedmal));

        Integer antallFilterEtterInsertAvHovedmalfilter = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena);
        Assertions.assertEquals(1, antallFilterEtterInsertAvHovedmalfilter);

        // Får rett når to lagra filter, der berre det eine inneheld filtervalet vi ser etter
        PortefoljeFilter filterUtenHovedmal = new PortefoljeFilter();
        filterUtenHovedmal.setVeiledere(List.of("Z123456", "Y123456", "X123456"));

        mineLagredeFilterRepository.lagreFilter(veilederId, new NyttFilterModel("filter uten hovedmal", filterUtenHovedmal));

        Integer antallFilterEtterInsertAvIkkehovedmalfilter = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena);
        Assertions.assertEquals(1, antallFilterEtterInsertAvIkkehovedmalfilter);
    }

    @Test
    public void kanTelleFiltervalgForAlleHovedmalOgInnsatsgruppefiltervalg() {
        // Given
        String veilederId = "11223312345";

        PortefoljeFilter filterMedAlleMigreringsfiltervalg = new PortefoljeFilter();
        filterMedAlleMigreringsfiltervalg.setHovedmal(List.of(SKAFFEA.name()));
        filterMedAlleMigreringsfiltervalg.setHovedmalGjeldendeVedtak14a(List.of(SKAFFE_ARBEID.name()));
        filterMedAlleMigreringsfiltervalg.setInnsatsgruppe(List.of(BATT.name()));
        filterMedAlleMigreringsfiltervalg.setInnsatsgruppeGjeldendeVedtak14a(List.of(STANDARD_INNSATS.name()));

        mineLagredeFilterRepository.lagreFilter(veilederId, new NyttFilterModel("filter", filterMedAlleMigreringsfiltervalg));

        // When/Then
        Assertions.assertEquals(1, filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena));
        Assertions.assertEquals(1, filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterGjeldendeVedtak));
        Assertions.assertEquals(1, filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(innsatsgruppefilterArena));
        Assertions.assertEquals(1, filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(innsatsgruppefilterGjeldendeVedtak));
    }

    @Test
    public void kanTelleFiltervalgNarDetErFilterForFlereBrukere() {
        // Given
        String veilederId1 = "01010111111";
        String veilederId2 = "02020222222";
        PortefoljeFilter filterMedHovedmal = new PortefoljeFilter();
        filterMedHovedmal.setHovedmal(List.of(SKAFFEA.name()));

        mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter", filterMedHovedmal));
        mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter", filterMedHovedmal));

        // When
        Integer antallFilter = filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena);

        // Then
        Assertions.assertEquals(2, antallFilter);
    }

    @Test
    public void kanHenteUtEtBestemtAntallFilterSomInneholderFiltervalg() {
        // Given
        String veilederId1 = "01010111111";
        String veilederId2 = "02020222222";

        PortefoljeFilter filterMedHovedmal = new PortefoljeFilter();
        filterMedHovedmal.setHovedmal(List.of(SKAFFEA.name()));

        PortefoljeFilter filterMedMangeHovedmal = new PortefoljeFilter();
        filterMedMangeHovedmal.setHovedmal(List.of(SKAFFEA.name(), BEHOLDEA.name(), OKEDELT.name()));

        PortefoljeFilter filterUtenHovedmal = new PortefoljeFilter();
        filterUtenHovedmal.setInnsatsgruppe(List.of(BATT.name()));

        mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter uten hovedmål", filterUtenHovedmal));
        mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter uten hovedmål", filterUtenHovedmal));

        mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter 1", filterMedHovedmal));
        mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter 2", filterMedMangeHovedmal));
        mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter 3", filterMedHovedmal));
        mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter 4", filterMedMangeHovedmal));

        // when
        List<FilterModel> resultHent1 = filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena, 1);
        List<FilterModel> resultHent4 = filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena, 4);
        List<FilterModel> resultHent100 = filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena, 100);
        List<FilterModel> resultHentAlle = filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena);

        // then
        Assertions.assertEquals(1, resultHent1.size());
        Assertions.assertEquals(4, resultHent4.size());
        Assertions.assertEquals(4, resultHent100.size());
        Assertions.assertEquals(4, resultHentAlle.size());

        // also when/then
        Assertions.assertThrows(IllegalArgumentException.class, () -> filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena, -100));
    }

    @Test
    public void kanHenteUtFilterSomInneholderFiltervalg() {
        // Given
        String veilederId1 = "01010111111";
        String veilederId2 = "02020222222";

        PortefoljeFilter filterMedHovedmal = new PortefoljeFilter();
        filterMedHovedmal.setHovedmal(List.of(SKAFFEA.name()));

        PortefoljeFilter filterUtenHovedmal = new PortefoljeFilter();
        filterUtenHovedmal.setInnsatsgruppe(List.of(BATT.name()));

        PortefoljeFilter filterMedMangeHovedmal = new PortefoljeFilter();
        filterMedMangeHovedmal.setHovedmal(List.of(SKAFFEA.name(), BEHOLDEA.name(), OKEDELT.name()));

        mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter", filterMedHovedmal));
        mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter uten hovedmål", filterUtenHovedmal));
        mineLagredeFilterRepository.lagreFilter(veilederId1, new NyttFilterModel("filter med mange hovedmål", filterMedMangeHovedmal));
        mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter", filterMedHovedmal));
        mineLagredeFilterRepository.lagreFilter(veilederId2, new NyttFilterModel("filter med mange hovedmål", filterMedMangeHovedmal));


        // when
        List<FilterModel> result = filterRepository.hentMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena);
        List<PortefoljeFilter> portefoljeFilterMedHovedmal = result.stream().map(it -> it.getFilterValg()).toList();

        // then
        Assertions.assertEquals(4, portefoljeFilterMedHovedmal.size());
        Assertions.assertEquals(filterRepository.tellMineFilterSomInneholderEnBestemtFiltertype(hovedmalfilterArena), portefoljeFilterMedHovedmal.size());
        Assertions.assertEquals(filterMedHovedmal.getHovedmal(), portefoljeFilterMedHovedmal.getFirst().getHovedmal());
    }
}
