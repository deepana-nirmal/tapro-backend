package qr_ordering_system.service;

import qr_ordering_system.dto.AuthResponse;
import qr_ordering_system.dto.RegisterRequestDTO;

public interface AuthService {

    String register(RegisterRequestDTO request);

    AuthResponse login(String email, String password);
}