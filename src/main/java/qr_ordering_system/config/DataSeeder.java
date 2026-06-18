package qr_ordering_system.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            UserRepository userRepo,
            RestaurantRepository restaurantRepo,
            PasswordEncoder passwordEncoder) {

        return args -> {
            Restaurant restaurant = ensureRestaurant(restaurantRepo);

            // 👑 SUPER ADMIN (YOU)
            createUserIfNotExists(
                    userRepo,
                    passwordEncoder,
                    "System Admin",
                    "admin@tapro.com",
                    "admin123",
                    Role.SUPER_ADMIN,
                    restaurant
            );

            // Restaurant Owner
            createUserIfNotExists(
                    userRepo,
                    passwordEncoder,
                    "Owner",
                    "owner@gmail.com",
                    "owner123",
                    Role.OWNER,
                    restaurant
            );

            // Kitchen Staff
            createUserIfNotExists(
                    userRepo,
                    passwordEncoder,
                    "Kitchen",
                    "kitchen@gmail.com",
                    "kitchen123",
                    Role.KITCHEN,
                    restaurant
            );

            // Staff
            createUserIfNotExists(
                    userRepo,
                    passwordEncoder,
                    "Staff",
                    "staff@gmail.com",
                    "staff123",
                    Role.STAFF,
                    restaurant
            );

            // Cashier
            createUserIfNotExists(
                    userRepo,
                    passwordEncoder,
                    "Cashier",
                    "cashier@gmail.com",
                    "cashier123",
                    Role.CASHIER,
                    restaurant
            );

            System.out.println("✅ Data seeding completed");
        };
    }

    private Restaurant ensureRestaurant(RestaurantRepository restaurantRepo) {
        return restaurantRepo.findAll().stream().findFirst().orElseGet(() -> {
            Restaurant restaurant = new Restaurant();
            restaurant.setName("Harbor Table");
            restaurant.setAddress("120 Ocean Ave");
            restaurant.setPhone("+1 555 410 9001");
            restaurant.setEmail("hello@harbortable.com");
            restaurant.setDescription("Coastal dining and QR ordering.");
            restaurant.setOpeningHours("Mon-Sun: 11:00 - 23:00");
            restaurant.setServiceChargePercentage(10.0);
            restaurant.setTaxPercentage(8.0);
            restaurant.setCurrency("USD");
            restaurant.setThemeColor("#10b981");
            return restaurantRepo.save(restaurant);
        });
    }

    private void createUserIfNotExists(
            UserRepository userRepo,
            PasswordEncoder passwordEncoder,
            String name,
            String email,
            String password,
            Role role,
            Restaurant restaurant) {

        if (userRepo.findByEmail(email).isEmpty()) {

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setRestaurant(restaurant);

            userRepo.save(user);

            System.out.println("✅ Created user: " + role + " → " + email);
        }
    }
}
