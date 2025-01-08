package no.nav.pto.veilarbfilter.repository;


import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.domene.value.ArenaHovedmal;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@WebMvcTest
@ActiveProfiles({"test"})
public class MineLagredeFilterRepositoryTest extends AbstractTest {
    @Autowired
    private MineLagredeFilterRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE filter CASCADE");
    }

    @Test
    public void kanLagreOgHenteUtFilterForVeileder() {
        String veilederId = "11223312345";
        PortefoljeFilter portefoljeFilter = new PortefoljeFilter();
        List<String> alleHovedmalfilter = List.of(ArenaHovedmal.OKEDELT.name(), ArenaHovedmal.BEHOLDEA.name(), ArenaHovedmal.SKAFFEA.name());
        portefoljeFilter.setHovedmal(alleHovedmalfilter);
        NyttFilterModel nyttFilter = new NyttFilterModel("Navn på nytt filter", portefoljeFilter);

        repository.lagreFilter(veilederId, nyttFilter);

        List<FilterModel> result = repository.finnFilterForFilterBruker(veilederId);
        List<String> hovedmalfilter = result.getFirst().getFilterValg().getHovedmal();
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(hovedmalfilter.size() == 3);
        Assertions.assertTrue(hovedmalfilter.containsAll(alleHovedmalfilter));
    }

    @Test
    public void kanTelleAlleFilterMedEnFiltertype() {
        Integer antallFilterForInserts = repository.tellMineFilterSomInneholderEnBestemtFiltertype();
        Assertions.assertEquals(0, antallFilterForInserts);

        String veilederId = "11223312345";
        List<String> alleHovedmalfilter = List.of(ArenaHovedmal.OKEDELT.name(), ArenaHovedmal.BEHOLDEA.name(), ArenaHovedmal.SKAFFEA.name());
        PortefoljeFilter filterMedHovedmal = new PortefoljeFilter();
        filterMedHovedmal.setHovedmal(alleHovedmalfilter);

        repository.lagreFilter(veilederId, new NyttFilterModel("filter med hovedmal", filterMedHovedmal));

        Integer antallFilterEtterInsertAvHovedmalfilter = repository.tellMineFilterSomInneholderEnBestemtFiltertype();
        Assertions.assertEquals(1, antallFilterEtterInsertAvHovedmalfilter);

        PortefoljeFilter filterUtenHovedmal = new PortefoljeFilter();
        filterUtenHovedmal.setVeiledere(List.of("Z123456", "Y123456", "X123456"));

        repository.lagreFilter(veilederId, new NyttFilterModel("filter uten hovedmal", filterUtenHovedmal));

        Integer antallFilterEtterInsertAvIkkehovedmalfilter = repository.tellMineFilterSomInneholderEnBestemtFiltertype();
        Assertions.assertEquals(1, antallFilterEtterInsertAvIkkehovedmalfilter);
    }
}
