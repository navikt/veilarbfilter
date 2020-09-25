package no.nav.pto.veilarbfilter.client.dto;

import lombok.Value;

import java.util.List;

@Value
public class VeiledereResponse {
    PortefoljeEnhet enhet;
    public List<Veileder> veilederListe;
}
