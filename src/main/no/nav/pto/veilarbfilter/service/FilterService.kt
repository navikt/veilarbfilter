package no.nav.pto.veilarbfilter.service


import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel

interface FilterService {
    suspend fun lagreFilter(filterBrukerId: String, nyttFilter: NyttFilterModel): FilterModel?
    suspend fun oppdaterFilter(filterBrukerId: String, filter: FilterModel): FilterModel
    suspend fun hentFilter(filterId: Int): FilterModel?
    suspend fun finnFilterForFilterBruker(filterBrukerId: String): List<FilterModel>
    suspend fun slettFilter(filterId: Int, filterBrukerId: String): Int
}
