package no.nav.pto.veilarbfilter.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.datetime
import java.time.LocalDateTime


object EnhetFilter : Table() {
    val filterId = integer("filter_id").primaryKey().autoIncrement()
    val enhet = varchar("enhet_id", 32)
    val filterNavn = varchar("filter_navn", 255)
    val valgteFilter = jsonb("valgte_filter", EnhetPortefoljeFilter::class.java)
    val opprettetDato = datetime("opprettet")
}

data class FilterModel (
    val filterId: Int,
    val filterNavn : String,
    val filterValg: EnhetPortefoljeFilter
)

data class EnhetFilterModel (
    val filterId: Int,
    val enhetId: String,
    val filterNavn: String,
    val filterValg: EnhetPortefoljeFilter,
    val opprettetDato: LocalDateTime
)

data class NyttFilterModel (val filterNavn : String, val filterValg: EnhetPortefoljeFilter)
