package qr_ordering_system;

import qr_ordering_system.config.EarlyStartupDiagnostics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QrOrderingSystemApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(QrOrderingSystemApplication.class);
        application.addListeners(new EarlyStartupDiagnostics());
        application.run(args);
    }
}
