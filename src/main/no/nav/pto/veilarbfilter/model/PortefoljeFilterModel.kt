package no.nav.pto.veilarbfilter.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class PortefoljeFilter(
    val aktiviteter: Aktiviteter? = null,
    val alder: List<String> = emptyList(),
    val ferdigfilterListe: List<String> = emptyList(),
    val fodselsdagIMnd: List<String> = emptyList(),
    val formidlingsgruppe: List<String> = emptyList(),
    val hovedmal: List<String> = emptyList(),
    val innsatsgruppe: List<String> = emptyList(),
    val kjonn: String? = "",
    val manuellBrukerStatus: List<String> = emptyList(),
    val navnEllerFnrQuery: String = "",
    val rettighetsgruppe: List<String> = emptyList(),
    val servicegruppe: List<String> = emptyList(),
    val tiltakstyper: List<String> = emptyList(),
    val veilederNavnQuery: String = "",
    val veiledere: List<String> = emptyList(),
    val ytelse: String? = "",
    val registreringstype: List<String>? = emptyList(),
    val cvJobbprofil: String? = "",
    val arbeidslisteKategori: List<KategoriModell>? = emptyList(),
    val utdanning: List<String> = emptyList(),
    val utdanningGodkjent: List<String> = emptyList(),
    val utdanningBestatt: List<String> = emptyList(),
    val sisteEndringKategori: List<String> = emptyList(),
    var ulesteEndringer: String? = "",
    var aktiviteterForenklet: List<String> = emptyList()
) {

    @JsonIgnore
    fun isNotEmpty(): Boolean {
        return listOf(
            kjonn,
            navnEllerFnrQuery,
            veilederNavnQuery,
            ytelse,
            cvJobbprofil
        ).any { it != null && it.isNotEmpty() }
                || listOf(
            alder,
            ferdigfilterListe,
            fodselsdagIMnd,
            formidlingsgruppe,
            hovedmal,
            innsatsgruppe,
            manuellBrukerStatus,
            rettighetsgruppe,
            servicegruppe,
            tiltakstyper,
            veiledere,
            registreringstype,
            utdanning,
            utdanningGodkjent,
            utdanningBestatt
        ).any { it != null && it.isNotEmpty() }
                || aktiviteter != null
    }
}

data class Aktiviteter(
    @get:JsonProperty("BEHANDLING") val BEHANDLING: String? = "NA",
    @get:JsonProperty("EGEN") val EGEN: String? = "NA",
    @get:JsonProperty("GRUPPEAKTIVITET") val GRUPPEAKTIVITET: String? = "NA",
    @get:JsonProperty("IJOBB") val IJOBB: String? = "NA",
    @get:JsonProperty("MOTE") val MOTE: String? = "NA",
    @get:JsonProperty("SOKEAVTALE") val SOKEAVTALE: String? = "NA",
    @get:JsonProperty("STILLING") val STILLING: String? = "NA",
    @get:JsonProperty("TILTAK") val TILTAK: String? = "NA",
    @get:JsonProperty("UTDANNINGAKTIVITET") val UTDANNINGAKTIVITET: String? = "NA"
)

enum class KategoriModell {
    @JsonProperty("BLA")
    BLA,

    @JsonProperty("LILLA")
    LILLA,

    @JsonProperty("GRONN")
    GRONN,

    @JsonProperty("GUL")
    GUL
}

