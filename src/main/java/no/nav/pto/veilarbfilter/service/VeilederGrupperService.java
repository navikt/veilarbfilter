package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.EnhetId;
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient;
import no.nav.pto.veilarbfilter.domene.*;
import no.nav.pto.veilarbfilter.repository.VeilederGruppeFilterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VeilederGrupperService implements FilterService {
    private final VeilederGruppeFilterRepository veilederGruppeFilterRepository;
    private final VeilarbveilederClient veilarbveilederClient;

    @Override
    public Optional<FilterModel> lagreFilter(String enhetId, NyttFilterModel nyttFilter) throws IllegalArgumentException {
        try {
            return veilederGruppeFilterRepository.lagreFilter(enhetId, nyttFilter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String enhetId, FilterModel filter) throws IllegalArgumentException {
        try {
            return veilederGruppeFilterRepository.oppdaterFilter(enhetId, filter);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public Optional<FilterModel> hentFilter(Integer filterId) {
        return veilederGruppeFilterRepository.hentFilter(filterId);
    }

    @Override
    public List<FilterModel> finnFilterForFilterBruker(String enhetId) {
        return veilederGruppeFilterRepository.finnFilterForFilterBruker(enhetId);
    }

    @Override
    public Integer slettFilter(Integer filterId, String enhetId) {
        return veilederGruppeFilterRepository.slettFilter(filterId, enhetId);
    }

    @Override
    public List<FilterModel> lagreSortering(String enhetId, List<SortOrder> sortOrder) {
        return veilederGruppeFilterRepository.lagreSortering(enhetId, sortOrder);
    }

    public List<String> hentAlleEnheter() {
        return veilederGruppeFilterRepository.hentAlleEnheter();
    }

    public void slettVeiledereSomIkkeErAktivePaEnheten(String enhetId) {
        List<String> veilederePaEnheten = veilarbveilederClient.hentVeilederePaaEnhet(EnhetId.of(enhetId));

        List<FilterModel> filterForBruker = finnFilterForFilterBruker(enhetId);

        filterForBruker.forEach(filter -> {
            List<String> alleVeiledere = filter.getFilterValg().getVeiledere();
            List<String> aktiveVeileder = alleVeiledere.stream().filter(veilederIdent -> veilederePaEnheten.contains(veilederIdent)).collect(Collectors.toList());

            String removedVeileder = getRemovedVeiledere(alleVeiledere, aktiveVeileder);

            if (aktiveVeileder.isEmpty()) {
                log.warn("Removed veiledere: " + removedVeileder);
                slettFilter(filter.getFilterId(), enhetId);
                log.warn("Removed veiledergruppe: " + filter.getFilterNavn() + " from enhet: " + enhetId);
            } else if (aktiveVeileder.size() < alleVeiledere.size()) {
                log.warn("Removed veiledere: " + removedVeileder);

                PortefoljeFilter filterValg = filter.getFilterValg();
                filterValg.setVeiledere(aktiveVeileder);
                VeilederGruppeFilterModel updatedVeilederGruppeFilterModel = new VeilederGruppeFilterModel(filter.getFilterId(), filter.getFilterNavn(), filterValg, filter.getOpprettetDato(), 1, enhetId);
                oppdaterFilter(enhetId, updatedVeilederGruppeFilterModel);
                log.warn("Updated veiledergruppe: " + filter.getFilterNavn() + " from enhet: " + enhetId);
            }
        });
    }

    private String getRemovedVeiledere(List<String> alleVeiledere, List<String> aktiveVeileder) {
        return alleVeiledere.stream()
                .filter(veilederIdent -> !aktiveVeileder.contains(veilederIdent))
                .collect(Collectors.joining(", "));
    }
}
