package no.nav.pto.veilarbfilter.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.service.LagredeFilterFeilmeldinger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(controllers = MineLagredeFilterController.class)
@ActiveProfiles({"test"})
public class MineLagredeFilterTest extends AbstractTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc = MockMvcBuilders.standaloneSetup()
            .setControllerAdvice(RestResponseEntityExceptionHandler.class).build();

    @BeforeEach
    public void beforeEach() {

    }

    @Test
    public void testInit() {
        Assertions.assertNotNull(mockMvc);
    }

    /**
     * TESTER RELATERT TIL GYLDIGHET FOR LAGRING AV NYTT FILTER
     **/
    @Test

    public void LagringAvNyttFilterErGyldig() {
        val mineLagredeFilterResponse = getMineLagredeFilter();

        if (mineLagredeFilterResponse.getContent() == null) {
            fail();
        }

        lagreNyttFilterRespons(getRandomNyttFilter());
        val mineLagredeFilterNyResponsEtterLagring = getMineLagredeFilter();

        if (mineLagredeFilterNyResponsEtterLagring.getContent() == null) {
            fail();
        }

        assertTrue(mineLagredeFilterResponse.getContent().size() < mineLagredeFilterNyResponsEtterLagring.getContent().size());
    }

    /**
     * TESTER RELATERT TIL UGYLDIGHET FOR LAGRING AV NYTT FILTER
     **/
    @Test
    public void FilternavnEksistererAlleredeForNyttFilter() {
        val randomNyttFilter = getRandomNyttFilter();

        lagreNyttFilterRespons(randomNyttFilter);
        val mineLagredeFilterResponse = getMineLagredeFilter();

        val nyttFilterModelEksisterendeNavn =
                new NyttFilterModel(
                        randomNyttFilter.getFilterNavn(),
                        getRandomPortefoljeFilter()
                );

        val lagreNyttFilterMedEksisterendeNavn = lagreNyttFilterRespons(nyttFilterModelEksisterendeNavn);
        val mineLagredeFilterResponsEtterFeilLagring = getMineLagredeFilter();

        if (mineLagredeFilterResponse.getContent() == null || mineLagredeFilterResponsEtterFeilLagring.getContent() == null) {
            fail();
        }

        assertEquals(lagreNyttFilterMedEksisterendeNavn.getError(), LagredeFilterFeilmeldinger.NAVN_EKSISTERER.message);
        assertTrue(mineLagredeFilterResponse.getContent().size() == mineLagredeFilterResponsEtterFeilLagring.getContent().size());
    }

    @Test
    public void FiltervalgEksistereAlleredeForNyttFilter() {
        val randomNyttFilter = getRandomNyttFilter();

        lagreNyttFilterRespons(randomNyttFilter);
        val mineLagredeFilterResponse = getMineLagredeFilter();

        val nyttFilterModelEksisterendeFilter =
                new NyttFilterModel(
                        "NyFilter",
                        randomNyttFilter.getFilterValg()
                );

        val lagreNyttFilterMedEksisterendeFilterKombinasjon =
                lagreNyttFilterRespons(nyttFilterModelEksisterendeFilter);
        val mineLagredeFilterResponseEtterFeilLagring = getMineLagredeFilter();

        if (mineLagredeFilterResponse.getContent() == null || mineLagredeFilterResponseEtterFeilLagring.getContent() == null) {
            fail();
        }

        assertEquals(
                lagreNyttFilterMedEksisterendeFilterKombinasjon.getError(),
                LagredeFilterFeilmeldinger.FILTERVALG_EKSISTERER.message
        );
        assertTrue(lagreNyttFilterMedEksisterendeFilterKombinasjon.getStatus() == 400);
        assertTrue(mineLagredeFilterResponse.getContent().size() == mineLagredeFilterResponseEtterFeilLagring.getContent().size());
    }

    @Test
    public void TomtNavnErUgyldigForNyttFilter() {
        val nyttFilterModel =
                new NyttFilterModel(
                        "",
                        getRandomPortefoljeFilter()
                );

        val lagreNyttFilterMedTomtFilterNavn = lagreNyttFilterRespons(nyttFilterModel);

        assertTrue(lagreNyttFilterMedTomtFilterNavn.getStatus() == 400);
        assertEquals(lagreNyttFilterMedTomtFilterNavn.getError(), LagredeFilterFeilmeldinger.NAVN_TOMT.message);
    }


    @Test
    public void TomtFiltervalgErUgyldigForNyttFilter() {
        val nyttFilterModel =
                new NyttFilterModel(
                        "Nytt filter",
                        new PortefoljeFilter()
                );

        val lagreNyttFilterMedTomFilterKombinasjon = lagreNyttFilterRespons(nyttFilterModel);
        assertTrue(lagreNyttFilterMedTomFilterKombinasjon.getStatus() == 400);
        assertEquals(
                lagreNyttFilterMedTomFilterKombinasjon.getError(),
                LagredeFilterFeilmeldinger.FILTERVALG_TOMT.message
        );
    }

    /**
     * TESTER RELATERT TIL GYLDIGHET FOR OPPDATERING AV EKSISTERENDE FILTER
     **/
    @Test
    public void OppdateringAvFilterErGyldig() throws JsonProcessingException {
        val nyttFilter = lagreNyttFilterVerdi(getRandomNyttFilter());

        if (nyttFilter == null) {
            fail();
        }

        nyttFilter.setFilterNavn("New name");
        nyttFilter.setFilterValg(getRandomPortefoljeFilter());

        oppdaterMineLagredeFilter(nyttFilter);

        val mineLagredeFilterResponse = getMineLagredeFilter();

        List<MineLagredeFilterModel> mineLagredeFilterList = mineLagredeFilterResponse.getContent();

        if (mineLagredeFilterList == null) {
            fail();
        }

        val oppdatertFilter =
                mineLagredeFilterList.stream().filter(x -> x.getFilterId() == nyttFilter.getFilterId()).findFirst();

        assertTrue(oppdatertFilter.isPresent());
        assertTrue(oppdatertFilter.get().getFilterNavn().equals(nyttFilter.getFilterNavn()));
        assertTrue(objectMapper.writeValueAsString(oppdatertFilter.get().getFilterValg()).equals(objectMapper.writeValueAsString(nyttFilter.getFilterValg())));
    }

    @Test
    public void SlettingAvFilterErGyldig() {
        val lagretMineLagredeFilterResponse = lagreNyttFilterVerdi(getRandomNyttFilter());

        if (lagretMineLagredeFilterResponse == null) {
            fail();
        }

        val responseCode = deleteMineLagredeFilter(lagretMineLagredeFilterResponse.getFilterId());
        assertTrue(responseCode == 200 || responseCode == 204);

        val mineLagredeFilterResponse = getMineLagredeFilter();

        if (mineLagredeFilterResponse.getContent() == null) {
            fail();
        }

        val mineLagredeFilter = mineLagredeFilterResponse.getContent();

        assertTrue(mineLagredeFilter.stream().noneMatch(x -> x.getFilterId() == lagretMineLagredeFilterResponse.getFilterId()));
    }

    /**
     * TESTER RELATERT TIL UGYLDIGHET FOR OPPDATERING AV EKSISTERENDE FILTER 
     **/
    @Test
    public void ForLangtNnavnErUgyldig() {
        val endepunktRespons =
                lagreNyttFilterRespons(
                        new NyttFilterModel(
                                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                                getRandomPortefoljeFilter()
                        )
                );

        assertEquals(endepunktRespons.getError(), LagredeFilterFeilmeldinger.NAVN_FOR_LANGT.message);
        assertTrue(endepunktRespons.getStatus() != 200);
    }

    @Test
    public void TomtNavnErUgyldigForOppdateringAvFilter() {
        val nyttFilter = lagreNyttFilterRespons(getRandomNyttFilter()).getContent();

        if (nyttFilter == null) {
            fail();
        }

        nyttFilter.setFilterNavn("");
        val endepunktRespons = oppdaterMineLagredeFilter(nyttFilter);

        assertEquals(endepunktRespons.getError(), LagredeFilterFeilmeldinger.NAVN_TOMT.message);
        assertTrue(endepunktRespons.getStatus() == 400);
    }

    @Test
    public void TomtFiltervalgErUgyldigForOppdatertFilter() {
        val nyttFilter = lagreNyttFilterRespons(getRandomNyttFilter()).getContent();

        if (nyttFilter == null) {
            fail();
        }

        nyttFilter.setFilterValg(new PortefoljeFilter());

        val endepunktRespons = oppdaterMineLagredeFilter(nyttFilter);

        assertEquals(endepunktRespons.getError(), LagredeFilterFeilmeldinger.FILTERVALG_TOMT.message);
        assertTrue(endepunktRespons.getStatus() == 400);
    }


    /**
     * TESTER RELATERT TIL GYLDIGHET FOR BÅDE LAGRING OG OPPDATERING
     **/
    //@Test
    // @TODO: fix this test
    public void SpesialbokstaverFungerer() {
        val spesialbokstaverFilterNavn = "æøåöäáâò";
        val endepunktRespons =
                lagreNyttFilterRespons(
                        new NyttFilterModel(
                                spesialbokstaverFilterNavn,
                                getRandomPortefoljeFilter()
                        )
                );
        assertTrue(endepunktRespons.getStatus() == 200);
        assertTrue(endepunktRespons.getContent().getFilterNavn().equals(spesialbokstaverFilterNavn));
    }

    /**
     * TESTER RELATERT TIL SORTING
     **/
    @Test
    public void SortingFungerer() {
        Random random = new Random();
        lagreNyttFilterVerdi(getRandomNyttFilter());
        lagreNyttFilterVerdi(getRandomNyttFilter());
        lagreNyttFilterVerdi(getRandomNyttFilter());

        ApiResponse<List<MineLagredeFilterModel>> mineLagredeFilter = getMineLagredeFilter();
        if (mineLagredeFilter.getContent() == null) {
            fail();
        }
        assertTrue(mineLagredeFilter.getContent().size() >= 3);

        val sortOrder = new ArrayList<SortOrder>();
        mineLagredeFilter.getContent().stream().forEach(x -> {
            sortOrder.add(new SortOrder(x.getFilterId(), Integer.valueOf(random.nextInt(20))));
        });

        val oppdaterSortingMineLagredeFilter = oppdaterMineLagredeFilter(sortOrder);
        assertTrue(oppdaterSortingMineLagredeFilter.getContent() != null);
        assertTrue(oppdaterSortingMineLagredeFilter.getContent().size() >= 3);

        val mineLagredeFilterMedSortOrder = getMineLagredeFilter();

        Map<Integer, Integer> sortOrderMap = sortOrder.stream().collect(Collectors.toMap(SortOrder::getFilterId, SortOrder::getSortOrder));
        assertTrue(mineLagredeFilterMedSortOrder.getContent().stream().allMatch(x -> x.getSortOrder() == sortOrderMap.get(x.getFilterId())));
    }


    /**
     * HJELPEFUNKSJONER
     **/
    private ApiResponse<List<MineLagredeFilterModel>> getMineLagredeFilter() {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/minelagredefilter").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andReturn();

            if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
                }), "");
            } else {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(),
                        null, mvcResult.getResolvedException().getMessage());
            }
        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private ApiResponse<MineLagredeFilterModel> lagreNyttFilterRespons(NyttFilterModel valgteFilter) {
        try {

            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/minelagredefilter").content(objectMapper.writeValueAsString(valgteFilter)).contentType(MediaType.APPLICATION_JSON)).andReturn();

            if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), objectMapper.readValue(mvcResult.getResponse().getContentAsString(), MineLagredeFilterModel.class), "");
            } else {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, mvcResult.getResponse().getContentAsString());
            }
        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private ApiResponse<MineLagredeFilterModel> oppdaterMineLagredeFilter(FilterModel filterModel) {
        try {

            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/minelagredefilter").content(objectMapper.writeValueAsString(filterModel)).contentType(MediaType.APPLICATION_JSON)).andReturn();

            if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), objectMapper.readValue(mvcResult.getResponse().getContentAsString(), MineLagredeFilterModel.class), "");
            } else {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, mvcResult.getResponse().getContentAsString());
            }
        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private ApiResponse<List<MineLagredeFilterModel>> oppdaterMineLagredeFilter(List<SortOrder> sortOrder) {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/minelagredefilter/lagresortering").content(objectMapper.writeValueAsString(sortOrder)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andReturn();
            if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
                }), "");
            } else {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, mvcResult.getResponse().getContentAsString());
            }
        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private Integer deleteMineLagredeFilter(Integer filterId) {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/minelagredefilter/" + filterId).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andReturn();
            return mvcResult.getResponse().getStatus();
        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private MineLagredeFilterModel lagreNyttFilterVerdi(NyttFilterModel filterModel) {
        ApiResponse<MineLagredeFilterModel> mineLagredeFilterModelApiResponse = lagreNyttFilterRespons(filterModel);
        assertEquals(mineLagredeFilterModelApiResponse.getStatus(), 200);
        return mineLagredeFilterModelApiResponse.getContent();
    }

    private NyttFilterModel getRandomNyttFilter() {
        Random random = new Random();

        return new NyttFilterModel("Filter navn " + random.nextInt(100000), getRandomPortefoljeFilter());
    }

    public PortefoljeFilter getRandomPortefoljeFilter() {
        Random random = new Random();
        Aktiviteter aktiviteter = new Aktiviteter();
        aktiviteter.setBEHANDLING("J");
        aktiviteter.setIJOBB("N");
        val alderVelg = List.of("19-og-under", "20-24", "25-29", "30-39", "40-49", "50-59", "60-66", "67-70");
        val kjonnVelg = List.of("K", "M");
        val ferdigfilterListe = List.of("UFORDELTE_BRUKERE", "NYE_BRUKERE_FOR_VEILEDER", "TRENGER_OPPFOLGINGSVEDTAK", "INAKTIVE_BRUKERE", "VENTER_PA_SVAR_FRA_NAV", "VENTER_PA_SVAR_FRA_BRUKER", "UTLOPTE_AKTIVITETER");
        return new PortefoljeFilter(aktiviteter,
                List.of(alderVelg.get(random.nextInt(7)),
                        alderVelg.get(random.nextInt(7)), alderVelg.get(random.nextInt(7))),
                List.of(ferdigfilterListe.get(random.nextInt(6)), ferdigfilterListe.get(random.nextInt(6)), ferdigfilterListe.get(random.nextInt(6))),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                kjonnVelg.get(random.nextInt(1)),
                emptyList(),
                String.valueOf(rndChar()),
                emptyList(),
                emptyList(),
                emptyList(),
                "",
                emptyList(),
                "",
                emptyList(),
                "",
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                "",
                emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),
                emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),
                emptyList(), emptyList(), emptyList(), emptyList(), emptyList());
    }

    private static char rndChar() {
        int rnd = (int) (Math.random() * 52); // or use Random or whatever
        char base = (rnd < 26) ? 'A' : 'a';
        return (char) (base + rnd % 26);

    }
}
