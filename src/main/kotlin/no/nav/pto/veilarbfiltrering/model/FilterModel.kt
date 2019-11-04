package no.nav.pto.veilarbfiltrering.model

import org.jetbrains.exposed.sql.Table

object EnhetFilter : Table() {
    val filterId = integer("filter_id").primaryKey().autoIncrement()
    val enhet = varchar("enhet_id", 32)
    val filterNavn = varchar("filter_navn", 255)
    val valgteFilter = text("valgte_filter")
}

data class EnhetFilterModel (
    val filterId: Int,
    val enhetId: String,
    val filterNavn: String,
    val filterValg: String
)


data class FilterModel (
    val filterId: Int,
    val filterNavn : String,
    val filterValg: String
)

data class NyttFilterModel (
    val filterNavn : String,
    val filterValg: String
)