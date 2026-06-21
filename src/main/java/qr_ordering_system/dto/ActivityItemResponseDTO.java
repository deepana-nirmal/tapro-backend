package qr_ordering_system.dto;

import java.time.LocalDateTime;

public record ActivityItemResponseDTO(
        String id,
        String title,
        String description,
        LocalDateTime timestamp
) {
}
