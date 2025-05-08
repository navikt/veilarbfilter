package no.nav.pto.veilarbfilter.rest;

import io.getunleash.DefaultUnleash;
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

import static no.nav.pto.veilarbfilter.config.FeatureToggle.ERSTATT_TRENGER_VURDERING_MED_TRENGER_OPPFOLGINGSVEDTAK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/api/minelagredefilter", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MineLagredeFilterController {
    private final MineLagredeFilterService mineLagredeFilterService;
    private final DefaultUnleash defaultUnleash;

    private static final String TRENGER_VURDERING_FILTERVERDI = "TRENGER_VURDERING";
    private static final String TRENGER_OPPFOLGINGSVEDTAK_FILTERVERDI = "TRENGER_OPPFOLGINGSVEDTAK";

    @PostMapping
    public ResponseEntity<FilterModel> lagreFilter(@RequestBody NyttFilterModel nyttFilterModel) {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();

        Optional<FilterModel> filterModelOptional = mineLagredeFilterService.lagreFilter(veilederId, nyttFilterModel);
        if (filterModelOptional.isPresent()) {
            return ResponseEntity.ok().body(filterModelOptional.get());
        }
        throw new IllegalStateException();
    }

    /* 2025-05-08, Sondre
     *
     * Vi er i prosessen med å byte ut "Trenger vurdering"-filteret i veilarbportefolje/veilarbportefoljeflatefs
     * med nytt filter "Trenger oppfølgingsvedtak § 14 a". I ein overgangsfase brukar vi feature-toggles for å styre
     * om vi skal vise nytt filter.
     *
     * Her tek vi hensyn til eventuelle lagra filter som inneheld "Trenger vurdering"-filteret og mappar dette om
     * til å vere "Trenger oppfølgingsvedtak § 14 a"-filter.
     *
     * På same måten, dersom vi har hatt endringa skrudd på ei stund men treng å reversere (skru av), så mappar vi
     * "Trenger oppfølgingsvedtak § 14 a" til "Trenger vurdering", dersom nokon mot formodning skulle kome til å
     * lagre eit filter med "Trenger oppfølgingsvedtak § 14 a"-filteret.
     *
     * Planen er å køyre ei databasespørjing som skriv alle "TRENGER_VURDERING" om til "TRENGER_OPPFOLGINGSVEDTAK"
     * når vi er fornøgd med endringa. Då kan feature-toggle samt denne funksjonen fjernast.
     */
    private Optional<List<String>> mapTrengerVurderingFerdigFilterTilTrengerOppfolgingsvedtak(List<String> ferdigFilterListe) {
        boolean skalErstatteTrengerVurderingFerdigFilter =
                defaultUnleash.isEnabled(ERSTATT_TRENGER_VURDERING_MED_TRENGER_OPPFOLGINGSVEDTAK)
                        && ferdigFilterListe.stream().anyMatch(TRENGER_VURDERING_FILTERVERDI::equals);
        boolean skalErstatteTrengerOppfolgingsvedtakFerdigFilter =
                !defaultUnleash.isEnabled(ERSTATT_TRENGER_VURDERING_MED_TRENGER_OPPFOLGINGSVEDTAK)
                        && ferdigFilterListe.stream().anyMatch(TRENGER_OPPFOLGINGSVEDTAK_FILTERVERDI::equals);

        if (skalErstatteTrengerVurderingFerdigFilter) {
            return Optional.of(
                    ferdigFilterListe.stream()
                            .map(ferdigFilter ->
                                    TRENGER_VURDERING_FILTERVERDI.equals(ferdigFilter)
                                            ? TRENGER_OPPFOLGINGSVEDTAK_FILTERVERDI
                                            : ferdigFilter
                            )
                            .toList()
            );
        }

        if (skalErstatteTrengerOppfolgingsvedtakFerdigFilter) {
            return Optional.of(
                    ferdigFilterListe.stream()
                            .map(ferdigFilter ->
                                    TRENGER_OPPFOLGINGSVEDTAK_FILTERVERDI.equals(ferdigFilter)
                                            ? TRENGER_VURDERING_FILTERVERDI
                                            : ferdigFilter
                            )
                            .toList()
            );
        }

        return Optional.empty();
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

        filterModels.forEach(filterModel ->
                mapTrengerVurderingFerdigFilterTilTrengerOppfolgingsvedtak(filterModel.getFilterValg().getFerdigfilterListe())
                        .ifPresent(it -> filterModel.getFilterValg().setFerdigfilterListe(it))
        );

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
