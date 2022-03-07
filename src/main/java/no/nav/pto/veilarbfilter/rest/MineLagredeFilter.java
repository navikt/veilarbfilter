package no.nav.pto.veilarbfilter.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.auth.AuthUtils;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.SortOrder;
import no.nav.pto.veilarbfilter.service.MineLagredeFilterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/veilarbfilter/api/minelagredefilter", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MineLagredeFilter {
    private final MineLagredeFilterService mineLagredeFilterService;

    @PostMapping
    public ResponseEntity<FilterModel> lagreFilter(@RequestBody NyttFilterModel nyttFilterModel) {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        Optional<FilterModel> filterModelOptional = mineLagredeFilterService.lagreFilter(veilederId, nyttFilterModel);
        if (filterModelOptional.isPresent()) {
            return ResponseEntity.ok().body(filterModelOptional.get());
        }
        throw new IllegalStateException();
    }

    @PutMapping
    public ResponseEntity<FilterModel> oppdaterFilter(@RequestBody FilterModel filterModel) {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        Optional<FilterModel> filterModelOptional = mineLagredeFilterService.oppdaterFilter(veilederId, filterModel);
        if (filterModelOptional.isPresent()) {
            return ResponseEntity.ok().body(filterModelOptional.get());
        }
        throw new IllegalStateException();
    }

    @GetMapping
    public ResponseEntity<List<FilterModel>> finnFilterForFilterBruker() {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        List<FilterModel> filterModels = mineLagredeFilterService.finnFilterForFilterBruker(veilederId);

        return ResponseEntity.ok().body(filterModels);
    }

    @DeleteMapping("/{filterId}")
    public ResponseEntity slettFilter(@PathVariable("filterId") Integer filterId) {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        Integer slettetFilterId = mineLagredeFilterService.slettFilter(filterId, veilederId);
        if (slettetFilterId == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("lagresortering")
    public ResponseEntity lagresortering(@RequestBody List<SortOrder> sortOrder) {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        List<FilterModel> lagreSortering = mineLagredeFilterService.lagreSortering(veilederId, sortOrder);

        return ResponseEntity.ok().body(lagreSortering);
    }
}
