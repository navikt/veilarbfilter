package no.nav.pto.veilarbfilter.util;

import java.util.List;
import java.util.stream.Collectors;

public final class PostgresqlUtils {
    private PostgresqlUtils() {
        throw new UnsupportedOperationException();
    }

    public static String mapTilPostgresqlArray(List<String> elementer) {
        return elementer.stream().collect(Collectors.joining(",", "{", "}"));
    }
}
