package no.nav.pto.veilarbfilter.util;

import com.zaxxer.hikari.HikariConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil;

import javax.sql.DataSource;

import static no.nav.common.utils.EnvironmentUtils.isProduction;

@Slf4j
public class DbUtils {
    private enum DbRole {
        ADMIN,
        READONLY,
    }

    public static DataSource createDataSource(String dbUrl, boolean admin) {
        HikariConfig config = createDataSourceConfig(dbUrl);
        if (admin) {
            return createVaultRefreshDataSource(config, DbRole.ADMIN);
        }
        return createVaultRefreshDataSource(config, DbRole.READONLY);
    }

    public static HikariConfig createDataSourceConfig(String dbUrl) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        return config;
    }

    public static String getSqlAdminRole() {
        return "veilarbfilter-admin";
    }

    public static String getSqlReadOnlyRole() {
        return "veilarbfilter-user";
    }

    @SneakyThrows
    private static DataSource createVaultRefreshDataSource(HikariConfig config, DbRole role) {
        if (role.equals(DbRole.READONLY)) {
            return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, getMountPath(), getSqlReadOnlyRole());
        }
        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, getMountPath(), getSqlAdminRole());
    }

    private static String getMountPath() {
        boolean isProd = isProduction().orElse(false);
        return "postgresql/" + (isProd ? "prod-fss" : "preprod-fss");
    }
}
