package no.nav.pto.veilarbfilter.abac

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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

@JsonIgnoreProperties("Status")
data class Response (
    @JsonProperty("Decision") val decision: Decision?,
    @JsonProperty("AssociatedAdvice") val associatedAdvice: Advice?
)

data class Advice (
    @JsonProperty("Id") val id: String?,
    @JsonProperty("AttributeAssignment") val attributeAssignment: List<AttributeAssignment>?
)


data class AttributeAssignment (
    @JsonProperty("AttributeId") val attributeId: String?,
    @JsonProperty("Value") val value: Any?,
    @JsonProperty("Category") val category: String?,
    @JsonProperty("DataType") val dataType: String?
)