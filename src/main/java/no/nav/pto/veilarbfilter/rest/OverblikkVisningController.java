package no.nav.pto.veilarbfilter.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.auth.AuthUtils;
import no.nav.pto.veilarbfilter.domene.NyOverblikkVisningModel;
import no.nav.pto.veilarbfilter.domene.OverblikkVisningModel;
import no.nav.pto.veilarbfilter.service.OverblikkVisningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(value = "/api/overblikkvisning", produces = APPLICATION_JSON_VALUE)

public class OverblikkVisningController {

    private final OverblikkVisningService overblikkVisningService;


    @PostMapping
    public ResponseEntity lagreOgOppdater(@RequestBody NyOverblikkVisningModel nyOverblikkVisningModel) throws Exception {

        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        overblikkVisningService.lagreOgOppdater(veilederId, nyOverblikkVisningModel.getOverblikkVisning());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Optional <List<String>>> hentVisning() {

        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        Optional<OverblikkVisningModel> visningsListe = overblikkVisningService.hentVisning(veilederId);
        if (visningsListe.isPresent()){
            String overblikkVisning = visningsListe.get().getOverblikkVisning();
            List<String> overblikkVisningListe = new ArrayList<String>(Arrays.asList(overblikkVisning.replace("[", "").replace("]", "").split(",")));
            return ResponseEntity.ok().body(Optional.of(overblikkVisningListe));
        }

        return ResponseEntity.ok().body(Optional.empty());
    }

    @DeleteMapping
    public ResponseEntity slettVisning() {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();
        try {
            overblikkVisningService.slettVisning(veilederId);
            return ResponseEntity.noContent().build();
        }
        catch (Exception e) {
            log.error("Klarte ikke slette visningen", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
