package no.nav.pto.veilarbfilter.model
import java.time.LocalDateTime

open class FilterModel (
    open var filterId: Int,
    open var filterNavn : String,
    open var filterValg: PortefoljeFilter,
    open var opprettetDato: LocalDateTime ?
)

data class EnhetFilterModel(
    override var filterId: Int,
    override var filterNavn : String,
    override var filterValg: PortefoljeFilter,
    override var opprettetDato: LocalDateTime ?,
    var enhetId: String) :  FilterModel(filterId, filterNavn, filterValg, opprettetDato);

data class MineFilterModel(
    override var filterId: Int,
    override var filterNavn : String,
    override var filterValg: PortefoljeFilter,
    override var opprettetDato: LocalDateTime ?,
    var veilederId: String) :  FilterModel(filterId, filterNavn, filterValg, opprettetDato);

data class VeilederGruppeFilterModel(
    override var filterId: Int,
    override var filterNavn : String,
    override var filterValg: PortefoljeFilter,
    override var opprettetDato: LocalDateTime ?,
    var enhetId: String) :  FilterModel(filterId, filterNavn, filterValg, opprettetDato);

data class NyttFilterModel (val filterNavn : String, val filterValg: PortefoljeFilter)
