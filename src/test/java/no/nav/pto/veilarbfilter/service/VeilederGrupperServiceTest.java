package no.nav.pto.veilarbfilter.service;

import lombok.val;
import no.nav.common.json.JsonUtils;
import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@WebMvcTest
@ActiveProfiles({"test"})
class VeilederGrupperServiceTest extends AbstractTest {
    @Autowired
    private VeilederGrupperService veilederGrupperService;

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

    @Test
    public void singleVeilederInGroup() {
        val veildederGruppeId =
                veilederGrupperService.lagreFilter("1", getRandomFilter(List.of("1"))).map(x -> x.getFilterId()).orElse(-1);
        val filterList = veilederGrupperService.finnFilterForFilterBruker("1");

        Assertions.assertTrue(finnVeilederDB(veildederGruppeId, filterList, List.of("1")));
    }

    @Test
    public void retriveVeiledergruppe() {
        Optional<FilterModel> veildederGruppe = veilederGrupperService.lagreFilter("1", getRandomFilter(List.of("1")));

        Assertions.assertTrue(veildederGruppe.isPresent());
        if (veildederGruppe.isPresent()) {
            val filterFromService = veilederGrupperService.hentFilter(veildederGruppe.get().getFilterId());
            if (filterFromService != null) {
                Assertions.assertEquals(filterFromService.get().getFilterId(), veildederGruppe.get().getFilterId());
                Assertions.assertEquals(filterFromService.get().getFilterNavn(), veildederGruppe.get().getFilterNavn());
                Assertions.assertEquals(JsonUtils.toJson(filterFromService.get().getFilterValg()), JsonUtils.toJson(veildederGruppe.get().getFilterValg()));
            } else {
                Assertions.fail("Filter was not in DB");
            }
        }
    }

    @Test
    public void slettVeiledergruppe() {
        val veildederGruppe = veilederGrupperService.lagreFilter("1", getRandomFilter(List.of("1", "2")));
        Assertions.assertTrue(veildederGruppe.isPresent());
        if (veildederGruppe.isPresent()) {
            veilederGrupperService.slettFilter(veildederGruppe.get().getFilterId(), "1");
            val filterFromService = veilederGrupperService.hentFilter(veildederGruppe.get().getFilterId());
            Assertions.assertFalse(filterFromService.isPresent());
        }
    }

    @Test
    public void inactiveVeilederRemovedFromActiveGroup() {
        val veildederGruppeId =
                veilederGrupperService.lagreFilter("1", getRandomFilter(List.of("1", "5"))).map(x -> x.getFilterId()).orElse(-1);
        val filterList = veilederGrupperService.finnFilterForFilterBruker("1");

        Assertions.assertTrue(finnVeilederDB(veildederGruppeId, filterList, List.of("1")));
    }

    @Test
    public void updateFilter() {
        val veildederGruppe1Optional = veilederGrupperService.lagreFilter("1", getRandomFilter(List.of("1")));
        val veildederGruppe2Optional = veilederGrupperService.lagreFilter("1", getRandomFilter(List.of("1", "2", "3")));

        Assertions.assertTrue(veildederGruppe1Optional.isPresent());
        if (veildederGruppe1Optional.isPresent()) {
            FilterModel veiledederGruppe1 = veildederGruppe1Optional.get();
            PortefoljeFilter filterValg = veiledederGruppe1.getFilterValg();
            filterValg.setVeiledere(List.of("1", "2"));
            veiledederGruppe1.setFilterValg(filterValg);
            veilederGrupperService.oppdaterFilter("1", veiledederGruppe1);
            val filterList = veilederGrupperService.finnFilterForFilterBruker("1");
            Assertions.assertTrue(finnVeilederDB(veiledederGruppe1.getFilterId(), filterList, List.of("1", "2")));
        }

        Assertions.assertTrue(veildederGruppe2Optional.isPresent());
        if (veildederGruppe2Optional != null) {
            FilterModel veilederGruppe2 = veildederGruppe2Optional.get();
            PortefoljeFilter filterValg = veilederGruppe2.getFilterValg();
            filterValg.setVeiledere(List.of("1"));
            veilederGruppe2.setFilterValg(filterValg);
            veilederGrupperService.oppdaterFilter("1", veilederGruppe2);
            val filterList = veilederGrupperService.finnFilterForFilterBruker("1");
            Assertions.assertTrue(finnVeilederDB(veilederGruppe2.getFilterId(), filterList, List.of("1")));
        }
    }

    @Test
    public void testSlettTomtVeiledereGruppeEtterCleanup() {
        veilederGrupperService.lagreFilter("1", getRandomFilter(List.of("10", "12", "13")));
        veilederGrupperService.slettVeiledereSomIkkeErAktivePaEnheten("1");

        val filterList = veilederGrupperService.finnFilterForFilterBruker("1");

        Assertions.assertTrue(filterList.isEmpty());
    }

    private Boolean finnVeilederDB(Integer gruppeID, List<FilterModel> filterList, List veileders) {
        return filterList.stream().
                filter(gruppe -> gruppe.getFilterId().equals(gruppeID)).
                anyMatch(x -> x.getFilterValg().getVeiledere().containsAll(veileders));
    }

    private NyttFilterModel getRandomFilter(List<String> veiledereList) {
        Random randomGenerator = new Random();
        Integer filterId = randomGenerator.nextInt(10000);
        PortefoljeFilter portefoljeFilter = new PortefoljeFilter();
        portefoljeFilter.setVeiledere(veiledereList);
        return new NyttFilterModel(
                "Filter " + filterId,
                portefoljeFilter
        );
    }
}