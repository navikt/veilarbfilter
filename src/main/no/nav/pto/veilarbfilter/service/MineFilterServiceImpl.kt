package no.nav.pto.veilarbfilter.service

import no.nav.pto.veilarbfilter.config.dbQuery
import no.nav.pto.veilarbfilter.db.Filter
import no.nav.pto.veilarbfilter.db.MineFilter
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.MineFilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

class MineFilterServiceImpl() : FilterService {

    override suspend fun hentFilter(filterId: Int): FilterModel? = dbQuery {
        (Filter innerJoin MineFilter).slice(
            Filter.filterId,
            Filter.filterNavn,
            Filter.valgteFilter,
            Filter.opprettetDato,
            MineFilter.veilederId
        ).select { (Filter.filterId.eq(filterId)) }
            .mapNotNull { tilMineFilterModel(it) }
            .singleOrNull()
    }

    override suspend fun slettFilter(filterId: Int, veilederId: String): Int = dbQuery {
        val removedRows = MineFilter.deleteWhere { (MineFilter.filterId eq filterId) and (MineFilter.veilederId eq veilederId) }
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

            MineFilter.insert {
                it[filterId] = key
                it[MineFilter.veilederId] = veilederId
            }
        }
        return hentFilter(key)
    }

    override suspend fun oppdaterFilter(filterBrukerId: String, filterValg: FilterModel): FilterModel {
        dbQuery {
            (Filter innerJoin MineFilter)
                .update({ (Filter.filterId eq filterValg.filterId) and (MineFilter.veilederId eq filterBrukerId)}) {
                    it[Filter.filterNavn] = filterValg.filterNavn
                    it[Filter.valgteFilter] = filterValg.filterValg
                }
        }
        return hentFilter(filterValg.filterId)!!
    }

    private fun tilMineFilterModel(row: ResultRow): FilterModel =
        MineFilterModel(
            filterId = row[Filter.filterId],
            filterNavn = row[Filter.filterNavn],
            filterValg = row[Filter.valgteFilter],
            veilederId = row[MineFilter.veilederId],
            opprettetDato = row[Filter.opprettetDato]
        )

    override suspend fun finnFilterForFilterBruker(veilederId: String) = dbQuery {
        (Filter innerJoin MineFilter).slice(
            Filter.filterId,
            Filter.filterNavn,
            Filter.valgteFilter,
            Filter.opprettetDato,
            MineFilter.veilederId
        ).select { (MineFilter.veilederId.eq(veilederId)) }
            .mapNotNull { tilMineFilterModel(it) }
    }

}