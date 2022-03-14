package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum KategoriModell {
    @JsonProperty("BLA")
    BLA,
    @JsonProperty("LILLA")
    LILLA,
    @JsonProperty("GRONN")
    GRONN,
    @JsonProperty("GUL")
    GUL
}
