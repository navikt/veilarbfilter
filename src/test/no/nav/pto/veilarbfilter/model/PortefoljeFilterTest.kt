package no.nav.pto.veilarbfilter.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert
import org.junit.Test

class PortefoljeFilterTest {

    @Test
    fun testDeserialization() {
        var jsonString =
            "{\"aktiviteter\":{\"BEHANDLING\":\"NA\",\"EGEN\":\"NA\",\"GRUPPEAKTIVITET\":\"NA\",\"IJOBB\":\"NA\",\"MOTE\":\"NA\",\"SOKEAVTALE\":\"NA\",\"STILLING\":\"NA\",\"TILTAK\":\"NA\",\"UTDANNINGAKTIVITET\":\"NA\"},\"alder\":[],\"ferdigfilterListe\":[\"MIN_ARBEIDSLISTE\",\"NYE_BRUKERE_FOR_VEILEDER\",\"PERMITTERTE_ETTER_NIENDE_MARS\"],\"fodselsdagIMnd\":[],\"formidlingsgruppe\":[],\"hovedmal\":[],\"innsatsgruppe\":[],\"kjonn\":null,\"manuellBrukerStatus\":[],\"navnEllerFnrQuery\":\"\",\"rettighetsgruppe\":[],\"servicegruppe\":[],\"tiltakstyper\":[],\"veilederNavnQuery\":\"\",\"veiledere\":[],\"ytelse\":\"DAGPENGER\",\"registreringstype\":[],\"cvJobbprofil\":null,\"arbeidslisteKategori\":[\"BLA\",\"LILLA\"]}"
        var objectMapper = jacksonObjectMapper()
        var filterModel: PortefoljeFilter = objectMapper.readValue(jsonString);

        Assert.assertTrue(filterModel.arbeidslisteKategori != null)
        Assert.assertTrue(filterModel.arbeidslisteKategori?.size == 2)
    }
}