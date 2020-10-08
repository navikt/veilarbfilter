package no.nav.pto.veilarbfilter.db

import no.nav.pto.veilarbfilter.model.PortefoljeFilter
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.datetime

object Filter : Table() {
    val filterId = integer("filter_id").primaryKey().autoIncrement()
    val filterNavn = varchar("filter_navn", 255)
    val valgteFilter = jsonb("valgte_filter", PortefoljeFilter::class.java)
    val opprettetDato = datetime("opprettet")
    val filterCleanup = integer("filter_cleanup")
}

object EnhetensLagredeFilter : Table() {
    val filterId = integer("enhet_filter_id") references Filter.filterId;
    val enhetId = varchar("enhet_id", 32)
    val sortOrder = integer("sort_order")
}

object MineLagredeFilter : Table() {
    val filterId = integer("mine_filter_id") references Filter.filterId;
    val veilederId = varchar("veileder_id", 32)
    val sortOrder = integer("sort_order")
}

object VeilederGrupperFilter : Table() {
    val filterId = integer("veiledergruppe_filter_id") references Filter.filterId;
    val enhetId = varchar("enhet_id", 32)
}