package no.nav.pto.veilarbfilter.rest;

import io.getunleash.DefaultUnleash;
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient;
import no.nav.pto.veilarbfilter.config.FeatureToggle;
import no.nav.pto.veilarbfilter.domene.OverblikkVisningRequest;
import no.nav.pto.veilarbfilter.domene.OverblikkVisningResponse;
import no.nav.pto.veilarbfilter.service.OverblikkVisningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OverblikkVisningRoutingTest {
    private final OverblikkVisningService service = mock(OverblikkVisningService.class);
    private final VeilarbveilederClient client = mock(VeilarbveilederClient.class);
    private final DefaultUnleash unleash = mock(DefaultUnleash.class);
    private final OverblikkVisningController controller =
            new OverblikkVisningController(service, client, unleash);

    @BeforeEach
    void setUp() {
        when(unleash.isEnabled(FeatureToggle.OVERBLIKK_VISNING_I_VEILARBVEILEDER)).thenReturn(true);
    }

    @Test
    void routesReadWriteAndDeleteToVeilarbveilederWhenToggleIsEnabled() {
        var response = new OverblikkVisningResponse(List.of("CV"));
        when(client.hent()).thenReturn(ResponseEntity.ok(response));
        when(client.lagre(any(OverblikkVisningRequest.class))).thenReturn(ResponseEntity.noContent().build());
        when(client.slett()).thenReturn(ResponseEntity.noContent().build());

        assertThat(controller.hentOverblikkvisningForInnloggetVeileder().getBody())
                .isEqualTo(response);
        assertThat(controller.lagreOverblikkvisningForInnloggetVeileder(
                new OverblikkVisningRequest(List.of("CV"))
        ).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(controller.slettOverblikkvisningForInnloggetVeileder().getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        verify(client).hent();
        verify(client).lagre(any(OverblikkVisningRequest.class));
        verify(client).slett();
        verifyNoInteractions(service);
    }
}
