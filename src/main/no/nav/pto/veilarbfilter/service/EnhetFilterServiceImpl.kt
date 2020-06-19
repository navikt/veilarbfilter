package no.nav.pto.veilarbfilter.service

import no.nav.pto.veilarbfilter.config.dbQuery
import no.nav.pto.veilarbfilter.db.EnhetFilter
import no.nav.pto.veilarbfilter.db.Filter
import no.nav.pto.veilarbfilter.model.EnhetFilterModel
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

class EnhetFilterServiceImpl() : FilterService {

    override suspend fun hentFilter(filterId: Int): EnhetFilterModel? = dbQuery {
        (Filter innerJoin EnhetFilter).slice(
                Filter.filterId,
                Filter.filterNavn,
                Filter.valgteFilter,
                EnhetFilter.enhetId
        ).select { (Filter.filterId.eq(filterId)) }
                .mapNotNull { tilEnhetFilterModel(it) }
                .singleOrNull()
    }

    override suspend fun finnFilterForFilterBruker(enhetId: String): List<EnhetFilterModel> = dbQuery {
        (Filter innerJoin EnhetFilter).slice(
                Filter.filterId,
                Filter.filterNavn,
                Filter.valgteFilter,
                EnhetFilter.enhetId
        ).select { (EnhetFilter.enhetId.eq(enhetId)) }
                .mapNotNull { tilEnhetFilterModel(it) }
    }

    override suspend fun slettFilter(filterId: Int, enhetId: String): Int = dbQuery {
        val removedRows = EnhetFilter.deleteWhere { (EnhetFilter.filterId eq filterId) and (EnhetFilter.enhetId eq enhetId) }
        if (removedRows > 0) {
            Filter.deleteWhere { (Filter.filterId eq filterId) }
        } else {
            0
        }
    }

    override suspend fun oppdaterFilter(enhetId: String, filterValg: FilterModel): FilterModel {
        dbQuery {
            (Filter innerJoin EnhetFilter)
                    .update({ (Filter.filterId eq filterValg.filterId) and (EnhetFilter.enhetId eq enhetId) }) {
                        it[Filter.filterNavn] = filterValg.filterNavn
                        it[Filter.valgteFilter] = filterValg.filterValg
                    }
        }
        return hentFilter(filterValg.filterId)!!
    }

    override suspend fun lagreFilter(enhetId: String, nyttFilter: NyttFilterModel): EnhetFilterModel {
        var key = 0;
        dbQuery {
            key = (Filter.insert {
                it[filterNavn] = nyttFilter.filterNavn
                it[valgteFilter] = nyttFilter.filterValg
                it[opprettetDato] = LocalDateTime.now()
            } get Filter.filterId)

            EnhetFilter.insert {
                it[filterId] = key
                it[EnhetFilter.enhetId] = enhetId
            }
        }
        return hentFilter(key)!!
    }

    private fun tilEnhetFilterModel(row: ResultRow): EnhetFilterModel =
            EnhetFilterModel(
                    filterId = row[Filter.filterId],
                    filterNavn = row[Filter.filterNavn],
                    filterValg = row[Filter.valgteFilter],
                    enhetId = row[EnhetFilter.enhetId],
                    opprettetDato = row[Filter.opprettetDato]
            )
}