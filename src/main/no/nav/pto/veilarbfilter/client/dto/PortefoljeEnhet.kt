package no.nav.pto.veilarbfilter.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.common.types.identer.EnhetId

@JsonIgnoreProperties(ignoreUnknown = true)
class PortefoljeEnhet(
    var enhetId: EnhetId,
    var navn: String
)