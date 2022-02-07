package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.SortOrder;
import no.nav.pto.veilarbfilter.repository.MineLagredeFilterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MineLagredeFilterService implements FilterService {
    private final MineLagredeFilterRepository mineLagredeFilterRepository;

    @Override
    public Optional<FilterModel> lagreFilter(String veilederId, NyttFilterModel nyttFilter) {
        return mineLagredeFilterRepository.lagreFilter(veilederId, nyttFilter);
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String veilederId, FilterModel filter) {
        return mineLagredeFilterRepository.oppdaterFilter(veilederId, filter);
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
}
