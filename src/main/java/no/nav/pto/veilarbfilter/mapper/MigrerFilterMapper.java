package no.nav.pto.veilarbfilter.mapper;

import no.nav.pto.veilarbfilter.domene.value.ArenaHovedmal;
import no.nav.pto.veilarbfilter.domene.value.ArenaInnsatsgruppe;
import no.nav.pto.veilarbfilter.domene.value.Hovedmal;
import no.nav.pto.veilarbfilter.domene.value.Innsatsgruppe;

import java.util.List;

public class MigrerFilterMapper {

    public static List<Innsatsgruppe> mapTilInnsatsgruppeGjeldendeVedtak14a(ArenaInnsatsgruppe arenaInnsatsgruppe) {
        return switch (arenaInnsatsgruppe) {
            case BATT -> List.of(Innsatsgruppe.SPESIELT_TILPASSET_INNSATS);
            case BFORM -> List.of(Innsatsgruppe.SITUASJONSBESTEMT_INNSATS);
            case IKVAL -> List.of(Innsatsgruppe.STANDARD_INNSATS);
            case VARIG -> List.of(Innsatsgruppe.VARIG_TILPASSET_INNSATS, Innsatsgruppe.GRADERT_VARIG_TILPASSET_INNSATS);
        };
    }

    public static Hovedmal mapTilHovedmalGjeldendeVedtak14a(ArenaHovedmal arenaHovedmal) {
        return switch (arenaHovedmal) {
            case ArenaHovedmal.BEHOLDEA -> Hovedmal.BEHOLDE_ARBEID;
            case ArenaHovedmal.SKAFFEA -> Hovedmal.SKAFFE_ARBEID;
            case ArenaHovedmal.OKEDELT -> Hovedmal.OKE_DELTAKELSE;
        };
    }
}
