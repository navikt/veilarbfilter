package no.nav.pto.veilarbfilter.service

import no.nav.pto.veilarbfilter.config.dbQuery
import no.nav.pto.veilarbfilter.db.EnhetensLagredeFilter
import no.nav.pto.veilarbfilter.db.Filter
import no.nav.pto.veilarbfilter.model.EnhetensLagredeFilterModel
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.model.SortOrder
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

class EnhetFilterServiceImpl() : FilterService {

    override suspend fun hentFilter(filterId: Int): EnhetensLagredeFilterModel? = dbQuery {
        (Filter innerJoin EnhetensLagredeFilter).slice(
            Filter.filterId,
            Filter.filterNavn,
            Filter.valgteFilter,
            EnhetensLagredeFilter.enhetId,
            EnhetensLagredeFilter.sortOrder
        ).select { (Filter.filterId.eq(filterId)) }
            .mapNotNull { tilEnhetFilterModel(it) }
            .singleOrNull()
    }

    override suspend fun finnFilterForFilterBruker(enhetId: String): List<EnhetensLagredeFilterModel> = dbQuery {
        (Filter innerJoin EnhetensLagredeFilter).slice(
            Filter.filterId,
            Filter.filterNavn,
            Filter.valgteFilter,
            EnhetensLagredeFilter.enhetId,
            EnhetensLagredeFilter.sortOrder
        ).select { (EnhetensLagredeFilter.enhetId.eq(enhetId)) }
            .mapNotNull { tilEnhetFilterModel(it) }
    }

    override suspend fun slettFilter(filterId: Int, enhetId: String): Int = dbQuery {
        val removedRows =
            EnhetensLagredeFilter.deleteWhere { (EnhetensLagredeFilter.filterId eq filterId) and (EnhetensLagredeFilter.enhetId eq enhetId) }
        if (removedRows > 0) {
            Filter.deleteWhere { (Filter.filterId eq filterId) }
        } else {
            0
        }
    }

    override suspend fun lagreSortering(filterBrukerId: String, sortOrder: List<SortOrder>): List<FilterModel> {
        TODO("Not yet implemented")
    }

    override suspend fun oppdaterFilter(enhetId: String, filter: FilterModel): FilterModel {
        dbQuery {
            (Filter innerJoin EnhetensLagredeFilter)
                .update({ (Filter.filterId eq filter.filterId) and (EnhetensLagredeFilter.enhetId eq enhetId) }) {
                    it[Filter.filterNavn] = filter.filterNavn
                    it[Filter.valgteFilter] = filter.filterValg
                }
        }
        return hentFilter(filter.filterId)!!
    }

    override suspend fun lagreFilter(enhetId: String, nyttFilter: NyttFilterModel): EnhetensLagredeFilterModel {
        var key = 0;
        dbQuery {
            key = (Filter.insert {
                it[filterNavn] = nyttFilter.filterNavn
                it[valgteFilter] = nyttFilter.filterValg
                it[opprettetDato] = LocalDateTime.now()
            } get Filter.filterId)

            EnhetensLagredeFilter.insert {
                it[filterId] = key
                it[EnhetensLagredeFilter.enhetId] = enhetId
            }
        }
        return hentFilter(key)!!
    }

    private fun tilEnhetFilterModel(row: ResultRow): EnhetensLagredeFilterModel =
        EnhetensLagredeFilterModel(
            filterId = row[Filter.filterId],
            filterNavn = row[Filter.filterNavn],
            filterValg = row[Filter.valgteFilter],
            enhetId = row[EnhetensLagredeFilter.enhetId],
            opprettetDato = row[Filter.opprettetDato],
            sortOrder = row[EnhetensLagredeFilter.sortOrder]
        )
}
