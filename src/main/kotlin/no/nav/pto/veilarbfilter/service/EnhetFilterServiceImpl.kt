package no.nav.pto.veilarbfilter.service

import no.nav.pto.veilarbfilter.config.dbQuery
import no.nav.pto.veilarbfilter.model.EnhetFilter
import no.nav.pto.veilarbfilter.model.EnhetFilterModel
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

class EnhetFilterServiceImpl (): EnhetFilterService {

    override suspend fun hentFilter(filterId: Int): EnhetFilterModel? = dbQuery {
        EnhetFilter.select { (EnhetFilter.filterId eq filterId) }
            .mapNotNull { tilEnhetFilterModel(it) }
            .singleOrNull()
    }

    override suspend fun slettFilter(enhetId: String, filterId: String): Int  = dbQuery {
        EnhetFilter.deleteWhere {(EnhetFilter.enhet eq enhetId) and (EnhetFilter.filterId eq filterId.toInt()) }
    }

    override suspend fun finnFilterForEnhet(enhetId: String): List<EnhetFilterModel> = dbQuery {
        EnhetFilter.select { (EnhetFilter.enhet eq enhetId) }
            .mapNotNull { tilEnhetFilterModel(it) }
    }

    override suspend fun oppdaterEnhetFilter(enhetId: String, filterValg: FilterModel): EnhetFilterModel {
        dbQuery {
            EnhetFilter.update({ (EnhetFilter.enhet eq enhetId) and (EnhetFilter.filterId eq filterValg.filterId) }) {
                it[filterNavn] = filterValg.filterNavn
                it[valgteFilter] = filterValg.filterValg
                it[enhet] = enhetId
            }
        }
        return hentFilter(filterValg.filterId)!!
    }

    override suspend fun lagreEnhetFilter(enhetId: String, nyttFilter: NyttFilterModel): EnhetFilterModel {
        var key = 0
        dbQuery {
            key = (EnhetFilter.insert {
                it[filterNavn] = nyttFilter.filterNavn
                it[valgteFilter] = nyttFilter.filterValg
                it[enhet] = enhetId
                it[opprettetDato] = LocalDateTime.now()
            } get EnhetFilter.filterId)
        }
        return hentFilter(key)!!
    }

    private fun tilEnhetFilterModel(row: ResultRow): EnhetFilterModel =
        EnhetFilterModel(
            filterId = row[EnhetFilter.filterId],
            filterNavn = row[EnhetFilter.filterNavn],
            filterValg = row[EnhetFilter.valgteFilter],
            enhetId = row[EnhetFilter.enhet],
            opprettetDato = row[EnhetFilter.opprettetDato]
        )
}
