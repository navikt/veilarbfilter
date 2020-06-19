package no.nav.pto.veilarbfilter.service

import no.nav.pto.veilarbfilter.config.dbQuery
import no.nav.pto.veilarbfilter.db.Filter
import no.nav.pto.veilarbfilter.db.VeilederGrupperFilter
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.model.VeilederGruppeFilterModel
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

class VeilederGrupperService() : FilterService {

    override suspend fun lagreFilter(enhetId: String, nyttFilter: NyttFilterModel): FilterModel? {
        var key = 0;
        dbQuery {
            key = (Filter.insert {
                it[filterNavn] = nyttFilter.filterNavn
                it[valgteFilter] = nyttFilter.filterValg
                it[opprettetDato] = LocalDateTime.now()
            } get Filter.filterId)

            VeilederGrupperFilter.insert {
                it[filterId] = key
                it[VeilederGrupperFilter.enhetId] = enhetId
            }
        }
        return hentFilter(key)
    }

    override suspend fun oppdaterFilter(enhetId: String, filterValg: FilterModel): FilterModel {
        dbQuery {
            (Filter innerJoin VeilederGrupperFilter)
                .update({ (Filter.filterId eq filterValg.filterId) and (VeilederGrupperFilter.enhetId eq enhetId) }) {
                    it[Filter.filterNavn] = filterValg.filterNavn
                    it[Filter.valgteFilter] = filterValg.filterValg
                }
        }
        return hentFilter(filterValg.filterId)!!
    }


    override suspend fun hentFilter(filterId: Int): FilterModel? = dbQuery {
        (Filter innerJoin VeilederGrupperFilter).slice(
            Filter.filterId,
            Filter.filterNavn,
            Filter.valgteFilter,
            VeilederGrupperFilter.enhetId
        ).select { (Filter.filterId eq filterId) }
            .mapNotNull { tilVeilederGruppeFilterModel(it) }
            .singleOrNull()
    }

    private fun tilVeilederGruppeFilterModel(row: ResultRow): FilterModel =
        VeilederGruppeFilterModel(
            filterId = row[Filter.filterId],
            filterNavn = row[Filter.filterNavn],
            filterValg = row[Filter.valgteFilter],
            enhetId = row[VeilederGrupperFilter.enhetId],
            opprettetDato = row[Filter.opprettetDato]
        )

    override suspend fun finnFilterForFilterBruker(enhetId: String): List<FilterModel> = dbQuery {
        (Filter innerJoin VeilederGrupperFilter).slice(
            Filter.filterId,
            Filter.filterNavn,
            Filter.valgteFilter,
            VeilederGrupperFilter.enhetId
        ).select { (VeilederGrupperFilter.enhetId eq enhetId) }
            .mapNotNull { tilVeilederGruppeFilterModel(it) }
    }


    override suspend fun slettFilter(filterId: Int, enhetId: String): Int = dbQuery {
        val removedRows =
            VeilederGrupperFilter.deleteWhere { (VeilederGrupperFilter.filterId eq filterId) and (VeilederGrupperFilter.enhetId eq enhetId) }
        if (removedRows > 0) {
            Filter.deleteWhere { (Filter.filterId eq filterId) }
        } else {
            0
        }
    }

}
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

        /*
    private fun cleanupVeilederGrupper(enhetId: String) {
        return listeMedFilter.map {
            val filtrerVeileder = filtrerVeilederSomErIkkePaEnheten(it, veilederePaEnheten)
            val nyttFilter  = it.filterValg.copy(veiledere = filtrerVeileder)
            oppdaterFilter(it.enhetId , FilterModel(it.filterId, it.filterNavn, nyttFilter, null))
        }
         */


    /*
    private fun filtrerVeilederSomErIkkePaEnheten(
        lagretFilter: EnhetFilterModel,
        veilederePaEnheten: VeiledereResponse
    ): List<String> =
        lagretFilter.filterValg.veiledere.filter { veilederIdent ->
            veilederePaEnheten.veilederListe.map { it.ident }.contains(veilederIdent)
        }
     */