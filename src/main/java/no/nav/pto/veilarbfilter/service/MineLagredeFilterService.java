package no.nav.pto.veilarbfilter.service;

import io.getunleash.DefaultUnleash;
import io.getunleash.UnleashContext;
import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbfilter.auth.AuthUtils;
import no.nav.pto.veilarbfilter.config.FeatureToggle;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MineLagredeFilterService implements FilterService {
    private final MineLagredeFilterRepository mineLagredeFilterRepository;
    private final DefaultUnleash defaultUnleash;

    @Override
    public Optional<FilterModel> lagreFilter(String veilederId, NyttFilterModel nyttFilter) throws IllegalArgumentException {
        try {
            return mineLagredeFilterRepository.lagreFilter(veilederId, nyttFilter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String veilederId, FilterModel filter) throws IllegalArgumentException {
        try {
            return mineLagredeFilterRepository.oppdaterFilter(veilederId, filter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> hentFilter(Integer filterId) {
        Optional<FilterModel> filterModel = mineLagredeFilterRepository.hentFilter(filterId);
        return filtrerUtSamanlikn14aOgArenaFilterTilFrontend(filterModel);
    }

    @Override
    public List<FilterModel> finnFilterForFilterBruker(String veilederId) {
        List<FilterModel> filterModelListe = mineLagredeFilterRepository.finnFilterForFilterBruker(veilederId);
        return filterModelListe.stream().map(this::filtrerUtSamanlikn14aOgArenaFilterTilFrontend).filter(Objects::nonNull).toList();
    }

    @Override
    public Integer slettFilter(Integer filterId, String veilederId) {
        return mineLagredeFilterRepository.slettFilter(filterId, veilederId);
    }

    @Override
    public List<FilterModel> lagreSortering(String veilederId, List<SortOrder> sortOrder) {
        return mineLagredeFilterRepository.lagreSortering(veilederId, sortOrder);
    }

    public List<MineLagredeFilterModel> hentAllLagredeFilter() {
        return mineLagredeFilterRepository.hentAllLagredeFilter();
    }

    private Optional<FilterModel> filtrerUtSamanlikn14aOgArenaFilterTilFrontend(Optional<FilterModel> filterModelOptional) {
        return filterModelOptional.map(this::filtrerUtSamanlikn14aOgArenaFilterTilFrontend);
    }

    /* I ein mellomfase der vi har skrudd på feature-toggle for å skjule samanliknings-filteret
     * mellom gjeldande § 14 a-vedtak og Arena filtrerer vi vekk filter som inneheld det.
     * Vi har på førehand sjekka at omfanget er lite, og at filtera berre handlar om avvik mellom gjeldande vedtak og Arena. */
    private FilterModel filtrerUtSamanlikn14aOgArenaFilterTilFrontend(FilterModel filter) {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();
        UnleashContext unleashContext = UnleashContext.builder()
                .userId(veilederId)
                .build();

        boolean skjulFilterSammenligneGjeldende14aOgArena = defaultUnleash.isEnabled(FeatureToggle.SKJUL_FILTER_SAMMENLIGNE_GJELDENDE_14A_OG_ARENA, unleashContext);

        if (!skjulFilterSammenligneGjeldende14aOgArena) {
            // Ikkje gjer noko dersom feature-toggle er av
            return filter;
        } else {
            List<String> avvik14aFilter = filter.getFilterValg().getAvvik14aVedtak();

            // Ikkje gjer noko om filteret ikkje inneheld Samanlikningsfilteret
            if (avvik14aFilter.isEmpty()) {
                return filter;
            }

            // Filtrer bort filteret om det inneheld Samanlikningsfilteret
            return null;
        }
    }
}
