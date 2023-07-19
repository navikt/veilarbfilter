package no.nav.pto.veilarbfilter.rest;

import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbfilter.auth.AuthUtils;
import no.nav.pto.veilarbfilter.domene.ChipsModel;
import no.nav.pto.veilarbfilter.domene.NyttChipsModel;
import no.nav.pto.veilarbfilter.service.ChipsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/overblikkvisning", produces = APPLICATION_JSON_VALUE)

public class MineChipsController {

    private final ChipsService chipsService;


@PostMapping
    public ResponseEntity lagreOgOppdater(@RequestBody NyttChipsModel nyttChipsModel) throws Exception {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        chipsService.lagreOgOppdater(veilederId, nyttChipsModel.getDetaljerVisning());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Optional<ChipsModel>> hentVisning() {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        Optional<ChipsModel> visningsListe = chipsService.hentVisning(veilederId);
        return ResponseEntity.ok().body(visningsListe);

    }

    @DeleteMapping
    public ResponseEntity slettVisning() {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        chipsService.slettVisning(veilederId);
        return ResponseEntity.ok().build();
    }
}
