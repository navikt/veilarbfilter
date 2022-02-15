package no.nav.pto.veilarbfilter.service;

import lombok.val;
import no.nav.common.types.identer.EnhetId;
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Random;

public class VeilederGrupperServiceTest extends AbstractTest {
    @Autowired
    private VeilederGrupperService veilederGrupperService;

    private final VeilarbveilederClient veilarbVeilederClient;

    private final Random randomGenerator = new Random();

    public VeilederGrupperServiceTest() {
        veilarbVeilederClient = Mockito.mock(VeilarbveilederClient.class);
        Mockito.when(veilarbVeilederClient.hentVeilederePaaEnhet(EnhetId.of("1"))).thenReturn(List.of("1", "2", "3"));
    }

    @BeforeEach
    public void wipeAllGroups() {
        veilederGrupperService.finnFilterForFilterBruker("1").stream().forEach(filter -> {
            veilederGrupperService.slettFilter(filter.getFilterId(), "1");
        });
    }

    @Test
    public void testSlettInaktiveVeiledere() {
        Integer veildederGruppeId1 = veilederGrupperService.lagreFilter("1", getRandomFilter(List.of("1", "2", "3"))).map(x -> x.getFilterId()).orElse(-1);
        Integer veildederGruppeId2 = veilederGrupperService.lagreFilter("1", getRandomFilter(List.of("1", "2", "6"))).map(x -> x.getFilterId()).orElse(-1);
        Integer veildederGruppeId3 = veilederGrupperService.lagreFilter("1", getRandomFilter(List.of("10", "12", "13"))).map(x -> x.getFilterId()).orElse(-1);

        veilederGrupperService.slettVeiledereSomIkkeErAktivePaEnheten("1");

        val filterListSize = veilederGrupperService.finnFilterForFilterBruker("1").size();
        List<FilterModel> filterList = veilederGrupperService.finnFilterForFilterBruker("1");

        Assertions.assertEquals(filterListSize, 2);
        Assertions.assertTrue(finnVeilederDB(veildederGruppeId1, filterList, List.of("1", "2", "3")));
        Assertions.assertTrue(finnVeilederDB(veildederGruppeId2, filterList, List.of("1", "2")));
        Assertions.assertTrue(filterList.stream().noneMatch(gruppe -> gruppe.getFilterId().equals(veildederGruppeId3)));


        Assertions.assertTrue(filterList.stream().anyMatch(gruppe -> gruppe.getFilterId().equals(veildederGruppeId1) && gruppe.getFilterCleanup() == 0));
        Assertions.assertTrue(filterList.stream().anyMatch(gruppe -> gruppe.getFilterId().equals(veildederGruppeId2) && gruppe.getFilterCleanup() == 1));
    }

    private Boolean finnVeilederDB(Integer gruppeID, List<FilterModel> filterList, List veileders) {
        return filterList.stream().filter(gruppe -> gruppe.getFilterId().equals(gruppeID)).anyMatch(x -> x.getFilterValg().getVeiledere().containsAll(veileders));
    }

    private NyttFilterModel getRandomFilter(List<String> veiledereList) {
        Integer filterId = randomGenerator.nextInt(1, 1000);
        PortefoljeFilter portefoljeFilter = new PortefoljeFilter();
        portefoljeFilter.setVeiledere(veiledereList);
        return new NyttFilterModel(
                "Filter " + filterId,
                portefoljeFilter
        );
    }
}