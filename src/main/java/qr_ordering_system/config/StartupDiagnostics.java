package qr_ordering_system.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class StartupDiagnostics {

    private static final Logger logger = LoggerFactory.getLogger(StartupDiagnostics.class);

    @Bean
    ApplicationRunner startupDiagnosticsRunner(Environment environment) {
        return args -> {
            String[] activeProfiles = environment.getActiveProfiles();
            boolean hasDbUrlEnv = environment.containsProperty("DB_URL");
            boolean hasDatasourceUrl = environment.getProperty("spring.datasource.url") != null;

            logger.info("Active profiles: {}", Arrays.toString(activeProfiles));
            logger.info("DB_URL present: {}", hasDbUrlEnv);
            logger.info("spring.datasource.url resolved: {}", hasDatasourceUrl);
        };
    }
}
