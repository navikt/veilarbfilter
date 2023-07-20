package no.nav.pto.veilarbfilter.rest;

import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbfilter.auth.AuthUtils;
import no.nav.pto.veilarbfilter.domene.NyOverblikkVisningModel;
import no.nav.pto.veilarbfilter.domene.OverblikkVisningModel;
import no.nav.pto.veilarbfilter.service.OverblikkVisningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
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
    public ResponseEntity<Optional<OverblikkVisningModel>> hentVisning() {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        Optional<OverblikkVisningModel> visningsListe = overblikkVisningService.hentVisning(veilederId);
        return ResponseEntity.ok().body(visningsListe);

    }

    @DeleteMapping
    public ResponseEntity slettVisning() {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        overblikkVisningService.slettVisning(veilederId);
        return ResponseEntity.ok().build();
    }
}
