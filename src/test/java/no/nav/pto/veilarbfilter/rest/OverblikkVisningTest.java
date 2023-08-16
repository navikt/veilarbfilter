package no.nav.pto.veilarbfilter.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.database.Table;
import no.nav.pto.veilarbfilter.domene.OverblikkVisning;
import no.nav.pto.veilarbfilter.domene.OverblikkVisningResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = OverblikkVisningController.class)
@ActiveProfiles({"test"})
public class OverblikkVisningTest extends AbstractTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc = MockMvcBuilders.standaloneSetup()
            .setControllerAdvice(RestResponseEntityExceptionHandler.class).build();

    @Autowired
    private JdbcTemplate db;

    @BeforeEach
    public void resetDB() {
        db.update("TRUNCATE " + Table.OverblikkVisning.TABLE_NAME);
    }

    @Test
    public void testInit() {
        Assertions.assertNotNull(mockMvc);
    }

    @Test
    public void skal_returnere_overblikkvisning_naar_overblikkvisning_eksisterer() {
        var overblikkVisningJson = "{\"overblikkVisning\":[\"CV\",\"Personalia\"]}";
        lagreOverblikkVisning(overblikkVisningJson);

        var faktiskTilstand = hentOverblikkVisning();

        var forventetTilstand = new ArrayList<String>();
        forventetTilstand.add("CV");
        forventetTilstand.add("Personalia");
        assertThat(faktiskTilstand.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(faktiskTilstand.getContent().overblikkVisning()).containsExactlyInAnyOrderElementsOf(forventetTilstand);
    }

    @Test
    public void skal_returnere_default_respons_naar_ingen_overblikkvisning_eksisterer() {
        var hentVisningRespons = hentOverblikkVisning();

        assertThat(hentVisningRespons.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(hentVisningRespons.getContent().overblikkVisning()).isEqualTo(Collections.emptyList());
    }

    @Test
    public void skal_lagre_ny_overblikkvisning() {
        var overblikkVisningJson = "{\"overblikkVisning\":[\"CV\",\"Personalia\"]}";

        var lagreVisningRespons = lagreOverblikkVisning(overblikkVisningJson);
        var lagretTilstand = hentOverblikkVisning();

        var forventetTilstand = new ArrayList<String>();
        forventetTilstand.add("CV");
        forventetTilstand.add("Personalia");
        assertThat(lagreVisningRespons.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(lagretTilstand.getContent().overblikkVisning()).containsExactlyInAnyOrderElementsOf(forventetTilstand);
    }

    @Test
    public void skal_overskrive_overblikkvisning_dersom_overblikkvisning_eksisterer() {
        var overblikkVisningRequest_1 = "{\"overblikkVisning\":[\"CV\",\"Personalia\"]}";
        lagreOverblikkVisning(overblikkVisningRequest_1);

        var overblikkVisningRequest_2 = "{\"overblikkVisning\":[\"CV\"]}";
        lagreOverblikkVisning(overblikkVisningRequest_2);
        var faktiskTilstand = hentOverblikkVisning();

        var forventetTilstand = new ArrayList<String>();
        forventetTilstand.add("CV");
        assertThat(faktiskTilstand.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(faktiskTilstand.getContent().overblikkVisning()).containsExactlyInAnyOrderElementsOf(forventetTilstand);
    }

    @Test
    public void skal_returnere_tom_respons_nar_sletting_er_utfort_dersom_veileder_har_lagret_overblikkvisning() {
        var overblikkVisningJson = "{\"overblikkVisning\":[\"CV\",\"Personalia\"]}";
        lagreOverblikkVisning(overblikkVisningJson);

        var slettOverblikkvisningRespons = slettVisningTest();
        var faktiskTilstand = hentOverblikkVisning();

        List<String> forventetTilstand = Collections.emptyList();
        assertThat(slettOverblikkvisningRespons.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(faktiskTilstand.getContent().overblikkVisning()).containsExactlyInAnyOrderElementsOf(forventetTilstand);
    }

    @Test
    public void skal_returnere_tom_respons_nar_sletting_er_utfort_dersom_veileder_ikke_har_lagret_overblikkvisning() {
        var slettOverblikkvisningRespons = slettVisningTest();

        assertThat(slettOverblikkvisningRespons.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void skal_returnere_feilmelding_dersom_input_data_er_ulovlig() {
        var overblikkVisningJson = "{\"overblikkVisning\":[\"FØØ\",\"BÅR\"]}";

        var lagreVisningRespons = lagreOverblikkVisning(overblikkVisningJson);

        assertThat(lagreVisningRespons.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private ApiResponse<OverblikkVisningResponse> hentOverblikkVisning() {
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

    private ApiResponse<Object> lagreOverblikkVisning(String json) {
        try {
            MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/overblikkvisning").content(json).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andReturn();

            if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, "");
            } else {
                return new ApiResponse<>(mvcResult.getResponse().getStatus(), null, mvcResult.getResponse().getContentAsString());
            }

        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private ApiResponse<Object> slettVisningTest() {
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
