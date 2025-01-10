package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.domene.value.ArenaHovedmal;
import no.nav.pto.veilarbfilter.domene.value.Hovedmal;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import org.springframework.stereotype.Service;

import java.util.*;

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


    public void erstattArenahovedmalMedHovedmalGjeldendeVedtak14aIFiltervalg(String veilederId, Integer filterId) {
        try {
            FilterModel filterSomSkalOppdateres = mineLagredeFilterRepository.hentFilter(filterId).orElseThrow(); // todo handter feil ved henting

            // Lag liste over migrerte hovedmål
            List<String> hovedmalFraArenahovedmal = lagGjeldendeVedtakHovedmalFraArenahovedmal(filterSomSkalOppdateres.getFilterValg().getHovedmal());

            // Slå saman nye og gamle HovedmalGjeldendeVedtak-filter
            List<String> hovedmalFraGjeldendeVedtak14a = filterSomSkalOppdateres.getFilterValg().getHovedmalGjeldendeVedtak14a();
            Set<String> alleUnikeHovedmal = new HashSet<>(hovedmalFraGjeldendeVedtak14a);
            alleUnikeHovedmal.addAll(hovedmalFraArenahovedmal);

            List<String> unikeSorterteHovedmal = alleUnikeHovedmal.stream().sorted().toList();

            // Lag oppdatert porteføljefilter
            PortefoljeFilter portefoljeFilterSomSkalOppdateres = filterSomSkalOppdateres.getFilterValg();
            portefoljeFilterSomSkalOppdateres.setHovedmalGjeldendeVedtak14a(unikeSorterteHovedmal);
            portefoljeFilterSomSkalOppdateres.setHovedmal(Collections.emptyList());

            // Lag klart oppdatert filtermodell og skriv tilbake til databasen
            filterSomSkalOppdateres.setFilterValg(portefoljeFilterSomSkalOppdateres);
            mineLagredeFilterRepository.oppdaterFilter(veilederId, filterSomSkalOppdateres); // todo handter feil ved skriving
        } catch (Exception e) {
            // todo feilhåndtering her
            throw e;
        }
    }

    private List<String> lagGjeldendeVedtakHovedmalFraArenahovedmal(List<String> arenahovedmal) {
        List<Hovedmal> hovedmalGjeldendeVedtak = arenahovedmal.stream()
                .map(ArenaHovedmal::valueOf)
                .map(ArenaHovedmal::mapTilHovedmalGjeldendeVedtak14a)
                .toList();


        return hovedmalGjeldendeVedtak.stream().map(Hovedmal::name).toList();
    }
}
