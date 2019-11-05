package no.nav.pto.veilarbfiltrering.service

import no.nav.pto.veilarbfiltrering.config.dbQuery;
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import no.nav.pto.veilarbfiltrering.model.EnhetFilter
import no.nav.pto.veilarbfiltrering.model.EnhetFilterModel
import no.nav.pto.veilarbfiltrering.model.FilterModel
import no.nav.pto.veilarbfiltrering.model.NyttFilterModel

class EnhetFilterServiceImpl (): EnhetFilterService {

    override suspend fun hentFilter(filterId: Int): EnhetFilterModel? = dbQuery {
        EnhetFilter.select { (EnhetFilter.filterId eq filterId) }
            .mapNotNull { tilEnhetFilterModel(it) }
            .singleOrNull()
    }

    override suspend fun finnFilterForEnhet(enhetId: String): List<EnhetFilterModel> = dbQuery {
        EnhetFilter.select { (EnhetFilter.enhet eq enhetId) }
            .mapNotNull { tilEnhetFilterModel(it) }
    }

    override suspend fun oppdaterEnhetFilter(enhetId: String, filterValg: FilterModel): EnhetFilterModel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun lagreEnhetFilter(enhetId: String, nyttFilter: NyttFilterModel): EnhetFilterModel {
       var key = 0;
        dbQuery {
            key = (EnhetFilter.insert {
                it[filterNavn] = nyttFilter.filterNavn
                it[valgteFilter] = nyttFilter.filterValg
                it[enhet] = enhetId
            } get EnhetFilter.filterId)
        }
        return hentFilter(key)!!
    }

    private fun tilEnhetFilterModel(row: ResultRow): EnhetFilterModel =
        EnhetFilterModel(
            filterId = row[EnhetFilter.filterId],
            filterNavn = row[EnhetFilter.filterNavn],
            filterValg = row[EnhetFilter.valgteFilter],
            enhetId = row[EnhetFilter.enhet]
        )
}