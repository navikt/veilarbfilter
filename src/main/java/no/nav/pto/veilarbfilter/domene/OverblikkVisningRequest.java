package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

public record OverblikkVisningRequest(
        List<String> overblikkVisning
) {
}
