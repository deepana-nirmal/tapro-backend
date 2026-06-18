package qr_ordering_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QrOrderingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(QrOrderingSystemApplication.class, args);
    }
}
