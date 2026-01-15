package com.itu.taxi_brousse.dto.stats;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientRevenueDTO {
    private String clientName;
    private Double totalRevenue;
}