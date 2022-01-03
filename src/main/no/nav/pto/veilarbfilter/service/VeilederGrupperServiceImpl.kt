package no.nav.pto.veilarbfilter.service

import kotlinx.coroutines.runBlocking
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.config.dbQuery
import no.nav.pto.veilarbfilter.db.Filter
import no.nav.pto.veilarbfilter.db.VeilederGrupperFilter
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.model.SortOrder
import no.nav.pto.veilarbfilter.model.VeilederGruppeFilterModel
import org.jetbrains.exposed.sql.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class VeilederGrupperServiceImpl(
    private val veilarbveilederClient: VeilarbveilederClient,
    private val mineLagredeFilterService: MineLagredeFilterServiceImpl
) : FilterService {
    private val log = LoggerFactory.getLogger("VeilederGrupperServiceImpl")

    override suspend fun lagreFilter(enhetId: String, nyttFilter: NyttFilterModel): FilterModel? {
        var key = 0
        dbQuery {
            key = (Filter.insert {
                it[filterNavn] = nyttFilter.filterNavn
                it[valgteFilter] = nyttFilter.filterValg
                it[opprettetDato] = LocalDateTime.now()
                it[filterCleanup] = 0
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
                        it[filterCleanup] = filter.filterCleanup
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
            Filter.opprettetDato,
            Filter.filterCleanup
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
            opprettetDato = row[Filter.opprettetDato],
            filterCleanup = row[Filter.filterCleanup]
        )

    override suspend fun finnFilterForFilterBruker(enhetId: String): List<FilterModel> = dbQuery {
        (Filter innerJoin VeilederGrupperFilter).slice(
            Filter.filterId,
            Filter.filterNavn,
            Filter.valgteFilter,
            VeilederGrupperFilter.enhetId,
            Filter.opprettetDato,
            Filter.filterCleanup
        ).select { (VeilederGrupperFilter.enhetId eq enhetId) }
            .mapNotNull { tilVeilederGruppeFilterModel(it) }
    }


    override suspend fun slettFilter(filterId: Int, enhetId: String): Int {
        val veilederGruppe = hentFilter(filterId)
        if (veilederGruppe === null) {
            return 0
        }
        var erSlettet = 0

        dbQuery {
            val removedRows =
                VeilederGrupperFilter.deleteWhere { (VeilederGrupperFilter.filterId eq filterId) and (VeilederGrupperFilter.enhetId eq enhetId) }
            if (removedRows > 0) {
                Filter.deleteWhere { (Filter.filterId eq filterId) }
                runBlocking {
                    mineLagredeFilterService.deactivateMineFilterWithDeletedVeilederGroup(
                        veilederGruppe.filterNavn,
                        veilederGruppe.filterValg.veiledere
                    )
                }
                erSlettet = 1
            }
        }
        return erSlettet
    }

    override suspend fun lagreSortering(filterBrukerId: String, sortOrder: List<SortOrder>): List<FilterModel> {
        TODO("Not yet implemented")
    }

    suspend fun slettVeiledereSomIkkeErAktivePaEnheten(enhetId: String) {
        val veilederePaEnheten = veilarbveilederClient
            .hentVeilederePaEnheten(enhetId)
            ?: throw IllegalStateException("Can not get veiledere for enhet " + enhetId)

        val filterForBruker = finnFilterForFilterBruker(enhetId)

        filterForBruker.forEach {
            val alleVeiledere = it.filterValg.veiledere
            val aktiveVeileder = alleVeiledere.filter { veilederIdent ->
                veilederePaEnheten.contains(veilederIdent)
            }
            val removedVeileder = getRemovedVeiledere(alleVeiledere, aktiveVeileder)

            if (aktiveVeileder.isEmpty()) {
                log.warn("Removed veiledere: $removedVeileder")
                slettFilter(filterId = it.filterId, enhetId = enhetId)
                log.warn("Removed veiledergruppe: ${it.filterNavn} from enhet: ${enhetId}")
            } else if (aktiveVeileder.size < alleVeiledere.size) {
                log.warn("Removed veiledere: $removedVeileder")
                val nyttFilter = it.filterValg.copy(veiledere = aktiveVeileder)
                oppdaterFilter(enhetId, FilterModel(it.filterId, it.filterNavn, nyttFilter, it.opprettetDato, 1))
                log.warn("Updated veiledergruppe: ${it.filterNavn} from enhet: ${enhetId}")
            }
        }
    }

    private fun getRemovedVeiledere(alleVeiledere: List<String>, aktiveVeileder: List<String>): List<String> {
        return alleVeiledere.filter { veilederIdent -> !aktiveVeileder.contains(veilederIdent) }
    }

    suspend fun hentAlleEnheter(): List<String> =
        dbQuery {
            VeilederGrupperFilter.slice(VeilederGrupperFilter.enhetId)
                .selectAll()
                .distinct()
                .map { it[VeilederGrupperFilter.enhetId] }
        }
}



