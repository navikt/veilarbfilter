package no.nav.pto.veilarbfilter.abac

import com.fasterxml.jackson.annotation.JsonProperty

enum class Decision {
    Permit,
    Deny,
    NotApplicable,
    Indeterminate
}

data class XacmlResponse (
    @JsonProperty("Response") val response: Response? = null
)

data class Response (
    @JsonProperty("Decision") val decision: Decision? = null,
    @JsonProperty("AssociatedAdvice") val associatedAdvice: Advice? = null
)

data class Advice (
    @JsonProperty("Id") val id: String? = null,
    @JsonProperty("AttributeAssignment") val attributeAssignment: List<Attribute>? = null
)