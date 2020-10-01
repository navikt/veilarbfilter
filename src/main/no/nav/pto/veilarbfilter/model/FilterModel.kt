package no.nav.pto.veilarbfilter.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime
import kotlin.reflect.typeOf

@JsonIgnoreProperties(ignoreUnknown = true)
open class FilterModel(
        val filterId: Int,
        var filterNavn: String,
        var filterValg: PortefoljeFilter,
        val opprettetDato: LocalDateTime?
){
        override fun equals(other: Any?): Boolean {
                if(other != null && other is FilterModel){
                        return filterId == other.filterId && filterNavn == other.filterNavn && filterValg == other.filterValg && opprettetDato == other.opprettetDato
                }
                return false
        }
}

class EnhetensLagredeFilterModel(
        filterId: Int,
        filterNavn: String,
        filterValg: PortefoljeFilter,
        opprettetDato: LocalDateTime?,
        val enhetId: String,
        val sortOrder: Int
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato);

class MineLagredeFilterModel(
        filterId: Int,
        filterNavn: String,
        filterValg: PortefoljeFilter,
        opprettetDato: LocalDateTime?,
        val veilederId: String,
        val sortOrder: Int
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato);

class VeilederGruppeFilterModel(
        filterId: Int,
        filterNavn: String,
        filterValg: PortefoljeFilter,
        opprettetDato: LocalDateTime?,
        val enhetId: String
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato)

data class NyttFilterModel(val filterNavn: String, val filterValg: PortefoljeFilter)

data class SortOrder(val filterId: Int, val sortOrder: Int)
