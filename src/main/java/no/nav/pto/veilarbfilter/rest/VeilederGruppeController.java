package no.nav.pto.veilarbfilter.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.Pep;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.poao_tilgang.client.Decision;
import no.nav.poao_tilgang.client.NavAnsattTilgangTilNavEnhetPolicyInput;
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.pto.veilarbfilter.auth.AuthUtils;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.service.VeilederGrupperService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/enhet")
@RequiredArgsConstructor
public class VeilederGruppeController {
    private final VeilederGrupperService veilederGrupperService;
    private final Pep veilarbPep;
    private final PoaoTilgangClient poaoTilgangClient;

    private final AuthContextHolder authContextHolder;

    @PostMapping("/{enhetId}")
    public ResponseEntity<FilterModel> lagreFilter(@PathVariable(value = "enhetId") String enhetId, @RequestBody NyttFilterModel nyttFilterModel) {
        UUID innloggetVeilederUUID = AuthUtils.getInnloggetVeilederUUID(authContextHolder);

		if (harVeilederTilgangTilEnhet(innloggetVeilederUUID, enhetId)) {
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
        UUID innloggetVeilederUUID = AuthUtils.getInnloggetVeilederUUID(authContextHolder);

		if (harVeilederTilgangTilEnhet(innloggetVeilederUUID, enhetId)) {
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
        UUID innloggetVeilederUUID = AuthUtils.getInnloggetVeilederUUID(authContextHolder);

		if (harVeilederTilgangTilEnhet(innloggetVeilederUUID, enhetId)) {
			List<FilterModel> filterModels = veilederGrupperService.finnFilterForFilterBruker(enhetId);

			return ResponseEntity.ok().body(filterModels);
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @DeleteMapping("{enhetId}/filter/{filterId}")
    public ResponseEntity slettFilter(@PathVariable(value = "enhetId") String enhetId, @PathVariable(value = "filterId") Integer filterId) {
        UUID innloggetVeilederUUID = AuthUtils.getInnloggetVeilederUUID(authContextHolder);

		if (harVeilederTilgangTilEnhet(innloggetVeilederUUID, enhetId)) {
			Integer slettetFilterId = veilederGrupperService.slettFilter(filterId, enhetId);
			if (slettetFilterId == 0) {
				return ResponseEntity.notFound().build();
			}

			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    public boolean harVeilederTilgangTilEnhet(UUID innloggetVeilederUUID, String enhetId) {
        Decision desicion = poaoTilgangClient.evaluatePolicy(new NavAnsattTilgangTilNavEnhetPolicyInput(innloggetVeilederUUID, enhetId
        )).getOrThrow();
        return desicion.isPermit();
    }

}
