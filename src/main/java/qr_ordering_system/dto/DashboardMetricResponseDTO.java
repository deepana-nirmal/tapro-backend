package qr_ordering_system.dto;

public record DashboardMetricResponseDTO(
        String label,
        long value,
        String helper,
        String tone
) {
}
