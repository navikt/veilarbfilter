package no.nav.pto.veilarbfilter.service

import no.nav.pto.veilarbfilter.client.VeiledereResponse
import no.nav.pto.veilarbfilter.model.EnhetFilterModel
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel

interface EnhetFilterService {
    suspend fun lagreEnhetFilter (enhetId: String, nyttFilter: NyttFilterModel) : EnhetFilterModel
    suspend fun finnFilterForEnhet (enhetId: String) : List<EnhetFilterModel>
    suspend fun oppdaterEnhetFilter (enhetId: String, filterValg: FilterModel) : EnhetFilterModel
    suspend fun hentFilter (filterId: Int) : EnhetFilterModel?
    suspend fun slettFilter (enhetId: String, filterId: String): Int
}
