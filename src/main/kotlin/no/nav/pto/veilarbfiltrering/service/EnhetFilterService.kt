package no.nav.pto.veilarbfiltrering.service

import no.nav.pto.veilarbfiltrering.model.EnhetFilterModel
import no.nav.pto.veilarbfiltrering.model.FilterModel
import no.nav.pto.veilarbfiltrering.model.NyttFilterModel

interface EnhetFilterService {
    suspend fun lagreEnhetFilter (enhetId: String, nyttFilter: NyttFilterModel) : EnhetFilterModel;
    suspend fun finnFilterForEnhet (enhetId: String) : List<EnhetFilterModel>;
    suspend fun oppdaterEnhetFilter (enhetId: String, filterValg: FilterModel) : EnhetFilterModel;
    suspend fun hentFilter (filterId: Int) : EnhetFilterModel?;
}