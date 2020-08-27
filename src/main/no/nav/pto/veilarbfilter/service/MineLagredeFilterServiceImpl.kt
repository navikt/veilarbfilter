package no.nav.pto.veilarbfilter.service

import no.nav.pto.veilarbfilter.config.dbQuery
import no.nav.pto.veilarbfilter.db.Filter
import no.nav.pto.veilarbfilter.db.MineLagredeFilter
import no.nav.pto.veilarbfilter.model.FilterModel
import no.nav.pto.veilarbfilter.model.MineLagredeFilterModel
import no.nav.pto.veilarbfilter.model.NyttFilterModel
import no.nav.pto.veilarbfilter.model.PortefoljeFilter
import org.jetbrains.exposed.sql.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class MineLagredeFilterServiceImpl() : FilterService {
    private val log = LoggerFactory.getLogger("Exceptionhandler")

    override suspend fun hentFilter(filterId: Int): FilterModel? {
        try {
            return dbQuery {

                (Filter innerJoin MineLagredeFilter).slice(
                        Filter.filterId,
                        Filter.filterNavn,
                        Filter.valgteFilter,
                        Filter.opprettetDato,
                        MineLagredeFilter.veilederId
                ).select { (Filter.filterId.eq(filterId)) }
                        .mapNotNull { tilFilterModel(it) }
                        .singleOrNull()
            }
        } catch (e: Exception) {
            log.error("Hent filter error", e)
            return null
        }
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

    override suspend fun lagreFilter(
            veilederId: String,
            nyttFilter: NyttFilterModel
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
                        (MineLagredeFilter.veilederId eq veilederId)
            }.count() > 0
        }

        dbQuery {
            erUgyldigFiltervalg = (Filter innerJoin MineLagredeFilter).slice(
                    Filter.valgteFilter,
                    Filter.filterId,
                    MineLagredeFilter.veilederId
            ).select {
                (Filter.valgteFilter eq nyttFilter.filterValg) and
                        (MineLagredeFilter.veilederId eq veilederId)
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
            }

        }
        return hentFilter(key)
    }

    override suspend fun oppdaterFilter(
            veilederId: String,
            filter: FilterModel
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
                        (Filter.filterId neq filter.filterId)
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
                        (Filter.filterId neq filter.filterId)
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
                MineLagredeFilter.veilederId
        ).selectAll()
                .mapNotNull { tilMineLagredeFilterModel(it) }
    }

    private fun tilFilterModel(row: ResultRow): FilterModel =
            MineLagredeFilterModel(
                    filterId = row[Filter.filterId],
                    filterNavn = row[Filter.filterNavn],
                    filterValg = row[Filter.valgteFilter],
                    veilederId = row[MineLagredeFilter.veilederId],
                    opprettetDato = row[Filter.opprettetDato]
            )

    private fun tilMineLagredeFilterModel(row: ResultRow): MineLagredeFilterModel =
            MineLagredeFilterModel(
                    filterId = row[Filter.filterId],
                    filterNavn = row[Filter.filterNavn],
                    filterValg = row[Filter.valgteFilter],
                    veilederId = row[MineLagredeFilter.veilederId],
                    opprettetDato = row[Filter.opprettetDato]
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

    override suspend fun finnFilterForFilterBruker(veilederId: String) = dbQuery {
        (Filter innerJoin MineLagredeFilter).slice(
                Filter.filterId,
                Filter.filterNavn,
                Filter.valgteFilter,
                Filter.opprettetDato,
                MineLagredeFilter.veilederId
        ).select { (MineLagredeFilter.veilederId.eq(veilederId)) }
                .mapNotNull { tilFilterModel(it) }
    }
}
