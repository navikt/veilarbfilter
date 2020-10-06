package no.nav.pto.veilarbfilter.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
open class FilterModel(
        val filterId: Int,
        var filterNavn: String,
        var filterValg: PortefoljeFilter,
        val opprettetDato: LocalDateTime?,
        val cleanedUp: Int
)

class EnhetensLagredeFilterModel(
        filterId: Int,
        filterNavn: String,
        filterValg: PortefoljeFilter,
        opprettetDato: LocalDateTime?,
        cleanedUp: Int,
        val enhetId: String,
        val sortOrder: Int
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato, cleanedUp);

class MineLagredeFilterModel(
        filterId: Int,
        filterNavn: String,
        filterValg: PortefoljeFilter,
        opprettetDato: LocalDateTime?,
        cleanedUp: Int,
        val veilederId: String,
        val sortOrder: Int
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato, cleanedUp);

class VeilederGruppeFilterModel(
        filterId: Int,
        filterNavn: String,
        filterValg: PortefoljeFilter,
        opprettetDato: LocalDateTime?,
        cleanedUp: Int,
        val enhetId: String
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato, cleanedUp)

data class NyttFilterModel(val filterNavn: String, val filterValg: PortefoljeFilter)

data class SortOrder(val filterId: Int, val sortOrder: Int)
