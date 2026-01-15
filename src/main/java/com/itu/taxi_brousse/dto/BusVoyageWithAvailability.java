package com.itu.taxi_brousse.dto;

import com.itu.taxi_brousse.entity.BusClasse;
import com.itu.taxi_brousse.entity.BusVoyage;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BusVoyageWithAvailability {
    private BusVoyage busVoyage;
    private Double price;
    private Integer availableSeats;
    private Integer capacity;
    private List<Integer> availableSeatNumbers;
    private BusClasse busClasse;
    
    public boolean hasAvailableSeats() {
        return availableSeats != null && availableSeats > 0;
    }
}