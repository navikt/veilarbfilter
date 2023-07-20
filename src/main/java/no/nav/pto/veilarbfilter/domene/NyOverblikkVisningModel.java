package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Data
@Setter
@AllArgsConstructor
@JsonDeserialize
@NoArgsConstructor
public class NyOverblikkVisningModel {
    List<String> overblikkVisning;
}
