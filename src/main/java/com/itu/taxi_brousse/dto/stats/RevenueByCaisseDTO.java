package com.itu.taxi_brousse.dto.stats;

import lombok.*;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueByCaisseDTO {
    private String caisseName;
    private Map<Integer, Double> revenueByPeriod; // Month or Year -> Revenue
}