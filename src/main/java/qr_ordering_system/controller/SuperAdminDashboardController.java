package qr_ordering_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.ActivityItemResponseDTO;
import qr_ordering_system.dto.DashboardMetricResponseDTO;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.SuperAdminDashboardService;

@RestController
@RequestMapping("/api/super-admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminDashboardController {

    private final SuperAdminDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DashboardMetricResponseDTO>>> getMetrics() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Dashboard metrics retrieved successfully",
                dashboardService.getMetrics()
        ));
    }

    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<List<ActivityItemResponseDTO>>> getActivities() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Dashboard activities retrieved successfully",
                dashboardService.getRecentActivities()
        ));
    }
}
