package no.nav.pto.veilarbfilter.domene.value;

public enum ArenaHovedmal {
    SKAFFEA,
    BEHOLDEA,
    OKEDELT;

    public static Hovedmal mapTilHovedmalGjeldendeVedtak14a(ArenaHovedmal arenaHovedmal) {
        return switch (arenaHovedmal) {
            case ArenaHovedmal.BEHOLDEA -> Hovedmal.BEHOLDE_ARBEID;
            case ArenaHovedmal.SKAFFEA -> Hovedmal.SKAFFE_ARBEID;
            case ArenaHovedmal.OKEDELT -> Hovedmal.OKE_DELTAKELSE;
        };
    }
}
