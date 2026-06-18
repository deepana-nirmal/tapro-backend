package qr_ordering_system.service;

import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.RestaurantStatus;
import qr_ordering_system.repository.RestaurantRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {

    private final RestaurantRepository restaurantRepository;

    public SubscriptionService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // every day midnight
    public void checkExpiry() {

        List<Restaurant> restaurants = restaurantRepository.findAll();

        for (Restaurant r : restaurants) {

            if (r.getSubscriptionExpiryDate() != null &&
                r.getSubscriptionExpiryDate().isBefore(LocalDate.now())) {

                r.setStatus(RestaurantStatus.EXPIRED);
                restaurantRepository.save(r);
            }
        }
    }
}