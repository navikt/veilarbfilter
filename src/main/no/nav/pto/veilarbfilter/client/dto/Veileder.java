package no.nav.pto.veilarbfilter.client.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Veileder {
    public String ident;
    String navn;
    String fornavn;
    String etternavn;
}