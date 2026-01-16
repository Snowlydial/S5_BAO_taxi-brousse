package com.itu.taxi_brousse.dto;

import com.itu.taxi_brousse.entity.BusClasse;
import com.itu.taxi_brousse.entity.BusVoyage;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BusVoyageWithAvailability {
    private BusVoyage busVoyage;
    private Double price;
    private Integer availableSeats;
    private Integer capacity;
    private List<Integer> availableSeatNumbers;
    private BusClasse busClasse;
    
    // New fields for place types
    private Map<String, Integer> placeTypeCapacities; // e.g., {"Premium": 10, "Standard": 20}
    private Map<String, Integer> placeTypeAvailableSeats; // e.g., {"Premium": 5, "Standard": 15}
    private Map<String, Double> placeTypePrices; // e.g., {"Premium": 25000.0, "Standard": 15000.0}
    
    public boolean hasAvailableSeats() {
        return availableSeats != null && availableSeats > 0;
    }
    
    public List<String> getPlaceTypes() {
        return placeTypeCapacities != null ? 
            new java.util.ArrayList<>(placeTypeCapacities.keySet()) : 
            new java.util.ArrayList<>();
    }
}