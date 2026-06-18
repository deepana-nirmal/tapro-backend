package qr_ordering_system.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.AuthResponse;
import qr_ordering_system.dto.RegisterRequestDTO;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.security.JwtUtil;
import qr_ordering_system.service.AuthService;
import qr_ordering_system.service.RestaurantAccessGuard;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log =
            LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RestaurantAccessGuard restaurantAccessGuard;

    @Override
    public String register(RegisterRequestDTO request) {

        log.info("Register attempt for email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));

        userRepository.save(user);

        log.info("User registered successfully: {}", request.getEmail());

        return "User registered successfully";
    }

    @Override
    public AuthResponse login(String email, String password) {

        log.info("Login attempt: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new BadRequestException("User account is disabled");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        restaurantAccessGuard.ensureLoginAllowed(user);

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        log.info("Login successful: {}", email);

        return new AuthResponse(
                token,
                user.getRole().name(),
                user.getRestaurant() != null ? user.getRestaurant().getId() : null
        );
    }
}
