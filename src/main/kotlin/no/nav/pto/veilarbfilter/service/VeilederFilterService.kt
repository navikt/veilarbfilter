package no.nav.pto.veilarbfilter.service

import no.nav.pto.veilarbfilter.model.EnhetFilterModel
import no.nav.pto.veilarbfilter.model.FilterModel

interface VeilederFilterService {
    suspend fun lagreVeilederFilter (veilederId: String, filterValg: FilterModel) : EnhetFilterModel;
    suspend fun finnFilterForVeileder (veilederId: String) : EnhetFilterModel?;
    suspend fun oppdaterVeilederFilter (veilederId: String, filterValg: FilterModel) : EnhetFilterModel;
}
