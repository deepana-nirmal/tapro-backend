package qr_ordering_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import qr_ordering_system.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}