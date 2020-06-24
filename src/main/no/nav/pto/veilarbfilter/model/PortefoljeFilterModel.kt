package no.nav.pto.veilarbfilter.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PortefoljeFilter (
    val aktiviteter: Aktiviteter,
    val alder: List<String>,
    val ferdigfilterListe: List<String>,
    val fodselsdagIMnd: List<String>,
    val formidlingsgruppe: List<String>,
    val hovedmal: List<String>,
    val innsatsgruppe: List<String>,
    val kjonn: String?,
    val manuellBrukerStatus: List<String>,
    val navnEllerFnrQuery: String,
    val rettighetsgruppe: List<String>,
    val servicegruppe: List<String>,
    val tiltakstyper: List<String>,
    val veilederNavnQuery: String,
    val veiledere: List<String>,
    val ytelse: String?,
    val registreringstype: List<String>?,
    val cvJobbprofil: String?
    )

data class Aktiviteter (
    @get:JsonProperty("BEHANDLING") val BEHANDLING: String?,
    @get:JsonProperty("EGEN") val EGEN: String?,
    @get:JsonProperty("GRUPPEAKTIVITET") val GRUPPEAKTIVITET: String?,
    @get:JsonProperty("IJOBB") val IJOBB: String?,
    @get:JsonProperty("MOTE") val MOTE: String?,
    @get:JsonProperty("STILLING") val STILLING: String?,
    @get:JsonProperty("UTDANNINGAKTIVITET") val UTDANNINGAKTIVITET: String?
)