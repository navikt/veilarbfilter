package no.nav.pto.veilarbfilter.repository;

import no.nav.pto.veilarbfilter.domene.FilterModel;
import no.nav.pto.veilarbfilter.domene.NyttFilterModel;
import no.nav.pto.veilarbfilter.domene.SortOrder;

import java.util.List;
import java.util.Optional;

public interface FilterService {
    Optional<FilterModel> lagreFilter(String filterBrukerId, NyttFilterModel nyttFilter);

    Optional<FilterModel> oppdaterFilter(String filterBrukerId, FilterModel filter);

    Optional<FilterModel> hentFilter(Integer filterId);

    List<FilterModel> finnFilterForFilterBruker(String filterBrukerId);

    Integer slettFilter(Integer filterId, String filterBrukerId);

    List<FilterModel> lagreSortering(String filterBrukerId, List<SortOrder> sortOrder);
}
