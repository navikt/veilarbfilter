package no.nav.pto.veilarbfilter.service

import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel

class VeilederGrupperService() : FilterService {
    override suspend fun lagreFilter(filterBrukerId: String, nyttFilter: NyttFilterModel): FilterModel {
        TODO("Not yet implemented")
    }

    override suspend fun oppdaterFilter(filterId: Int, filterValg: FilterModel): FilterModel {
        TODO("Not yet implemented")
    }

    override suspend fun hentFilter(filterId: Int): FilterModel? {
        TODO("Not yet implemented")
    }

    override suspend fun finnFilterForFilterBruker(filterBrukerId: String): List<FilterModel> {
        TODO("Not yet implemented")
    }

    override suspend fun slettFilter(filterId: Int): Int {
        TODO("Not yet implemented")
    }

    fun fjernIkkeAktiveVeiledereIVeilederGruppe(veilarbveilederClient: VeilarbveilederClient) {
        /*
            fetch all rows from table: veiledergrupper
            for each row
                - make api request to fetch all veilederer for that enhet
                - filter all veileder that are not longer active and update db
                - ask Lars: if groups is empty after cleanup we can remove group as well
         */

        /*
        val veilederePaEnheten = veilarbveilederClient
                        .hentVeilederePaEnheten(it, call.request.cookies["ID_token"])
                        ?: throw IllegalStateException()
         */
    }

    private fun cleanupVeilederGrupper(enhetId: String) {
        /*
        return listeMedFilter.map {
            val filtrerVeileder = filtrerVeilederSomErIkkePaEnheten(it, veilederePaEnheten)
            val nyttFilter  = it.filterValg.copy(veiledere = filtrerVeileder)
            oppdaterFilter(it.enhetId , FilterModel(it.filterId, it.filterNavn, nyttFilter, null))
        }
         */
    }

    /*
    private fun filtrerVeilederSomErIkkePaEnheten(
        lagretFilter: EnhetFilterModel,
        veilederePaEnheten: VeiledereResponse
    ): List<String> =
        lagretFilter.filterValg.veiledere.filter { veilederIdent ->
            veilederePaEnheten.veilederListe.map { it.ident }.contains(veilederIdent)
        }
     */
}