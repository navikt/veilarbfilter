package no.nav.pto.veilarbfilter.domene


data class NyttFilterModel(
    val filterNavn: String,
    val filterValg: PortefoljeFilter,
    val aktiveFilterValg: String,
)
