package qr_ordering_system.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

public class EarlyStartupDiagnostics implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(EarlyStartupDiagnostics.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        String dbUrl = environment.getProperty("DB_URL");

        logger.info("Early startup active profiles: {}", Arrays.toString(activeProfiles));
        logger.info("Early startup default profiles: {}", Arrays.toString(defaultProfiles));
        logger.info("Early startup DB_URL present: {}", hasText(dbUrl));
        logger.info("Early startup spring.datasource.url resolved: {}", hasText(datasourceUrl));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
