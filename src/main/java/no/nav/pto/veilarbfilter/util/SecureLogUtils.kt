package no.nav.pto.veilarbfilter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SecureLogUtils {
    private SecureLogUtils() {
        throw new UnsupportedOperationException();
    }

    public static final Logger secureLog = LoggerFactory.getLogger("SecureLog");
}
