package no.nav.pto.veilarbfilter.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

public interface DatabaseConfig {

    DataSource dataSource();

    JdbcTemplate db(DataSource dataSource);

    NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource);

    PlatformTransactionManager transactionManager(DataSource dataSource);
}
