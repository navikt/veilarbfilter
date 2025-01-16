package no.nav.pto.veilarbfilter.domene.value;

import java.util.List;

public enum ArenaInnsatsgruppe {
    BATT,
    BFORM,
    IKVAL,
    VARIG;

    public static List<Innsatsgruppe> mapTilInnsatsgruppeGjeldendeVedtak14a(ArenaInnsatsgruppe arenaInnsatsgruppe) {
        return switch (arenaInnsatsgruppe) {
            case BATT -> List.of(Innsatsgruppe.SPESIELT_TILPASSET_INNSATS);
            case BFORM -> List.of(Innsatsgruppe.SITUASJONSBESTEMT_INNSATS);
            case IKVAL -> List.of(Innsatsgruppe.STANDARD_INNSATS);
            case VARIG -> List.of(Innsatsgruppe.VARIG_TILPASSET_INNSATS, Innsatsgruppe.GRADERT_VARIG_TILPASSET_INNSATS);
        };
    }
}
