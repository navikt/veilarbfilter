package no.nav.pto.veilarbfilter.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

import static no.nav.common.utils.EnvironmentUtils.isProduction;

@Slf4j
public class DbUtils {
    public static DataSource createDataSource(String dbUrl) {
        HikariConfig hikariConfig = createDataSourceConfig(dbUrl);
        return new HikariDataSource(hikariConfig);
    }

    public static HikariConfig createDataSourceConfig(String dbUrl) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        return config;
    }
}
