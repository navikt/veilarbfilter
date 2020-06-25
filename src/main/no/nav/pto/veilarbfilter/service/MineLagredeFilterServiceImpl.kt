package no.nav.pto.veilarbfilter.service

import no.nav.pto.veilarbfilter.config.dbQuery
import no.nav.pto.veilarbfilter.db.Filter
import no.nav.pto.veilarbfilter.db.MineLagredeFilter
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.MineLagredeFilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

class MineLagredeFilterServiceImpl() : FilterService {

    override suspend fun hentFilter(filterId: Int): FilterModel? = dbQuery {
        (Filter innerJoin MineLagredeFilter).slice(
            Filter.filterId,
            Filter.filterNavn,
            Filter.valgteFilter,
            Filter.opprettetDato,
            MineLagredeFilter.veilederId
        ).select { (Filter.filterId.eq(filterId)) }
            .mapNotNull { tilMineLagredeFilterModel(it) }
            .singleOrNull()
    }

    override suspend fun slettFilter(filterId: Int, veilederId: String): Int = dbQuery {
        val removedRows =
            MineLagredeFilter.deleteWhere { (MineLagredeFilter.filterId eq filterId) and (MineLagredeFilter.veilederId eq veilederId) }
        if (removedRows > 0) {
            Filter.deleteWhere { (Filter.filterId eq filterId) }
        } else {
            0
        }
    }

    override suspend fun lagreFilter(veilederId: String, nyttFilter: NyttFilterModel): FilterModel? {
        var key = 0;
        dbQuery {
            key = (Filter.insert {
                it[filterNavn] = nyttFilter.filterNavn
                it[valgteFilter] = nyttFilter.filterValg
                it[opprettetDato] = LocalDateTime.now()
            } get Filter.filterId)

            MineLagredeFilter.insert {
                it[filterId] = key
                it[MineLagredeFilter.veilederId] = veilederId
            }
        }
        return hentFilter(key)
    }

    override suspend fun oppdaterFilter(filterBrukerId: String, filterValg: FilterModel): FilterModel {
        dbQuery {
            val isValidUpdate =
                MineLagredeFilter.select { (MineLagredeFilter.filterId eq filterValg.filterId) and (MineLagredeFilter.veilederId eq filterBrukerId) }
                    .count() > 0
            if (isValidUpdate) {
                Filter
                    .update({ (Filter.filterId eq filterValg.filterId) }) {
                        it[filterNavn] = filterValg.filterNavn
                        it[valgteFilter] = filterValg.filterValg
                    }
            }
        }
        return hentFilter(filterValg.filterId)!!
    }

    private fun tilMineLagredeFilterModel(row: ResultRow): FilterModel =
        MineLagredeFilterModel(
            filterId = row[Filter.filterId],
            filterNavn = row[Filter.filterNavn],
            filterValg = row[Filter.valgteFilter],
            veilederId = row[MineLagredeFilter.veilederId],
            opprettetDato = row[Filter.opprettetDato]
        )

    override suspend fun finnFilterForFilterBruker(veilederId: String) = dbQuery {
        (Filter innerJoin MineLagredeFilter).slice(
            Filter.filterId,
            Filter.filterNavn,
            Filter.valgteFilter,
            Filter.opprettetDato,
            MineLagredeFilter.veilederId
        ).select { (MineLagredeFilter.veilederId.eq(veilederId)) }
            .mapNotNull { tilMineLagredeFilterModel(it) }
    }

}