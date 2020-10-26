package no.nav.pto.veilarbfilter.service

import kotlinx.coroutines.runBlocking
import no.nav.pto.veilarbfilter.client.VeilarbveilederClient
import no.nav.pto.veilarbfilter.config.dbQuery
import no.nav.pto.veilarbfilter.db.Filter
import no.nav.pto.veilarbfilter.db.MineLagredeFilter
import no.nav.pto.veilarbfilter.model.*
import no.nav.pto.veilarbfilter.model.SortOrder
import org.jetbrains.exposed.sql.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import kotlin.streams.toList

class MineLagredeFilterServiceImpl(veilarbveilederClient: VeilarbveilederClient) {
    private val log = LoggerFactory.getLogger("MineLagredeFilterServiceImpl")
    private val veilarbveilederClient: VeilarbveilederClient = veilarbveilederClient;

    suspend fun hentFilter(filterId: Int): FilterModel? {
        try {
            return dbQuery {

                (Filter innerJoin MineLagredeFilter).slice(
                    Filter.filterId,
                    Filter.filterNavn,
                    Filter.valgteFilter,
                    Filter.opprettetDato,
                    MineLagredeFilter.veilederId,
                    MineLagredeFilter.sortOrder,
                    Filter.filterCleanup
                ).select { (Filter.filterId.eq(filterId)) }
                    .mapNotNull { tilFilterModel(it) }
                    .singleOrNull()
            }
        } catch (e: Exception) {
            log.error("Hent filter error", e)
            return null
        }
    }

    suspend fun slettFilter(filterId: Int, veilederId: String): Int = dbQuery {
        val removedRows =
            MineLagredeFilter.deleteWhere { (MineLagredeFilter.filterId eq filterId) and (MineLagredeFilter.veilederId eq veilederId) }
        if (removedRows > 0) {
            Filter.deleteWhere { (Filter.filterId eq filterId) }
        } else {
            0
        }
    }

    suspend fun lagreSortering(veilederId: String, sortOrder: List<SortOrder>, enhetId: String): List<FilterModel> {
        val filterIdsList = sortOrder.stream().map { it.filterId }.toList()
        var isValidUpdate = false
        dbQuery {
            isValidUpdate =
                (Filter innerJoin MineLagredeFilter).select {
                    (MineLagredeFilter.filterId inList filterIdsList) and
                            (MineLagredeFilter.veilederId eq veilederId) and
                            (MineLagredeFilter.enhetId eq enhetId)
                }
                    .count() == filterIdsList.size

            if (isValidUpdate) {
                var sortOrderValue: Int;
                sortOrder.forEach {
                    sortOrderValue = it.sortOrder
                    MineLagredeFilter
                        .update({ (MineLagredeFilter.filterId eq it.filterId) }) {
                            it[MineLagredeFilter.sortOrder] = sortOrderValue
                        }
                }
            }
        }
        return finnFilterForFilterBruker(veilederId, enhetId);
    }

    suspend fun lagreFilter(
        veilederId: String,
        nyttFilter: NyttFilterModel,
        enhetId: String
    ): FilterModel? {
        var key = 0;
        var erUgyldigNavn = true;
        var erUgyldigFiltervalg = true;

        validerNavn(nyttFilter.filterNavn)
        validerValg(nyttFilter.filterValg)

        dbQuery {
            erUgyldigNavn = (Filter innerJoin MineLagredeFilter).slice(
                Filter.filterNavn,
                Filter.filterId,
                MineLagredeFilter.veilederId
            ).select {
                (Filter.filterNavn eq nyttFilter.filterNavn) and
                        (MineLagredeFilter.veilederId eq veilederId) and
                        (MineLagredeFilter.enhetId eq enhetId)
            }.count() > 0
        }

        dbQuery {
            erUgyldigFiltervalg = (Filter innerJoin MineLagredeFilter).slice(
                Filter.valgteFilter,
                Filter.filterId,
                MineLagredeFilter.veilederId
            ).select {
                (Filter.valgteFilter eq nyttFilter.filterValg) and
                        (MineLagredeFilter.veilederId eq veilederId) and
                        (MineLagredeFilter.enhetId eq enhetId)
            }.count() > 0
        }

        validerUnikhet(!erUgyldigNavn, !erUgyldigFiltervalg)

        dbQuery {
            key = (Filter.insert {
                it[filterNavn] = nyttFilter.filterNavn
                it[valgteFilter] = nyttFilter.filterValg
                it[opprettetDato] = LocalDateTime.now()
            } get Filter.filterId)

            MineLagredeFilter.insert {
                it[filterId] = key
                it[MineLagredeFilter.veilederId] = veilederId
                it[MineLagredeFilter.enhetId] = enhetId
            }

        }

        return hentFilter(key)
    }

    suspend fun oppdaterFilter(
        veilederId: String,
        filter: FilterModel,
        enhetId: String
    ): FilterModel {
        var erUgyldigNavn = true;
        var erUgyldigFiltervalg = true;

        validerValg(filter.filterValg)
        validerNavn(filter.filterNavn)

        dbQuery {
            erUgyldigNavn = (Filter innerJoin MineLagredeFilter).slice(
                Filter.filterNavn,
                Filter.filterId,
                MineLagredeFilter.veilederId
            ).select {
                (Filter.filterNavn eq filter.filterNavn) and
                        (MineLagredeFilter.veilederId eq veilederId) and
                        (Filter.filterId neq filter.filterId) and
                        (MineLagredeFilter.enhetId eq enhetId)
            }.count() > 0
        }

        dbQuery {
            erUgyldigFiltervalg = (Filter innerJoin MineLagredeFilter).slice(
                Filter.valgteFilter,
                Filter.filterId,
                MineLagredeFilter.veilederId
            ).select {
                (Filter.valgteFilter eq filter.filterValg) and
                        (MineLagredeFilter.veilederId eq veilederId) and
                        (Filter.filterId neq filter.filterId) and
                        (MineLagredeFilter.enhetId eq enhetId)
            }.count() > 0
        }

        validerUnikhet(!erUgyldigNavn, !erUgyldigFiltervalg)

        dbQuery {
            val isValidUpdate =
                MineLagredeFilter.select {
                    (MineLagredeFilter.filterId eq filter.filterId) and
                            (MineLagredeFilter.veilederId eq veilederId)
                }
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

    suspend fun hentAllLagredeFilter() = dbQuery {
        (Filter innerJoin MineLagredeFilter).slice(
            Filter.filterId,
            Filter.filterNavn,
            Filter.valgteFilter,
            Filter.opprettetDato,
            MineLagredeFilter.veilederId,
            MineLagredeFilter.sortOrder,
            Filter.filterCleanup
        ).selectAll()
            .mapNotNull { tilMineLagredeFilterModel(it) }
    }

    private fun tilFilterModel(row: ResultRow): FilterModel =
        MineLagredeFilterModel(
            filterId = row[Filter.filterId],
            filterNavn = row[Filter.filterNavn],
            filterValg = row[Filter.valgteFilter],
            veilederId = row[MineLagredeFilter.veilederId],
            opprettetDato = row[Filter.opprettetDato],
            sortOrder = row[MineLagredeFilter.sortOrder],
            filterCleanup = row[Filter.filterCleanup]
        )

    private fun tilMineLagredeFilterModel(row: ResultRow): MineLagredeFilterModel =
        MineLagredeFilterModel(
            filterId = row[Filter.filterId],
            filterNavn = row[Filter.filterNavn],
            filterValg = row[Filter.valgteFilter],
            veilederId = row[MineLagredeFilter.veilederId],
            opprettetDato = row[Filter.opprettetDato],
            sortOrder = row[MineLagredeFilter.sortOrder],
            filterCleanup = row[Filter.filterCleanup]
        )

    private fun validerNavn(navn: String) {
        require(navn.length < 255) { LagredeFilterFeilmeldinger.NAVN_FOR_LANGT.message }
        require(navn.isNotEmpty()) { LagredeFilterFeilmeldinger.NAVN_TOMT.message }
    }

    private fun validerValg(valg: PortefoljeFilter) {
        require(valg.isNotEmpty()) { LagredeFilterFeilmeldinger.FILTERVALG_TOMT.message }
    }

    private fun validerUnikhet(navn: Boolean, valg: Boolean) {
        require(navn) { LagredeFilterFeilmeldinger.NAVN_EKSISTERER.message }
        require(valg) { LagredeFilterFeilmeldinger.FILTERVALG_EKSISTERER.message }
    }

    suspend fun finnFilterForFilterBruker(veilederId: String, enhetId: String): List<FilterModel> {
        try {
            return dbQuery {
                (Filter innerJoin MineLagredeFilter).slice(
                    Filter.filterId,
                    Filter.filterNavn,
                    Filter.valgteFilter,
                    Filter.opprettetDato,
                    MineLagredeFilter.veilederId,
                    MineLagredeFilter.sortOrder,
                    Filter.filterCleanup
                ).select { (MineLagredeFilter.veilederId.eq(veilederId)) and (MineLagredeFilter.enhetId.eq(enhetId)) }
                    .mapNotNull { tilFilterModel(it) }
            }
        } catch (e: java.lang.Exception) {
            log.error("Hent filter error", e)
            return emptyList()
        }
    }

    suspend fun hentAlleMineFilter(): List<MineLagredeFilterModel> {
        try {
            return dbQuery {
                (Filter innerJoin MineLagredeFilter).slice(
                    Filter.filterId,
                    Filter.filterNavn,
                    Filter.valgteFilter,
                    Filter.opprettetDato,
                    MineLagredeFilter.veilederId,
                    MineLagredeFilter.sortOrder,
                    Filter.filterCleanup
                ).select({ MineLagredeFilter.enhetId.isNull() }).map { tilMineLagredeFilterModel(it) }
            }
        } catch (e: java.lang.Exception) {
            log.error("Hent mine filter error", e)
            return emptyList()
        }
    }

    fun leggTilEnhetIdTilMineFilter(filterId: Int, enhetId: String) {
        try {
            MineLagredeFilter
                .update({ (Filter.filterId eq filterId) }) {
                    it[MineLagredeFilter.enhetId] = enhetId
                }
            log.info("Added enhetId ${enhetId} to MineFilter ${filterId}")
        } catch (e: java.lang.Exception) {
            log.error("Legg til enhet id til mine filter error", e)
        }
    }

    suspend fun copyMineFilterOgLeggTilEnhetIdTilMineFilter(mineFilter: MineLagredeFilterModel, enhetId: String) {
        var key: Int;
        dbQuery {
            key = (Filter.insert {
                it[filterNavn] = mineFilter.filterNavn
                it[valgteFilter] = mineFilter.filterValg
                it[opprettetDato] = LocalDateTime.now()
            } get Filter.filterId)

            MineLagredeFilter.insert {
                it[filterId] = key
                it[veilederId] = mineFilter.veilederId
                it[MineLagredeFilter.enhetId] = enhetId
            }
            log.info("Replicated MineFilter ${mineFilter.filterId} to Enhet ${enhetId}")
        }
    }

    suspend fun leggTilEnhetIdTilMineFilter() {
        val alleMineFilter = hentAlleMineFilter()
        alleMineFilter.stream().forEach { mineFilter ->
            val enheterForVeileder = veilarbveilederClient.hentEnheterForVeileder(mineFilter.veilederId)
            if (enheterForVeileder != null && enheterForVeileder.size > 0) {
                leggTilEnhetIdTilMineFilter(mineFilter.filterId, enheterForVeileder[0].get())
                var i = 1;
                while (i < enheterForVeileder.size) {
                    runBlocking {
                        copyMineFilterOgLeggTilEnhetIdTilMineFilter(mineFilter, enheterForVeileder[i].get())
                    }
                    i++
                }
            }

        }
    }

}
