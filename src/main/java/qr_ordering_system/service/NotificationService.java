package qr_ordering_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import qr_ordering_system.model.Notification;
import qr_ordering_system.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification create(String message, String type) {

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setType(type);

        return notificationRepository.save(notification);
    }
}