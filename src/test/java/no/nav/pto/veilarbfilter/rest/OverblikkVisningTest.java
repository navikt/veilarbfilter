package no.nav.pto.veilarbfilter.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.OverblikkVisningModel;
import no.nav.pto.veilarbfilter.service.OverblikkVisningService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = OverblikkVisningController.class)
@ActiveProfiles({"test"})
public class OverblikkVisningTest extends AbstractTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc = MockMvcBuilders.standaloneSetup()
            .setControllerAdvice(RestResponseEntityExceptionHandler.class).build();

    @BeforeEach
    public void beforeEach() {
    }

    @MockBean
    private OverblikkVisningService overblikkVisningService;

    @Test
    public void testInit() {
        Assertions.assertNotNull(mockMvc);
    }

    @Test
    public void testSlettVisning() throws Exception {
        String veilederId = "VeilederId";
        doNothing().when(overblikkVisningService).slettVisning(eq(veilederId));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/api/overblikkvisning"))
                .andReturn();
        Assertions.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());

        val visningResponse = hentVisningTest();
        if (visningResponse == null) {
            fail();
        }

        val responseCode = slettVisningTest(veilederId);
        assertFalse(responseCode.equals(200));
    }

    @Test
    public void testLagreOgOppdaterVisning() throws Exception {
        OverblikkVisningModel overblikkVisningModel = new OverblikkVisningModel();

        val visningRespons = hentVisningTest();
        lagreEllerOppdaterVisninger("someVeilederID", List.of("CV", "personalia"), LocalDateTime.now());

        val nyVisningResponse = hentVisningTest();

        if (nyVisningResponse.getContent() == null) {
            fail();
        }

        assertTrue(visningRespons.getContent() == nyVisningResponse.getContent());


    }

    //Hjelpefunskjoner

    private String slettVisningTest(String veilederId) {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/overblikkvisning/" + veilederId).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andReturn();
            return mvcResult.getResponse().getContentAsString();
        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private ApiResponse<Optional<OverblikkVisningModel>> hentVisningTest() {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/overblikkvisning").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andReturn();

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

    private ApiResponse<OverblikkVisningModel> lagreEllerOppdaterVisninger (String veilederId, List<String> overblikkVisning, LocalDateTime opprettet) throws Exception {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/overblikkvisning").content(objectMapper.writeValueAsString(veilederId)).contentType(MediaType.APPLICATION_JSON)).andReturn();

            if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), objectMapper.readValue(mvcResult.getResponse().getContentAsString(), OverblikkVisningModel.class), "");
            } else {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, mvcResult.getResponse().getContentAsString());
            }

        } catch (Exception e){
            Assertions.fail();
            return null;
        }
    }


}
