package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.flywaydb.core.internal.configuration.ListDeserializer;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@JsonDeserialize
@NoArgsConstructor
public class NyOverblikkVisningModel {
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    List<String> overblikkVisning;
}
