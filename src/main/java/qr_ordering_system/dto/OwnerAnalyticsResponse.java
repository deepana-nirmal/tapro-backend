package qr_ordering_system.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerAnalyticsResponse {

    private OwnerMetricPeriodValue revenue;
    private OwnerMetricPeriodValue orders;
    private List<OwnerItemSalesResponse> topSellingItems;
    private List<OwnerItemSalesResponse> leastSellingItems;
    private List<OwnerPeakHourResponse> peakOrderingHours;
    private double averageOrderValue;
}
