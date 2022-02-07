package no.nav.veilarbfilter.util;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class TestUtil {

    public static void testMigrate(DataSource dataSource) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("db/")
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }
}
