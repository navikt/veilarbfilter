package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.domene.value.ArenaHovedmal;
import no.nav.pto.veilarbfilter.domene.value.Hovedmal;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MineLagredeFilterService implements FilterService {
    private final MineLagredeFilterRepository mineLagredeFilterRepository;

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


    public void erstattArenahovedmalfiltervalgMedHovedmalGjeldendeVedtak14aFiltervalg(String veilederId, Integer filterId) {
        try {
            FilterModel filterSomSkalOppdateres = mineLagredeFilterRepository.hentFilter(filterId).orElseThrow(); // todo handter feil ved henting

            // Lag liste over migrerte hovedmål
            List<ArenaHovedmal> arenahovedmal = filterSomSkalOppdateres.getFilterValg().getHovedmal().stream().map(ArenaHovedmal::valueOf).toList();
            List<Hovedmal> hovedmalGjeldendeVedtak = arenahovedmal.stream()
                    .map(ArenaHovedmal::mapTilHovedmalGjeldendeVedtak14a)
                    .toList();
            List<String> hovedmalGjeldendeVedtakSomStreng = hovedmalGjeldendeVedtak.stream().map(it -> it.name()).toList();

            // Todo slå saman nye og gamle HovedmalGjeldendeVedtak-filter

            // Lag oppdatert porteføljefilter
            PortefoljeFilter portefoljeFilterSomSkalOppdateres = filterSomSkalOppdateres.getFilterValg();
            portefoljeFilterSomSkalOppdateres.setHovedmalGjeldendeVedtak14a(hovedmalGjeldendeVedtakSomStreng);
            portefoljeFilterSomSkalOppdateres.setHovedmal(Collections.emptyList());

            // Lag klart oppdatert filtermodell og skriv tilbake til databasen
            filterSomSkalOppdateres.setFilterValg(portefoljeFilterSomSkalOppdateres);
            mineLagredeFilterRepository.oppdaterFilter(veilederId, filterSomSkalOppdateres); // todo handter feil ved skriving
        } catch (Exception e) {
            // todo feilhåndtering her
            throw e;
        }
    }
}
