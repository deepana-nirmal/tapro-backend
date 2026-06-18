package qr_ordering_system.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import qr_ordering_system.model.Invitation;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByToken(String token);

    boolean existsByEmailAndUsedFalse(String email);

    boolean existsByEmailAndUsedFalseAndExpiresAtAfter(String email, LocalDateTime expiresAt);
}
