package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
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

    @Override
    public Optional<FilterModel> lagreFilter(String veilederId, NyttFilterModel nyttFilter) throws IllegalArgumentException {
        try {
            PortefoljeFilter kopiertAapYtelseTilNyttAapFilterHvisDetFinnes = leggTilAapFraYtelseINyttAapArenaFilter(nyttFilter.getFilterValg());
            NyttFilterModel oppdatertNyttFilter = new NyttFilterModel(nyttFilter.getFilterNavn(), kopiertAapYtelseTilNyttAapFilterHvisDetFinnes);
            return mineLagredeFilterRepository.lagreFilter(veilederId, oppdatertNyttFilter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String veilederId, FilterModel filter) throws IllegalArgumentException {
        try {
            PortefoljeFilter kopiertAapYtelseTilNyttAapFilterHvisDetFinnes = leggTilAapFraYtelseINyttAapArenaFilter(filter.getFilterValg());
            FilterModel oppdatertFilter =
                    new FilterModel(filter.getFilterId(),
                            filter.getFilterNavn(),
                            kopiertAapYtelseTilNyttAapFilterHvisDetFinnes,
                            filter.getOpprettetDato(),
                            filter.getFilterCleanup()
                    );
            return mineLagredeFilterRepository.oppdaterFilter(veilederId, oppdatertFilter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> hentFilter(Integer filterId) {
        return mineLagredeFilterRepository.hentFilter(filterId);
    }

    @Override
    public List<FilterModel> finnFilterForFilterBruker(String veilederId) {
        return mineLagredeFilterRepository.finnFilterForFilterBruker(veilederId);
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
}
