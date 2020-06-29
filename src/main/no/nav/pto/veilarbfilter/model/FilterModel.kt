package no.nav.pto.veilarbfilter.model

import java.time.LocalDateTime

open class FilterModel(
    val filterId: Int,
    val filterNavn: String,
    val filterValg: PortefoljeFilter,
    val opprettetDato: LocalDateTime?
)

class EnhetensLagredeFilterModel(
    filterId: Int,
    filterNavn: String,
    filterValg: PortefoljeFilter,
    opprettetDato: LocalDateTime?,
    val enhetId: String
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato);

class MineLagredeFilterModel(
    filterId: Int,
    filterNavn: String,
    filterValg: PortefoljeFilter,
    opprettetDato: LocalDateTime?,
    val veilederId: String
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato);

class VeilederGruppeFilterModel(
    filterId: Int,
    filterNavn: String,
    filterValg: PortefoljeFilter,
    opprettetDato: LocalDateTime?,
    val enhetId: String
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato)

data class NyttFilterModel(val filterNavn: String, val filterValg: PortefoljeFilter)
