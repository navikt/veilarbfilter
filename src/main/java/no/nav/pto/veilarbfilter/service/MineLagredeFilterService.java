package no.nav.pto.veilarbfilter.service;

import io.getunleash.DefaultUnleash;
import io.getunleash.UnleashContext;
import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbfilter.auth.AuthUtils;
import no.nav.pto.veilarbfilter.config.FeatureToggle;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
            PortefoljeFilter filtereMedKopiertAapArenaFilter = leggTilTpFraYtelseINyttTpArenaFilter(nyttFilter.getFilterValg());
            nyttFilter.setFilterValg(filtereMedKopiertAapArenaFilter);
            return mineLagredeFilterRepository.lagreFilter(veilederId, nyttFilter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String veilederId, FilterModel filter) throws IllegalArgumentException {
        try {
            PortefoljeFilter filtereMedKopiertAapArenaFilter = leggTilTpFraYtelseINyttTpArenaFilter(filter.getFilterValg());
            filter.setFilterValg(filtereMedKopiertAapArenaFilter);
            return mineLagredeFilterRepository.oppdaterFilter(veilederId, filter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> hentFilter(Integer filterId) {
        Optional<FilterModel> filterModel = mineLagredeFilterRepository.hentFilter(filterId);
        return fjernDuplikatAvTpArenaFilterTilFrontend(filterModel);
    }

    @Override
    public List<FilterModel> finnFilterForFilterBruker(String veilederId) {
        List<FilterModel> filterModelListe = mineLagredeFilterRepository.finnFilterForFilterBruker(veilederId);
        return filterModelListe.stream()
                .map(this::fjernDuplikatAvTpArenaFilterTilFrontend)
                .toList();
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


    private PortefoljeFilter leggTilTpFraYtelseINyttTpArenaFilter(PortefoljeFilter filter) {
        String tpYtelse = filter.getYtelse();
        boolean tiltakspenger = Objects.equals(tpYtelse, "TILTAKSPENGER");

        if (tiltakspenger) {
            filter.setYtelseTiltakspengerArena(List.of("HAR_TILTAKSPENGER"));
        }

        return filter;
    }

    private Optional<FilterModel> fjernDuplikatAvTpArenaFilterTilFrontend(Optional<FilterModel> filterModelOptional) {
        return filterModelOptional.map(this::fjernDuplikatAvTpArenaFilterTilFrontend);
    }

    // Når toggle er på skal kun det nye filteret returneres. Når den er av skal kun det gamle returneres.
    private FilterModel fjernDuplikatAvTpArenaFilterTilFrontend(FilterModel filter) {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();
        UnleashContext unleashContext = UnleashContext.builder()
                .userId(veilederId)
                .build();

        boolean brukNyttTpFilterErSkruddPå = defaultUnleash.isEnabled(FeatureToggle.BRUK_NYTT_ARENA_TILTAKSPENGER_FILTER, unleashContext);

        if (brukNyttTpFilterErSkruddPå) {
            String gammeltYtelseFilter = filter.getFilterValg().getYtelse();
            if (gammeltYtelseFilter != null && gammeltYtelseFilter.equals("TILTAKSPENGER")) {
                filter.getFilterValg().setYtelse(null);
            }
        } else {
            filter.getFilterValg().setYtelseTiltakspengerArena(Collections.emptyList());
        }
        return filter;
    }

}
