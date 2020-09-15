package no.nav.pto.veilarbfilter.service

import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.config.dbQuery
import no.nav.pto.veilarbfilter.db.Filter
import no.nav.pto.veilarbfilter.db.VeilederGrupperFilter
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.model.VeilederGruppeFilterModel
import org.jetbrains.exposed.sql.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class VeilederGrupperServiceImpl(veilarbveilederClient: VeilarbveilederClient) : FilterService {
    private val veilarbveilederClient: VeilarbveilederClient = veilarbveilederClient;
    private val log = LoggerFactory.getLogger("VeilederGrupperServiceImpl")

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

    override suspend fun oppdaterFilter(enhetId: String, filter: FilterModel): FilterModel {
        dbQuery {
            val isValidUpdate =
                    (VeilederGrupperFilter).select { (VeilederGrupperFilter.filterId eq filter.filterId) and (VeilederGrupperFilter.enhetId eq enhetId) }
                            .count() > 0
            if (isValidUpdate) {
                Filter
                        .update({ (Filter.filterId eq filter.filterId) }) {
                            it[filterNavn] = filter.filterNavn
                            it[valgteFilter] = filter.filterValg
                        }
            }
        }
        return hentFilter(filter.filterId)!!
    }


    override suspend fun hentFilter(filterId: Int): FilterModel? = dbQuery {
        (Filter innerJoin VeilederGrupperFilter).slice(
                Filter.filterId,
                Filter.filterNavn,
                Filter.valgteFilter,
                VeilederGrupperFilter.enhetId,
                Filter.opprettetDato
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
                VeilederGrupperFilter.enhetId,
                Filter.opprettetDato
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

    suspend fun slettVeiledereSomIkkeErAktivePaEnheten(enhetId: String) {
        val veilederePaEnheten = veilarbveilederClient
                .hentVeilederePaEnheten(enhetId)
                ?: throw IllegalStateException("Can not get veiledere for enhet " + enhetId);

        val filterForBruker = finnFilterForFilterBruker(enhetId);

        filterForBruker.forEach {
            log.info("Veiledergruppe fore filter: {}", it.filterValg.veiledere)
            val filtrerVeileder = filtrerVeilederSomErIkkePaEnheten(it, veilederePaEnheten)
            val nyttFilter = it.filterValg.copy(veiledere = filtrerVeileder)
            log.info("Veiledergruppe etter filter: {}", nyttFilter.veiledere)
            oppdaterFilter(enhetId, FilterModel(it.filterId, it.filterNavn, nyttFilter, it.opprettetDato))
        }
    }

    private fun filtrerVeilederSomErIkkePaEnheten(
            lagretFilter: FilterModel,
            veilederePaEnheten: List<String>
    ): List<String> =
            lagretFilter.filterValg.veiledere.filter { veilederIdent ->
                veilederePaEnheten.contains(veilederIdent)
            }

    suspend fun hentAlleEnheter(): List<String> =
            dbQuery {
                VeilederGrupperFilter.slice(VeilederGrupperFilter.enhetId)
                        .selectAll()
                        .distinct()
                        .mapNotNull { it[VeilederGrupperFilter.enhetId] }
            }
}



