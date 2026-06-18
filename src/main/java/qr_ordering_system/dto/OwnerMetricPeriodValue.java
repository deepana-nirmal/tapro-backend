package qr_ordering_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerMetricPeriodValue {

    private double today;
    private double week;
    private double month;
}
