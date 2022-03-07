package no.nav.pto.veilarbfilter.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.service.VeilederGrupperService;
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

    @PostMapping("/{enhetId}")
    public ResponseEntity<FilterModel> lagreFilter(@PathVariable(value = "enhetId") String enhetId, @RequestBody NyttFilterModel nyttFilterModel) {
        Optional<FilterModel> filterModelOptional = veilederGrupperService.lagreFilter(enhetId, nyttFilterModel);
        if (filterModelOptional.isPresent()) {
            return ResponseEntity.ok().body(filterModelOptional.get());
        }
        throw new IllegalStateException();
    }

    @PutMapping("/{enhetId}")
    public ResponseEntity<FilterModel> oppdaterFilter(@PathVariable(value = "enhetId") String enhetId, @RequestBody FilterModel filterModel) {
        Optional<FilterModel> filterModelOptional = veilederGrupperService.oppdaterFilter(enhetId, filterModel);
        if (filterModelOptional.isPresent()) {
            return ResponseEntity.ok().body(filterModelOptional.get());
        }
        throw new IllegalStateException();
    }

    @GetMapping("/{enhetId}")
    public ResponseEntity<List<FilterModel>> finnFilterForFilterBruker(@PathVariable(value = "enhetId") String enhetId) {
        List<FilterModel> filterModels = veilederGrupperService.finnFilterForFilterBruker(enhetId);

        return ResponseEntity.ok().body(filterModels);
    }

    @DeleteMapping("{enhetId}/filter/{filterId}")
    public ResponseEntity slettFilter(@PathVariable(value = "enhetId") String enhetId, @PathVariable(value = "filterId") Integer filterId) {
        Integer slettetFilterId = veilederGrupperService.slettFilter(filterId, enhetId);
        if (slettetFilterId == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

}
