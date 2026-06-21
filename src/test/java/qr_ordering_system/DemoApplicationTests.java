package qr_ordering_system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import qr_ordering_system.service.EmailService;
import qr_ordering_system.service.NotificationService;

@SpringBootTest
class DemoApplicationTests {

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    void contextLoads() {
    }
}
