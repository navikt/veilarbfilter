package no.nav.pto.veilarbfilter.config;

import no.nav.pto.veilarbfilter.AbstractTest;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

@Configuration
@ActiveProfiles({"test"})
public class DbConfigTest implements DatabaseConfig {
    @Bean
    @Override
    public DataSource dataSource() {
        PostgreSQLContainer<?> postgreDBContainer = AbstractTest.postgreDBContainer;
        postgreDBContainer.start();
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgreDBContainer.getJdbcUrl());
        dataSource.setUser(postgreDBContainer.getUsername());
        dataSource.setPassword(postgreDBContainer.getPassword());

        Flyway.configure()
                .encoding("UTF-8")
                .dataSource(dataSource)
                .locations("db")
                .baselineOnMigrate(true)
                .load()
                .migrate();
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
