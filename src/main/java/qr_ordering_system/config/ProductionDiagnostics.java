package qr_ordering_system.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("prod")
public class ProductionDiagnostics {

    private static final Logger logger = LoggerFactory.getLogger(ProductionDiagnostics.class);

    @Bean
    ApplicationRunner productionDiagnosticsRunner(Environment environment, DataSource dataSource) {
        return args -> {
            logEnvironmentSummary(environment);
            logSchemaSummary(dataSource);
        };
    }

    private void logEnvironmentSummary(Environment environment) {
        logger.info("Production diagnostics active profiles: {}", Arrays.toString(environment.getActiveProfiles()));
        logger.info("Production diagnostics SPRING_PROFILES_ACTIVE present: {}", hasText(environment.getProperty("SPRING_PROFILES_ACTIVE")));
        logger.info("Production diagnostics DB_URL present: {}", hasText(environment.getProperty("DB_URL")));
        logger.info("Production diagnostics DB_USERNAME present: {}", hasText(environment.getProperty("DB_USERNAME")));
        logger.info("Production diagnostics DB_PASSWORD present: {}", hasText(environment.getProperty("DB_PASSWORD")));
        logger.info("Production diagnostics JWT_SECRET present: {}", hasText(environment.getProperty("JWT_SECRET")));
        logger.info("Production diagnostics FRONTEND_URLS present: {}", hasText(environment.getProperty("FRONTEND_URLS")));
        logger.info("Production diagnostics resolved app.frontend-urls: {}", environment.getProperty("app.frontend-urls"));
        logger.info("Production diagnostics resolved spring.datasource.url present: {}", hasText(environment.getProperty("spring.datasource.url")));
    }

    private void logSchemaSummary(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String schema = connection.getSchema();
            String resolvedSchema = hasText(schema) ? schema : "public";

            logger.info("Production diagnostics database product: {}", metaData.getDatabaseProductName());
            logger.info("Production diagnostics database schema: {}", resolvedSchema);

            Map<String, List<String>> requiredColumns = new LinkedHashMap<>();
            requiredColumns.put("restaurant", List.of(
                    "logo_url",
                    "description",
                    "opening_hours",
                    "service_charge_percentage",
                    "tax_percentage",
                    "currency",
                    "theme_color",
                    "status"
            ));
            requiredColumns.put("app_user", List.of("enabled", "restaurant_id", "role"));
            requiredColumns.put("orders", List.of("tenant_id", "table_number", "status", "created_at"));

            for (Map.Entry<String, List<String>> entry : requiredColumns.entrySet()) {
                String tableName = entry.getKey();
                if (!tableExists(metaData, resolvedSchema, tableName)) {
                    logger.error("Production diagnostics missing table: {}", tableName);
                    continue;
                }

                for (String columnName : entry.getValue()) {
                    if (!columnExists(metaData, resolvedSchema, tableName, columnName)) {
                        logger.error("Production diagnostics missing column: {}.{}", tableName, columnName);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Production diagnostics failed while inspecting database schema", ex);
        }
    }

    private boolean tableExists(DatabaseMetaData metaData, String schema, String tableName) throws SQLException {
        try (ResultSet tables = metaData.getTables(null, schema, tableName, new String[]{"TABLE"})) {
            if (tables.next()) {
                return true;
            }
        }

        try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return tables.next();
        }
    }

    private boolean columnExists(DatabaseMetaData metaData, String schema, String tableName, String columnName) throws SQLException {
        try (ResultSet columns = metaData.getColumns(null, schema, tableName, columnName)) {
            if (columns.next()) {
                return true;
            }
        }

        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            return columns.next();
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
