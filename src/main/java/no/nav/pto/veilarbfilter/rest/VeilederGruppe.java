package no.nav.pto.veilarbfilter.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.Pep;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.NavIdent;
import no.nav.pto.veilarbfilter.auth.AuthUtils;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.value.VeilederId;
import no.nav.pto.veilarbfilter.service.VeilederGrupperService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/enhet")
@RequiredArgsConstructor
public class VeilederGruppe {
    private final VeilederGrupperService veilederGrupperService;
    private final Pep veilarbPep;

    @PostMapping("/{enhetId}")
    public ResponseEntity<FilterModel> lagreFilter(@PathVariable(value = "enhetId") String enhetId, @RequestBody NyttFilterModel nyttFilterModel) {
        VeilederId innloggetVeilederIdent = AuthUtils.getInnloggetVeilederIdent();
        if (veilarbPep.harVeilederTilgangTilEnhet(NavIdent.of(innloggetVeilederIdent.toString()), EnhetId.of(enhetId))) {
            Optional<FilterModel> filterModelOptional = veilederGrupperService.lagreFilter(enhetId, nyttFilterModel);
            if (filterModelOptional.isPresent()) {
                return ResponseEntity.ok().body(filterModelOptional.get());
            }
            throw new IllegalStateException();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

    }

    @PutMapping("/{enhetId}")
    public ResponseEntity<FilterModel> oppdaterFilter(@PathVariable(value = "enhetId") String enhetId, @RequestBody FilterModel filterModel) {
        VeilederId innloggetVeilederIdent = AuthUtils.getInnloggetVeilederIdent();
        if (veilarbPep.harVeilederTilgangTilEnhet(NavIdent.of(innloggetVeilederIdent.toString()), EnhetId.of(enhetId))) {
            Optional<FilterModel> filterModelOptional = veilederGrupperService.oppdaterFilter(enhetId, filterModel);
            if (filterModelOptional.isPresent()) {
                return ResponseEntity.ok().body(filterModelOptional.get());
            }
            throw new IllegalStateException();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/{enhetId}")
    public ResponseEntity<List<FilterModel>> finnFilterForFilterBruker(@PathVariable(value = "enhetId") String enhetId) {
        VeilederId innloggetVeilederIdent = AuthUtils.getInnloggetVeilederIdent();
        if (veilarbPep.harVeilederTilgangTilEnhet(NavIdent.of(innloggetVeilederIdent.toString()), EnhetId.of(enhetId))) {
            List<FilterModel> filterModels = veilederGrupperService.finnFilterForFilterBruker(enhetId);

            return ResponseEntity.ok().body(filterModels);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @DeleteMapping("{enhetId}/filter/{filterId}")
    public ResponseEntity slettFilter(@PathVariable(value = "enhetId") String enhetId, @PathVariable(value = "filterId") Integer filterId) {
        VeilederId innloggetVeilederIdent = AuthUtils.getInnloggetVeilederIdent();
        if (veilarbPep.harVeilederTilgangTilEnhet(NavIdent.of(innloggetVeilederIdent.toString()), EnhetId.of(enhetId))) {
            Integer slettetFilterId = veilederGrupperService.slettFilter(filterId, enhetId);
            if (slettetFilterId == 0) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

}
