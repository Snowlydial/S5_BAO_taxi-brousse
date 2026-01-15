package com.itu.taxi_brousse.dto.stats;

import lombok.*;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardStatsDTO {
    private String periodType; // "monthly" or "yearly"
    private Integer year; // For monthly
    private Integer yearMin; // For yearly
    private Integer yearMax; // For yearly
    
    private Map<Integer, Double> globalRevenue;
    private Map<String, Long> genderUsage;
    private Map<String, Long> ageGroupUsage;
    private java.util.List<RevenueByCaisseDTO> revenueByCaisse;
    private java.util.List<VoyageStatsDTO> topVoyages;
    private java.util.List<ClientRevenueDTO> topClients;
}