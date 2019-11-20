package no.nav.pto.veilarbfilter.abac

import com.fasterxml.jackson.annotation.JsonProperty

class XacmlRequest {

    private val xacmlAttributes = HashMap<String, Attributes>()

    fun build(): Map<String, Map<String, Attributes>> {
        return mapOf("Request" to xacmlAttributes)
    }

    fun addAttribute(category: String, id: String, value: Any): XacmlRequest {
        xacmlAttributes.getOrPut(category) { Attributes() }.attributes += Attribute(id, value)
        return this
    }
}

data class Attributes (
    @JsonProperty("Attribute") var attributes: List<Attribute> = ArrayList()
)

data class Attribute (
    @JsonProperty("AttributeId") val attributeId: String,
    @JsonProperty("Value") val value: Any
)

enum class Decision {
    Permit,
    Deny,
    NotApplicable,
    Indeterminate
}