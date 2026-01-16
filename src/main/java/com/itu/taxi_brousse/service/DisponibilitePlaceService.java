package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.*;
import com.itu.taxi_brousse.repository.*;
import com.itu.taxi_brousse.util.exception.CapacityConfigurationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisponibilitePlaceService {
    
    private final ReservationRepository reservationRepository;
    private final BusBusConfRepository busBusConfRepository;
    private final ClassePlaceRepository classePlaceRepository;
    
    //?=== Get total capacity from sum of all nb_place_* configurations
    public Integer getBusCapacity(Integer busId) {
        List<BusBusConf> configs = busBusConfRepository.findByBusId(busId);
        
        int totalCapacity = configs.stream()
            .filter(link -> link.getBusConf().getLibelle().startsWith("nb_place_"))
            .mapToInt(link -> {
                try {
                    return Integer.parseInt(link.getBusConf().getValeur());
                } catch (NumberFormatException e) {
                    return 0;
                }
            })
            .sum();
        
        if (totalCapacity == 0) {
            throw new CapacityConfigurationException(
                "No valid nb_place_* configurations found for bus ID: " + busId
            );
        }
        
        return totalCapacity;
    }
    
    //?=== Get all place type configurations for a bus (nb_place_Premium, nb_place_Standard, etc.)
    public Map<String, Integer> getPlaceTypeCapacities(Integer busId) {
        List<BusBusConf> configs = busBusConfRepository.findByBusId(busId);
        
        Map<String, Integer> capacities = new HashMap<>();
        
        configs.stream()
            .filter(link -> link.getBusConf().getLibelle().startsWith("nb_place_"))
            .forEach(link -> {
                // Extract place type from "nb_place_premium" -> "premium"
                String placeType = link.getBusConf().getLibelle().substring("nb_place_".length());
                
                // Normalize to match ClassePlace using case-insensitive lookup
                // This handles: premium->Premium, VIP->VIP, standard->Standard
                String normalizedType = normalizeToClassePlaceCase(placeType);
                
                try {
                    int capacity = Integer.parseInt(link.getBusConf().getValeur());
                    capacities.put(normalizedType, capacity);
                } catch (NumberFormatException e) {
                    // Skip invalid configurations
                }
            });
        
        return capacities;
    }
    
    //?=== Normalize place type name to match ClassePlace case (case-insensitive lookup)
    private String normalizeToClassePlaceCase(String placeType) {
        // Find matching ClassePlace ignoring case
        Optional<ClassePlace> match = classePlaceRepository.findAll().stream()
            .filter(cp -> cp.getLibelle().equalsIgnoreCase(placeType))
            .findFirst();
        
        // Return exact ClassePlace libelle if found, otherwise use Title Case
        return match.map(ClassePlace::getLibelle)
                   .orElse(placeType.substring(0, 1).toUpperCase() + 
                          placeType.substring(1).toLowerCase());
    }
    
    //?=== Get number of seats for a specific place type (case-insensitive)
    public Integer getPlaceTypeCapacity(Integer busId, String placeType) {
        return busBusConfRepository.findByBusId(busId).stream()
            .filter(link -> link.getBusConf().getLibelle().equalsIgnoreCase("nb_place_" + placeType))
            .findFirst()
            .map(link -> {
                try {
                    return Integer.parseInt(link.getBusConf().getValeur());
                } catch (NumberFormatException e) {
                    return 0;
                }
            })
            .orElse(0);
    }
    
    //?=== Get available seat numbers for a Bus_Voyage
    public List<Integer> getAvailableSeats(BusVoyage busVoyage) {
        Integer capacity = getBusCapacity(busVoyage.getBus().getId());
        List<Reservation> reservations = reservationRepository.findByBusVoyage(busVoyage);
        
        //*-- Get taken seat numbers
        List<Integer> takenSeats = reservations.stream()
                .map(Reservation::getNumeroPlace)
                .collect(Collectors.toList());
        
        //*-- Generate all possible seats (1 to capacity)
        List<Integer> allSeats = new ArrayList<>();
        for (int i = 1; i <= capacity; i++) {
            allSeats.add(i);
        }
        
        //*-- Return seats not in takenSeats
        return allSeats.stream()
                .filter(seat -> !takenSeats.contains(seat))
                .collect(Collectors.toList());
    }
    
    //?=== Check if a specific seat is available
    public boolean isSeatAvailable(BusVoyage busVoyage, Integer seatNumber) {
        List<Integer> availableSeats = getAvailableSeats(busVoyage);
        return availableSeats.contains(seatNumber);
    }
    
    //?=== Get number of available seats
    public Integer getAvailableSeatCount(BusVoyage busVoyage) {
        Integer capacity = getBusCapacity(busVoyage.getBus().getId());
        Long reservedCount = reservationRepository.countByBusVoyage(busVoyage);
        return capacity - reservedCount.intValue();
    }
    
    //?=== Get available seats count by place type (Premium, Standard, VIP, etc.)
    public Map<String, Integer> getAvailableSeatsByType(BusVoyage busVoyage) {
        Map<String, Integer> totalCapacities = getPlaceTypeCapacities(busVoyage.getBus().getId());
        Map<String, Integer> occupiedSeats = getOccupiedSeatsByType(busVoyage);
        
        Map<String, Integer> availableSeats = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : totalCapacities.entrySet()) {
            String placeType = entry.getKey();
            Integer total = entry.getValue();
            Integer occupied = occupiedSeats.getOrDefault(placeType, 0);
            availableSeats.put(placeType, total - occupied);
        }
        
        return availableSeats;
    }
    
    //?=== Get occupied seats count by place type
    private Map<String, Integer> getOccupiedSeatsByType(BusVoyage busVoyage) {
        List<Reservation> reservations = reservationRepository.findByBusVoyage(busVoyage);
        Map<String, Integer> occupiedSeats = new HashMap<>();
        
        for (Reservation reservation : reservations) {
            if (reservation.getClassePlace() != null) {
                String placeType = reservation.getClassePlace().getLibelle();
                occupiedSeats.put(placeType, occupiedSeats.getOrDefault(placeType, 0) + 1);
            }
        }
        
        return occupiedSeats;
    }
    
    //?=== Calculate potential maximum revenue for a bus
    public Double calculatePotentialRevenue(Integer busId) {
        Map<String, Integer> placeCapacities = getPlaceTypeCapacities(busId);
        Double totalRevenue = 0.0;
        
        for (Map.Entry<String, Integer> entry : placeCapacities.entrySet()) {
            String placeType = entry.getKey();
            Integer capacity = entry.getValue();
            
            // Find corresponding ClassePlace
            Optional<ClassePlace> classePlaceOpt = classePlaceRepository
                .findByLibelleIgnoreCase(placeType);
            
            if (classePlaceOpt.isPresent() && classePlaceOpt.get().getPrixPlace() != null) {
                totalRevenue += capacity * classePlaceOpt.get().getPrixPlace();
            }
        }
        
        return totalRevenue;
    }
    
    //?=== Validate if seat selection is valid for a bus voyage
    public boolean validateSeatSelection(BusVoyage busVoyage, Map<Integer, ClassePlace> seatClasseMap) {
        Map<String, Integer> availableByType = getAvailableSeatsByType(busVoyage);
        Map<String, Long> requestedByType = seatClasseMap.values().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(ClassePlace::getLibelle, Collectors.counting()));
        
        for (Map.Entry<String, Long> entry : requestedByType.entrySet()) {
            String placeType = entry.getKey();
            Long requested = entry.getValue();
            Integer available = availableByType.getOrDefault(placeType, 0);
            
            if (requested > available) {
                return false;
            }
        }
        
        return true;
    }
    
    //?=== Get place type prices for a bus voyage
    public Map<String, Double> getPlaceTypePrices(BusVoyage busVoyage) {
        Map<String, Double> placeTypePrices = new HashMap<>();
        Map<String, Integer> placeTypeCapacities = getPlaceTypeCapacities(busVoyage.getBus().getId());
        
        // Get all ClassePlace records
        List<ClassePlace> allClassePlaces = classePlaceRepository.findAll();
        Map<String, Double> prixMap = new HashMap<>();
        allClassePlaces.forEach(cp -> prixMap.put(cp.getLibelle(), cp.getPrixPlace()));
        
        // Map place types to their prices
        for (String placeType : placeTypeCapacities.keySet()) {
            Double price = prixMap.get(placeType);
            if (price != null) {
                placeTypePrices.put(placeType, price);
            }
        }
        
        return placeTypePrices;
    }
    
    //?=== Get minimum price among available place types
    public Double getMinAvailablePrice(BusVoyage busVoyage) {
        Map<String, Integer> availableSeatsByType = getAvailableSeatsByType(busVoyage);
        Map<String, Double> placeTypePrices = getPlaceTypePrices(busVoyage);
        
        return availableSeatsByType.entrySet().stream()
            .filter(entry -> entry.getValue() > 0) // Only types with available seats
            .map(entry -> placeTypePrices.get(entry.getKey()))
            .filter(Objects::nonNull)
            .min(Double::compare)
            .orElse(0.0);
    }
    
    //?=== Get all place types with their details (capacity, available, price)
    public List<PlaceTypeDetails> getPlaceTypeDetails(BusVoyage busVoyage) {
        Map<String, Integer> capacities = getPlaceTypeCapacities(busVoyage.getBus().getId());
        Map<String, Integer> available = getAvailableSeatsByType(busVoyage);
        Map<String, Double> prices = getPlaceTypePrices(busVoyage);
        
        List<PlaceTypeDetails> details = new ArrayList<>();
        
        for (String placeType : capacities.keySet()) {
            PlaceTypeDetails detail = PlaceTypeDetails.builder()
                .placeType(placeType)
                .totalCapacity(capacities.get(placeType))
                .availableSeats(available.getOrDefault(placeType, 0))
                .price(prices.getOrDefault(placeType, 0.0))
                .build();
            details.add(detail);
        }
        
        return details;
    }
    
    //?=== DTO for place type details
    @lombok.Builder
    @lombok.Data
    public static class PlaceTypeDetails {
        private String placeType;
        private Integer totalCapacity;
        private Integer availableSeats;
        private Double price;
        
        public boolean hasAvailableSeats() {
            return availableSeats != null && availableSeats > 0;
        }
    }
}