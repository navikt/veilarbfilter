package no.nav.pto.veilarbfilter.config;

import io.getunleash.DefaultUnleash;

public class FeatureToggle {

    private FeatureToggle() {}

    public static final String EKSEMPELTOGGLE = "veilarbfilter.eksempeltogglenamn";

    public static final String BRUK_NYTT_ARENA_AAP_FILTER = "veilarbportefoljeflatefs.ytelser-bruk-nytt-arena-aap-filter";

    public static boolean brukNyttAapArenaFilter(DefaultUnleash defaultUnleash) {
        return defaultUnleash.isEnabled(FeatureToggle.BRUK_NYTT_ARENA_AAP_FILTER);
    }
}
