package no.nav.pto.veilarbfiltrering.service

import no.nav.pto.veilarbfiltrering.model.EnhetFilterModel
import no.nav.pto.veilarbfiltrering.model.FilterModel

interface VeilederFilterService {
    suspend fun lagreVeilederFilter (veilederId: String, filterValg: FilterModel) : EnhetFilterModel;
    suspend fun finnFilterForVeileder (veilederId: String) : EnhetFilterModel?;
    suspend fun oppdaterVeilederFilter (veilederId: String, filterValg: FilterModel) : EnhetFilterModel;
}