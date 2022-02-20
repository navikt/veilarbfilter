package no.nav.pto.veilarbfilter.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.GenericContainer;

import javax.sql.DataSource;

public class PostgresContainer {

    private  static String DB_IMAGE = "postgres:11.5";
    private  static String DB_USER = "postgres";
    private  static int DB_PORT = 5432;

    private  GenericContainer container;

    public PostgresContainer() {
        container = new GenericContainer(DB_IMAGE).withExposedPorts(DB_PORT);
        container.start(); // This will block until the container is started
    }

    @SneakyThrows
    public String execInContainer(String... cmd) {
        return container.execInContainer(cmd).getStdout();
    }

    public void stopContainer() {
        container.stop();
    }

    public DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getDbContainerUrl());
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);
        config.setUsername(DB_USER);

        return new HikariDataSource(config);
    }

    public JdbcTemplate createJdbcTemplate() {
        return new JdbcTemplate(createDataSource());
    }

    private String getDbContainerUrl() {
        String containerIp = container.getContainerIpAddress();
        String containerPort = container.getFirstMappedPort().toString();
        return String.format("jdbc:postgresql://%s:%s/postgres", containerIp, containerPort);
    }

}
