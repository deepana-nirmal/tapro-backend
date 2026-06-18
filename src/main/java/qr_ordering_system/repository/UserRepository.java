package qr_ordering_system.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByRestaurant_IdAndRoleIn(Long restaurantId, Collection<Role> roles);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("select u from User u left join fetch u.restaurant")
    List<User> findAllWithRestaurant();
}
