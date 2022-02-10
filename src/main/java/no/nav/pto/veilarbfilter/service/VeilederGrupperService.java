package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.SortOrder;
import no.nav.pto.veilarbfilter.repository.VeilederGruppeFilterRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VeilederGrupperService implements FilterService {
    private final VeilederGruppeFilterRepository veilederGruppeFilterRepository;

    @Override
    public Optional<FilterModel> lagreFilter(String enhetId, NyttFilterModel nyttFilter) {
        return veilederGruppeFilterRepository.lagreFilter(enhetId, nyttFilter);
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String enhetId, FilterModel filter) {
        return veilederGruppeFilterRepository.oppdaterFilter(enhetId, filter);
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
        /*final OppfolgingsBruker bruker = select(db, VW_PORTEFOLJE_INFO.TABLE_NAME, DbUtils::mapTilOppfolgingsBruker)
                .column("*")
                .where(WhereClause.equals("FODSELSNR", fnr.toString()))
                .execute();
         */
        return Collections.emptyList();
    }
}
