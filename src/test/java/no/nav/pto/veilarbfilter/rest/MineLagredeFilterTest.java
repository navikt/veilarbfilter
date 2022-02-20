package no.nav.pto.veilarbfilter.rest;

import lombok.val;
import no.nav.pto.veilarbfilter.AbstractTest;
import no.nav.pto.veilarbfilter.domene.*;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Random;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class MineLagredeFilterTest extends AbstractTest {
    @Autowired
    private MineLagredeFilter mineLagredeFilter;

    @LocalServerPort
    private String port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(mineLagredeFilter);
        Assertions.assertNotNull(restTemplate);
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

        assertTrue(mineLagredeFilterResponse.getContent().length < mineLagredeFilterNyResponsEtterLagring.getContent().length);
    }

    /**
     * HJELPEFUNKSJONER
     **/
    private ApiResponse<MineLagredeFilterModel[]> getMineLagredeFilter() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(String.format("http://localhost:%s/veilarbfilter/api/minelagredefilter/", port), String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                ObjectMapper objectMapper = new ObjectMapper();
                return new ApiResponse<>(response.getStatusCodeValue(), objectMapper.readValue(response.getBody(), MineLagredeFilterModel[].class), "");
            } else {
                return new ApiResponse<>(response.getStatusCodeValue(), null, response.getBody());
            }
        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private ApiResponse<MineLagredeFilterModel> lagreNyttFilterRespons(NyttFilterModel valgteFilter) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(String.format("http://localhost:%s/veilarbfilter/api/minelagredefilter/", port), valgteFilter, String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                ObjectMapper objectMapper = new ObjectMapper();
                return new ApiResponse<>(response.getStatusCodeValue(), objectMapper.readValue(response.getBody(), MineLagredeFilterModel.class), "");
            } else {
                return new ApiResponse<>(response.getStatusCodeValue(), null, response.getBody());
            }
        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private ApiResponse<MineLagredeFilterModel> oppdaterMineLagredeFilter(FilterModel filterModel) {
        try {
            HttpEntity<FilterModel> requestEntity = new HttpEntity<>(filterModel, new HttpHeaders());
            ResponseEntity<String> response = restTemplate.exchange(String.format("http://localhost:%s/veilarbfilter/api/minelagredefilter/", port), HttpMethod.PUT, requestEntity, String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                ObjectMapper objectMapper = new ObjectMapper();
                return new ApiResponse<>(response.getStatusCodeValue(), objectMapper.readValue(response.getBody(), MineLagredeFilterModel.class), "");
            } else {
                return new ApiResponse<>(response.getStatusCodeValue(), null, response.getBody());
            }
        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private ApiResponse<MineLagredeFilterModel> oppdaterMineLagredeFilter(List<SortOrder> sortOrder) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            ResponseEntity<String> response = restTemplate.postForEntity(String.format("http://localhost:%s/veilarbfilter/api/minelagredefilter/lagresortering", port), objectMapper.writeValueAsString(sortOrder), String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                return new ApiResponse<>(response.getStatusCodeValue(), objectMapper.readValue(response.getBody(), MineLagredeFilterModel.class), "");
            } else {
                return new ApiResponse<>(response.getStatusCodeValue(), null, response.getBody());
            }
        } catch (Exception e) {
            Assertions.fail();
            return null;
        }
    }

    private Integer deleteMineLagredeFilter(Integer filterId) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(String.format("http://localhost:%s/veilarbfilter/api/minelagredefilter/%s", port, filterId), HttpMethod.DELETE, null, String.class);
            return response.getStatusCode().value();
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