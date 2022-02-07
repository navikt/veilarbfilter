package no.nav.pto.veilarbfilter.service;

import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.SortOrder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VeilederGrupperService implements FilterService {
    private final JdbcTemplate db;

    @Override
    public Optional<FilterModel> lagreFilter(String filterBrukerId, NyttFilterModel nyttFilter) {
        return null;
    }

    @Override
    public Optional<FilterModel> oppdaterFilter(String filterBrukerId, FilterModel filter) {
        return null;
    }

    @Override
    public Optional<FilterModel> hentFilter(Integer filterId) {
        return null;
    }

    @Override
    public List<FilterModel> finnFilterForFilterBruker(String filterBrukerId) {
        return null;
    }

    @Override
    public Integer slettFilter(Integer filterId, String filterBrukerId) {
        return null;
    }

    @Override
    public List<FilterModel> lagreSortering(String filterBrukerId, List<SortOrder> sortOrder) {
        return null;
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
