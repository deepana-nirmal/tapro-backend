package qr_ordering_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import qr_ordering_system.dto.AuthResponse;
import qr_ordering_system.dto.LoginRequestDTO;
import qr_ordering_system.dto.RegisterRequestDTO;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        String result = authService.register(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User registered successfully",
                        result
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequestDTO request) {

        AuthResponse response =
                authService.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Login successful",
                        response
                )
        );
    }
}