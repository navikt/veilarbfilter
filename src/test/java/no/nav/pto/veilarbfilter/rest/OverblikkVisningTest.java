package no.nav.pto.veilarbfilter.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.NyOverblikkVisningModel;
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
import java.util.ArrayList;
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

    @Test
    public void testInit() {
        Assertions.assertNotNull(mockMvc);
    }



    @Test
    public void testLagreOgOppdaterVisning() throws Exception {
        //lagre en ny visning med 2 elementer
        val listeAvVisninger = new ArrayList<String>();
        listeAvVisninger.add("CV");
        listeAvVisninger.add("Personalia");
        val lagreVisning = lagreEllerOppdaterVisninger(new NyOverblikkVisningModel(listeAvVisninger));
        Assertions.assertEquals(HttpStatus.OK.value(), lagreVisning.getStatus());

        //hente visningen med 2
        val hentVisning = hentVisningTest();
        Assertions.assertEquals(HttpStatus.OK.value(), hentVisning.getStatus());
        Assertions.assertEquals(hentVisning.getContent().size(), 2);

        //oppdatere visningen til å ha 3 elementer
        val listeAvOppdaterteVisninger = new ArrayList<String>(listeAvVisninger);
        listeAvOppdaterteVisninger.add("Registrering");
        val oppdatereVisning = lagreEllerOppdaterVisninger(new NyOverblikkVisningModel(listeAvOppdaterteVisninger));
        Assertions.assertEquals(HttpStatus.OK.value(), oppdatereVisning.getStatus());

        //hent oppdatert visning
        val hentOppdatertVisning = hentVisningTest();
        Assertions.assertEquals(HttpStatus.OK.value(), hentOppdatertVisning.getStatus());
        Assertions.assertEquals(3, hentOppdatertVisning.getContent().size());

    }

    @Test
    public void testSlettVisning() throws Exception {
        //lagre en ny visning
        val listeAvVisninger = new ArrayList<String>();
        listeAvVisninger.add("CV");
        listeAvVisninger.add("Personalia");
        val lagreVisning = lagreEllerOppdaterVisninger(new NyOverblikkVisningModel(listeAvVisninger));
        Assertions.assertEquals(HttpStatus.OK.value(), lagreVisning.getStatus());

        //hente visningen
        val hentVisning = hentVisningTest();
        Assertions.assertEquals(HttpStatus.OK.value(), hentVisning.getStatus());
        Assertions.assertEquals(hentVisning.getContent().size(), 2);

        //slett visningen
        val slettVisning = slettVisningTest();
        val hentVisningEtterSletting = hentVisningTest();
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), slettVisning.getStatus());
        Assertions.assertEquals(HttpStatus.OK.value(), hentVisningEtterSletting.getStatus());
        Assertions.assertEquals(hentVisningEtterSletting.getContent(), null);

    }
    //Hjelpefunskjoner

    private ApiResponse<List<String>> hentVisningTest() {
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

    private ApiResponse<OverblikkVisningModel> lagreEllerOppdaterVisninger (NyOverblikkVisningModel nyOverblikkVisningModel) throws Exception {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/overblikkvisning").content(objectMapper.writeValueAsString(nyOverblikkVisningModel)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andReturn();

            if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, "");
            } else {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, mvcResult.getResponse().getContentAsString());
            }

        } catch (Exception e){
            Assertions.fail();
            return null;
        }
    }

    private ApiResponse<OverblikkVisningModel> slettVisningTest() throws Exception {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/overblikkvisning").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andReturn();

            if (mvcResult.getResponse().getStatus() == HttpStatus.NO_CONTENT.value()) {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, "");
            } else {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, mvcResult.getResponse().getContentAsString());
            }

        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }


}
