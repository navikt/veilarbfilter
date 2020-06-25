package no.nav.pto.veilarbfilter.model

import java.time.LocalDateTime

open class FilterModel(
    @Transient
    open var filterId: Int,
    @Transient
    open var filterNavn: String,
    @Transient
    open var filterValg: PortefoljeFilter,
    @Transient
    open var opprettetDato: LocalDateTime?
)

data class EnhetensLagredeFilterModel(
    override var filterId: Int,
    override var filterNavn: String,
    override var filterValg: PortefoljeFilter,
    override var opprettetDato: LocalDateTime?,
    var enhetId: String
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato);

data class MineLagredeFilterModel(
    override var filterId: Int,
    override var filterNavn: String,
    override var filterValg: PortefoljeFilter,
    override var opprettetDato: LocalDateTime?,
    var veilederId: String
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato);

data class VeilederGruppeFilterModel(
    override var filterId: Int,
    override var filterNavn: String,
    override var filterValg: PortefoljeFilter,
    override var opprettetDato: LocalDateTime?,
    var enhetId: String
) : FilterModel(filterId, filterNavn, filterValg, opprettetDato);

data class NyttFilterModel(val filterNavn: String, val filterValg: PortefoljeFilter)
