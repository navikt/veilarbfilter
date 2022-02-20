package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Aktiviteter {
    @JsonProperty("BEHANDLING")
    private  String BEHANDLING = "NA";
    @JsonProperty("EGEN")
    private  String EGEN = "NA";
    @JsonProperty("GRUPPEAKTIVITET")
    private  String GRUPPEAKTIVITET = "NA";
    @JsonProperty("IJOBB")
    private  String IJOBB = "NA";
    @JsonProperty("MOTE")
    private  String MOTE = "NA";
    @JsonProperty("SOKEAVTALE")
    private  String SOKEAVTALE = "NA";
    @JsonProperty("STILLING")
    private  String STILLING = "NA";
    @JsonProperty("TILTAK")
    private  String TILTAK = "NA";
    @JsonProperty("UTDANNINGAKTIVITET")
    private  String UTDANNINGAKTIVITET = "NA";
}
