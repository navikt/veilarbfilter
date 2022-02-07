package no.nav.veilarbfilter.util;


import static no.nav.veilarbfilter.util.TestUtil.testMigrate;

public class SingletonPostgresContainer {

    private static PostgresContainer postgresContainer;

    public static PostgresContainer init() {
        if (postgresContainer == null) {
            postgresContainer = new PostgresContainer();
            testMigrate(postgresContainer.createDataSource());
            setupShutdownHook();
        }

        return postgresContainer;
    }

    private static void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (postgresContainer != null) {
                postgresContainer.stopContainer();
            }
        }));
    }
}
