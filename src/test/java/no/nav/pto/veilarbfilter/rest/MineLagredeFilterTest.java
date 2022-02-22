package no.nav.pto.veilarbfilter.rest;

import lombok.val;
import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.auth.AuthUtils;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.domene.value.VeilederId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Random;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@WebMvcTest(MineLagredeFilter.class)
@ActiveProfiles({"Test"})
public class MineLagredeFilterTest extends AbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void setUp() {
        MockedStatic<AuthUtils> authUtilsMockedStatic = Mockito.mockStatic(AuthUtils.class);
        authUtilsMockedStatic.when(() -> AuthUtils.getInnloggetVeilederIdent())
                .thenReturn(VeilederId.of("1"));
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
     * HJELPEFUNKSJONER
     **/
    private ApiResponse<List<MineLagredeFilterModel>> getMineLagredeFilter() {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/veilarbfilter/api/minelagredefilter").accept(MediaType.APPLICATION_JSON)).andReturn();

            if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                ObjectMapper objectMapper = new ObjectMapper();
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<MineLagredeFilterModel>>() {
                }), "");
            } else {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, mvcResult.getResponse().getContentAsString());
            }
        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private ApiResponse<MineLagredeFilterModel> lagreNyttFilterRespons(NyttFilterModel valgteFilter) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/veilarbfilter/api/minelagredefilter").content(objectMapper.writeValueAsString(valgteFilter)).accept(MediaType.APPLICATION_JSON)).andReturn();

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
            ObjectMapper objectMapper = new ObjectMapper();

            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("veilarbfilter/api/minelagredefilter").content(objectMapper.writeValueAsString(filterModel)).accept(MediaType.APPLICATION_JSON)).andReturn();

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

    private ApiResponse<MineLagredeFilterModel> oppdaterMineLagredeFilter(List<SortOrder> sortOrder) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("veilarbfilter/api/minelagredefilter/lagresortering").content(objectMapper.writeValueAsString(sortOrder)).accept(MediaType.APPLICATION_JSON)).andReturn();
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

    private Integer deleteMineLagredeFilter(Integer filterId) {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("veilarbfilter/api/minelagredefilter/" + filterId).accept(MediaType.APPLICATION_JSON)).andReturn();
            return mvcResult.getResponse().getStatus();
        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private MineLagredeFilterModel lagreNyttFilterVerdi(NyttFilterModel filterModel) {
        ApiResponse<MineLagredeFilterModel> mineLagredeFilterModelApiResponse = lagreNyttFilterRespons(filterModel);
        Assertions.assertEquals(mineLagredeFilterModelApiResponse.getStatus(), 200);
        return mineLagredeFilterModelApiResponse.getContent();
    }

    private NyttFilterModel getRandomNyttFilter() {
        Random random = new Random();
        val alderVelg = List.of("19-og-under", "20-24", "25-29", "30-39", "40-49", "50-59", "60-66", "67-70");
        val kjonnVelg = List.of("K", "M");
        PortefoljeFilter portefoljeFilter = new PortefoljeFilter(null, List.of(alderVelg.get(random.nextInt(0, 7))), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), kjonnVelg.get(random.nextInt(0, 1)), emptyList(), "", emptyList(), emptyList(), emptyList(), "", emptyList(), "", emptyList(), "", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), "", emptyList());

        return new NyttFilterModel("Filter navn " + random.nextInt(100, 1000), portefoljeFilter);
    }
}