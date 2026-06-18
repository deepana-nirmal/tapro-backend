package qr_ordering_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerPeakHourResponse {

    private int hour;
    private long orderCount;
}
