package no.nav.pto.veilarbfilter.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.getunleash.DefaultUnleash;
import io.getunleash.UnleashContext;
import no.nav.pto.veilarbfilter.auth.AuthUtils;
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient;
import no.nav.pto.veilarbfilter.config.FeatureToggle;
import no.nav.pto.veilarbfilter.domene.OverblikkVisning;
import no.nav.pto.veilarbfilter.domene.OverblikkVisningAlternativer;
import no.nav.pto.veilarbfilter.domene.OverblikkVisningRequest;
import no.nav.pto.veilarbfilter.domene.OverblikkVisningResponse;
import no.nav.pto.veilarbfilter.domene.value.VeilederId;
import no.nav.pto.veilarbfilter.service.OverblikkVisningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static no.nav.pto.veilarbfilter.util.SecureLogUtils.secureLog;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(value = "/api/overblikkvisning", produces = APPLICATION_JSON_VALUE)
public class OverblikkVisningController {

    private final OverblikkVisningService overblikkVisningService;
    private final VeilarbveilederClient veilarbveilederClient;
    private final DefaultUnleash unleash;

    @PostMapping
    public ResponseEntity<Object> lagreOverblikkvisningForInnloggetVeileder(@RequestBody OverblikkVisningRequest overblikkVisningRequest) {
        VeilederId veilederId = AuthUtils.getInnloggetVeilederIdent();
        if (erFunksjonAktivertFor(veilederId)) {
            return veilarbveilederClient.lagre(overblikkVisningRequest);
        }

        try {
            validerRequest(overblikkVisningRequest);
            overblikkVisningService.lagreOverblikkVisning(veilederId, overblikkVisningRequest.overblikkVisning());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            secureLog.error(String.format("Klarte ikke å lagre overblikk visning for veilederId: %s. Grunn: ulovlige verdier i request.", veilederId), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            secureLog.error(String.format("Klarte ikke å lagre overblikk visning for veilederId: %s.", veilederId), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<OverblikkVisningResponse> hentOverblikkvisningForInnloggetVeileder() {
        VeilederId veilederId = AuthUtils.getInnloggetVeilederIdent();
        if (erFunksjonAktivertFor(veilederId)) {
            return veilarbveilederClient.hent();
        }

        try {
            Optional<OverblikkVisning> visningsListe = overblikkVisningService.hentOverblikkVisning(veilederId);
            return visningsListe
                    .map(overblikkVisning -> ResponseEntity.ok().body(new OverblikkVisningResponse(overblikkVisning.visning())))
                    .orElseGet(() -> ResponseEntity.ok().body(new OverblikkVisningResponse(Collections.emptyList())));
        } catch (Exception e) {
            secureLog.error(String.format("Klarte ikke å hente overblikk visning for veilederId: %s", veilederId), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Object> slettOverblikkvisningForInnloggetVeileder() {
        VeilederId veilederId = AuthUtils.getInnloggetVeilederIdent();
        if (erFunksjonAktivertFor(veilederId)) {
            return veilarbveilederClient.slett();
        }

        try {
            overblikkVisningService.slettOverblikkVisning(veilederId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            secureLog.error(String.format("Klarte ikke å slette overblikk visning for veilederId: %s", veilederId), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private void validerRequest(OverblikkVisningRequest overblikkVisningRequest) {

        boolean alleValgteAlternativerErLovlige = overblikkVisningRequest.overblikkVisning().stream()
                .map(String::toUpperCase)
                .allMatch(valgtAlternativ ->
                        Arrays.stream(OverblikkVisningAlternativer.values())
                                .map(Enum::name)
                                .anyMatch(lovligAlternativ -> lovligAlternativ.equals(valgtAlternativ))
                );

        if (!alleValgteAlternativerErLovlige) {
            throw new IllegalArgumentException();
        }
    }

    private boolean erFunksjonAktivertFor(VeilederId veilederId) {
        UnleashContext kontekst = UnleashContext.builder()
                .userId(veilederId.getValue())
                .build();
        return unleash.isEnabled(FeatureToggle.OVERBLIKK_VISNING_I_VEILARBVEILEDER, kontekst);
    }
}
