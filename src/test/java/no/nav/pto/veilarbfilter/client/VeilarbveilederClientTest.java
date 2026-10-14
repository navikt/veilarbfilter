package no.nav.pto.veilarbfilter.client;

import com.sun.net.httpserver.HttpServer;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.pto.veilarbfilter.domene.OverblikkVisningRequest;
import no.nav.pto.veilarbfilter.domene.OverblikkVisningResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VeilarbveilederClientTest {
    private static final String ENDPOINT = "/veilarbveileder/api/overblikkvisning";

    private HttpServer server;
    private final List<String> receivedRequests = new ArrayList<>();
    private AuthContextHolder authContextHolder;
    private AzureAdOnBehalfOfTokenClient tokenClient;
    private VeilarbveilederClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(ENDPOINT, exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            receivedRequests.add(
                    exchange.getRequestMethod() + " "
                            + exchange.getRequestURI().getPath() + " "
                            + exchange.getRequestHeaders().getFirst("Authorization") + " "
                            + body
            );
            if ("GET".equals(exchange.getRequestMethod())) {
                byte[] responseBody = "{\"overblikkVisning\":[\"CV\"]}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBody.length);
                exchange.getResponseBody().write(responseBody);
            } else {
                exchange.sendResponseHeaders(204, -1);
            }
            exchange.close();
        });
        server.start();

        authContextHolder = mock(AuthContextHolder.class);
        tokenClient = mock(AzureAdOnBehalfOfTokenClient.class);
        when(authContextHolder.requireIdTokenString()).thenReturn("id-token");
        when(tokenClient.exchangeOnBehalfOfToken("api://veilarbveileder/.default", "id-token"))
                .thenReturn("access-token");

        client = new VeilarbveilederClient(
                RestClient.builder()
                        .baseUrl("http://127.0.0.1:" + server.getAddress().getPort())
                        .build(),
                "api://veilarbveileder/.default",
                tokenClient,
                authContextHolder
        );
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void skal_sende_alle_kall_med_token_pa_vegne_av_brukeren() {
        var getResponse = client.hent();
        var postResponse = client.lagre(new OverblikkVisningRequest(List.of("CV")));
        var deleteResponse = client.slett();

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().overblikkVisning()).containsExactly("CV");
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(receivedRequests).containsExactly(
                "GET " + ENDPOINT + " Bearer access-token ",
                "POST " + ENDPOINT + " Bearer access-token {\"overblikkVisning\":[\"CV\"]}",
                "DELETE " + ENDPOINT + " Bearer access-token "
        );
        verify(tokenClient, org.mockito.Mockito.times(3))
                .exchangeOnBehalfOfToken("api://veilarbveileder/.default", "id-token");
    }
}
