package no.nav.pto.veilarbfilter.db

import no.nav.pto.veilarbfilter.model.PortefoljeFilter
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.datetime

object Filter : Table() {
    val filterId = integer("filter_id").primaryKey().autoIncrement()
    val filterNavn = varchar("filter_navn", 255)
    val valgteFilter = jsonb("valgte_filter", PortefoljeFilter::class.java)
    val opprettetDato = datetime("opprettet")
}

object EnhetFilter : Table() {
    val filterId = integer("enhet_filter_id") references Filter.filterId;
    val enhetId = varchar("enhet_id", 32)
}

object MineFilter : Table() {
    val filterId = integer("mine_filter_id") references Filter.filterId;
    val veilederId = varchar("veileder_id", 32)
}

object VeilederGrupperFilter : Table() {
    val filterId = integer("veiledergruppe_filter_id") references Filter.filterId;
    val enhetId = varchar("enhet_id", 32)
}