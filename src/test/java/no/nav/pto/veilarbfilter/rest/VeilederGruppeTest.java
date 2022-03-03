package no.nav.pto.veilarbfilter.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.MineLagredeFilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.service.VeilederGrupperService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@WebMvcTest(controllers = VeilederGruppe.class)
@ActiveProfiles({"Test"})
public class VeilederGruppeTest extends AbstractTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VeilederGrupperService veilederGrupperService;

    @Autowired
    private MockMvc mockMvc = MockMvcBuilders.standaloneSetup()
            .setControllerAdvice(RestResponseEntityExceptionHandler.class).build();

    @Test
    public void testInit() {
        Assertions.assertNotNull(mockMvc);
    }

    @Test
    public void LagringAvNyVeilederFilter() {
        val mineLagredeFilterResponse = getFilterGrupper("1");

        if (mineLagredeFilterResponse.getContent() == null) {
            fail();
        } else {
            lagreNyttFilterRespons("1", getRandomNyttFilter(List.of("1")));
            val mineLagredeFilterNyResponsEtterLagring = getFilterGrupper("1");

            if (mineLagredeFilterNyResponsEtterLagring.getContent() == null || mineLagredeFilterNyResponsEtterLagring.getContent().isEmpty()) {
                fail();
            } else {
                assertTrue(mineLagredeFilterResponse.getContent().size() < mineLagredeFilterNyResponsEtterLagring.getContent().size());
            }
        }
    }

    @Test
    public void CleanupVeilederGrupperFjernerUgyldigeVeildere() {
        val mineLagredeFilterResponse = getFilterGrupper("1");

        if (mineLagredeFilterResponse.getContent() == null) {
            fail();
        } else {
            val responsLagring =
                    lagreNyttFilterRespons("1", getRandomNyttFilter(List.of("1", "2", "23546576"))).getContent();
            if (responsLagring == null) {
                fail();
            } else {
                veilederGrupperService.slettVeiledereSomIkkeErAktivePaEnheten("1");
                try {
                    wait(1000);   // Wait for clean up
                } catch (Exception e) {
                }
                val filterePaEnhet = getFilterGrupper("1").getContent();

                if (filterePaEnhet == null || filterePaEnhet.isEmpty()) {
                    fail();
                } else {
                    assertTrue(filterePaEnhet.stream().filter(x -> Objects.equals(x.getFilterId(), responsLagring.getFilterId()))
                            .allMatch(x -> x.getFilterValg().getVeiledere().equals(List.of("1", "2"))));
                }
            }
        }
    }

    @Test
    public void OppdaterFilterFra3til2Veiledere() {
        val veildederGruppeFor = List.of("1", "2", "3");
        val veildederGruppeEtter = List.of("1", "2");
        val responsLagring = lagreNyttFilterRespons("1", getRandomNyttFilter(veildederGruppeFor)).getContent();

        if (responsLagring == null) {
            fail();
        } else {
            responsLagring.setFilterValg(getRandomPortefoljeFilter(veildederGruppeEtter));
            val responsOppdater = oppdaterVeilederFilter("1", responsLagring).getContent();

            val alleFilterePaEnhet = getFilterGrupper("1");

            if (responsOppdater == null || alleFilterePaEnhet.getContent() == null || alleFilterePaEnhet.getContent().isEmpty()) {
                fail();
            } else {
                val filterePaEnhet = alleFilterePaEnhet.getContent();
                assertTrue(filterePaEnhet.stream().filter(x -> x.getFilterId().equals(responsLagring.getFilterId())).allMatch(x -> x.getFilterValg().getVeiledere().equals(veildederGruppeEtter)));
            }
        }
    }

    @Test
    public void SletteVeiledergruppe() {
        val veildederGruppe = List.of("1", "2", "3");
        val responsLagring = lagreNyttFilterRespons("1", getRandomNyttFilter(veildederGruppe)).getContent();

        if (responsLagring == null) {
            fail();
        } else {
            val allefilterePaEnhetForSlett = getFilterGrupper("1").getContent();
            if (allefilterePaEnhetForSlett == null) {
                fail();
            } else {

                assertTrue(allefilterePaEnhetForSlett.stream().filter(x -> x.getFilterId().equals(responsLagring.getFilterId())).allMatch(x -> x.getFilterValg().getVeiledere().equals(veildederGruppe)));
                slettFilter("1", responsLagring.getFilterId());

                val allefilterePaEnhetEtterSlett = getFilterGrupper("1").getContent();
                if (allefilterePaEnhetEtterSlett == null) {
                    fail();
                } else {
                    Assertions.assertTrue(allefilterePaEnhetEtterSlett.stream().noneMatch(x -> x.getFilterId().equals(responsLagring.getFilterId())));
                }
            }
        }
    }

    /**
     * HJELPEFUNKSJONER 
     **/
    private ApiResponse<List<MineLagredeFilterModel>> getFilterGrupper(String enhetId) {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/veilarbfilter/api/enhet/" + enhetId).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andReturn();

            if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
                }), "");
            } else {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(),
                        null, mvcResult.getResolvedException().getMessage());
            }
        } catch (Exception e) {
            fail();
            return null;
        }
    }

    private ApiResponse<MineLagredeFilterModel> lagreNyttFilterRespons(String enhetId, NyttFilterModel valgteFilter) {
        try {

            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/veilarbfilter/api/enhet/" + enhetId).content(objectMapper.writeValueAsString(valgteFilter)).contentType(MediaType.APPLICATION_JSON)).andReturn();

            if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), objectMapper.readValue(mvcResult.getResponse().getContentAsString(), MineLagredeFilterModel.class), "");
            } else {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, mvcResult.getResponse().getContentAsString());
            }
        } catch (Exception e) {
            fail();
            return null;
        }
    }

    private ApiResponse<MineLagredeFilterModel> oppdaterVeilederFilter(String enhetId, FilterModel filterModel) {
        try {

            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/veilarbfilter/api/enhet/" + enhetId).content(objectMapper.writeValueAsString(filterModel)).contentType(MediaType.APPLICATION_JSON)).andReturn();

            if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), objectMapper.readValue(mvcResult.getResponse().getContentAsString(), MineLagredeFilterModel.class), "");
            } else {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, mvcResult.getResponse().getContentAsString());
            }
        } catch (Exception e) {
            fail();
            return null;
        }
    }

    private Integer slettFilter(String enhetId, Integer filterId) {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/veilarbfilter/api/enhet/" + enhetId + "/filter/" + filterId).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andReturn();
            return mvcResult.getResponse().getStatus();
        } catch (Exception e) {
            fail();
            return null;
        }
    }

    private NyttFilterModel getRandomNyttFilter(List<String> veiledersList) {
        Random random = new Random();

        return new NyttFilterModel("Filter navn " + random.nextInt(1000, 100000), getRandomPortefoljeFilter(veiledersList));
    }

    public PortefoljeFilter getRandomPortefoljeFilter(List<String> veiledersList) {
        return new PortefoljeFilter(null, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), "", emptyList(), "", emptyList(), emptyList(), emptyList(), "", veiledersList, "", emptyList(), "", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), "", emptyList());
    }
}
