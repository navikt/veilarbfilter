package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Aktiviteter {
    @JsonProperty("BEHANDLING")
    private final String BEHANDLING = "NA";
    @JsonProperty("EGEN")
    private final String EGEN = "NA";
    @JsonProperty("GRUPPEAKTIVITET")
    private final String GRUPPEAKTIVITET = "NA";
    @JsonProperty("IJOBB")
    private final String IJOBB = "NA";
    @JsonProperty("MOTE")
    private final String MOTE = "NA";
    @JsonProperty("SOKEAVTALE")
    private final String SOKEAVTALE = "NA";
    @JsonProperty("STILLING")
    private final String STILLING = "NA";
    @JsonProperty("TILTAK")
    private final String TILTAK = "NA";
    @JsonProperty("UTDANNINGAKTIVITET")
    private final String UTDANNINGAKTIVITET = "NA";
}
