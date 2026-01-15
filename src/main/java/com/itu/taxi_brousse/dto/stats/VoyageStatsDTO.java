package com.itu.taxi_brousse.dto.stats;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoyageStatsDTO {
    private String voyageRoute;
    private Long totalReservations;
    
    public void addReservations(Long count) {
        this.totalReservations += count;
    }
}