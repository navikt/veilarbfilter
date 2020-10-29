package no.nav.pto.veilarbfilter.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
open class FilterModel(
        val filterId: Int,
        var filterNavn: String,
        var filterValg: PortefoljeFilter,
        val opprettetDato: LocalDateTime?,
        val filterCleanup: Int = 0
)

class MineLagredeFilterModel(
        filterId: Int,
        filterNavn: String,
        filterValg: PortefoljeFilter,
        opprettetDato: LocalDateTime?,
        filterCleanup: Int,
        val veilederId: String,
        val sortOrder: Int,
        val enhetId: String,
        val note: String,
        val aktiv: Boolean
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato, filterCleanup);

class VeilederGruppeFilterModel(
        filterId: Int,
        filterNavn: String,
        filterValg: PortefoljeFilter,
        opprettetDato: LocalDateTime?,
        filterCleanup: Int,
        val enhetId: String
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato, filterCleanup)

data class NyttFilterModel(val filterNavn: String, val filterValg: PortefoljeFilter)

data class SortOrder(val filterId: Int, val sortOrder: Int)
