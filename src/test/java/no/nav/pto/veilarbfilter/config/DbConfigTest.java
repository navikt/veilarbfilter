package no.nav.pto.veilarbfilter.config;

import no.nav.pto.veilarbfilter.AbstractTest;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

@TestConfiguration
@ActiveProfiles({"Test"})

public class DbConfigTest implements DatabaseConfig {

    @Bean
    @Override
    public DataSource dataSource() {
        PostgreSQLContainer<?> postgreDBContainer = AbstractTest.postgreDBContainer;
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgreDBContainer.getJdbcUrl());
        dataSource.setUser(postgreDBContainer.getUsername());
        dataSource.setPassword(postgreDBContainer.getPassword());

        return dataSource;
    }

    @Bean
    @Override
    public JdbcTemplate db(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Override
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
