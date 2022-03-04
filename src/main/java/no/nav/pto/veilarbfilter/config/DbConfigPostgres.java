package no.nav.pto.veilarbfilter.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static no.nav.pto.veilarbfilter.util.DbUtils.createDataSource;
import static no.nav.pto.veilarbfilter.util.DbUtils.getSqlAdminRole;


@Slf4j
@RequiredArgsConstructor
@EnableTransactionManagement
public class DbConfigPostgres implements DatabaseConfig {
    private final EnvironmentProperties environmentProperties;

    @Bean
    @Override
    public DataSource dataSource() {
        return createDataSource(environmentProperties.getDbUrl(), true);
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

    @Bean
    @Override
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @PostConstruct
    @SneakyThrows
    public void migrateDb() {
        log.info("Starting database migration...");
        DataSource dataSource = createDataSource(environmentProperties.getDbUrl(), true);

        Flyway.configure()
                .encoding("UTF-8")
                .dataSource(dataSource)
                .locations("db/migration")
                .initSql("SET ROLE '" + getSqlAdminRole() + "';")
                .baselineOnMigrate(true)
                .load()
                .migrate();

        dataSource.getConnection().close();
    }
}
