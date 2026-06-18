package qr_ordering_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.exception.BadRequestException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.dev-mode:false}")
    private boolean devMode;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (RuntimeException ex) {
            if (devMode) {
                log.warn("SMTP delivery failed. DEV mode is enabled, so the email is logged instead. to={}, subject={}\n{}",
                        to, subject, body, ex);
                return;
            }

            throw new BadRequestException("Unable to send email. Check SMTP configuration.");
        }
    }
}
