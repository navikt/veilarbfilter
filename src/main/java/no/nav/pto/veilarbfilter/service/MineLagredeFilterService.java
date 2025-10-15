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
            PortefoljeFilter filtereMedKopiertAapArenaFilter = leggTilAapFraYtelseINyttAapArenaFilter(nyttFilter.getFilterValg());
            nyttFilter.setFilterValg(filtereMedKopiertAapArenaFilter);
            return mineLagredeFilterRepository.lagreFilter(veilederId, nyttFilter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String veilederId, FilterModel filter) throws IllegalArgumentException {
        try {
            PortefoljeFilter filtereMedKopiertAapArenaFilter = leggTilAapFraYtelseINyttAapArenaFilter(filter.getFilterValg());
            filter.setFilterValg(filtereMedKopiertAapArenaFilter);
            return mineLagredeFilterRepository.oppdaterFilter(veilederId, filter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> hentFilter(Integer filterId) {
        Optional<FilterModel> filterModel = mineLagredeFilterRepository.hentFilter(filterId);
        return fjernDuplikatAvAapArenaFilterTilFrontend(filterModel);
    }

    @Override
    public List<FilterModel> finnFilterForFilterBruker(String veilederId) {
        List<FilterModel> filterModelListe = mineLagredeFilterRepository.finnFilterForFilterBruker(veilederId);
        return filterModelListe.stream()
                .map(this::fjernDuplikatAvAapArenaFilterTilFrontend)
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

    // I en mellomfase hvor vi har trukket ut arena filteret for aap til et nytt et må vi lagre dobbelt.
    // I det vi har gått helt over til det nye filteret kan denne funksjonen slettes.
    private PortefoljeFilter leggTilAapFraYtelseINyttAapArenaFilter(PortefoljeFilter filter) {
        String aapYtelse = filter.getYtelse();
        boolean aapArenaUnntak = Objects.equals(aapYtelse, "AAP_UNNTAK");
        boolean aapArenaOrdinar = Objects.equals(aapYtelse, "AAP_MAXTID");
        boolean aapArena = Objects.equals(aapYtelse, "AAP");

        if (!aapArenaUnntak && !aapArenaOrdinar && !aapArena) {
            return filter;
        }

        List<String> aapNyttFilterListe;

        if (aapArenaUnntak) {
            aapNyttFilterListe = List.of("HAR_AAP_UNNTAK");
        } else if (aapArenaOrdinar) {
            aapNyttFilterListe = List.of("HAR_AAP_ORDINAR");
        } else {
            aapNyttFilterListe = List.of("HAR_AAP_ORDINAR", "HAR_AAP_UNNTAK");
        }

        filter.setYtelseAapArena(aapNyttFilterListe);
        return filter;
    }

    private Optional<FilterModel> fjernDuplikatAvAapArenaFilterTilFrontend(Optional<FilterModel> filterModelOptional) {
        return filterModelOptional.map(this::fjernDuplikatAvAapArenaFilterTilFrontend);
    }

    // Når toggle er på skal kun det nye AAP filteret returneres. Når den er av skal kun det gamle returneres.
    private FilterModel fjernDuplikatAvAapArenaFilterTilFrontend(FilterModel filter) {
        String veilederId = AuthUtils.getInnloggetVeilederIdent().toString();
        UnleashContext unleashContext = UnleashContext.builder()
                .userId(veilederId)
                .build();

        boolean brukNyttAapFilterErSkruddPå = defaultUnleash.isEnabled(FeatureToggle.BRUK_NYTT_ARENA_AAP_FILTER, unleashContext);

        if (brukNyttAapFilterErSkruddPå) {
            String gammeltYtelseFilter = filter.getFilterValg().getYtelse();
            if (gammeltYtelseFilter != null) {
                List<String> muligeAapYtelseFiltre = List.of("AAP", "AAP_MAXTID", "AAP_UNNTAK");
                if (muligeAapYtelseFiltre.contains(gammeltYtelseFilter)) {
                    filter.getFilterValg().setYtelse(null);
                }
            }
        } else {
            filter.getFilterValg().setYtelseAapArena(Collections.emptyList());
        }
        return filter;
    }
}
