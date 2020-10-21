package no.nav.pto.veilarbfilter.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.common.types.identer.NavIdent

@JsonIgnoreProperties(ignoreUnknown = true)
class IdentOgEnhetliste(
    var ident: NavIdent,
    var enhetliste: List<PortefoljeEnhet>
)