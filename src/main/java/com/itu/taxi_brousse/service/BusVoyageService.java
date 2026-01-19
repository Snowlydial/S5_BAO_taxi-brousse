package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.Gare;
import com.itu.taxi_brousse.dto.BusVoyageWithAvailability;
import com.itu.taxi_brousse.repository.BusVoyageRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusVoyageService {
    
    private final BusVoyageRepository busVoyageRepository;
    private final PricingService pricingService;
    private final DisponibilitePlaceService availabilityService;
    
    @Transactional
    public BusVoyage createBusVoyage(BusVoyage busVoyage) {
        BusVoyage saved = busVoyageRepository.save(busVoyage);
        pricingService.recordInitialPrice(saved);
        return saved;
    }

    //?=== Search bus voyages with multiple filters
    public List<BusVoyage> searchBusVoyages(Gare gareDepart, Gare gareArrivee, LocalDate dateDepart, LocalTime heureDepartMin, 
                                            Double prixMin, Double prixMax) {
        
        //*-- First filter by basic criteria
        List<BusVoyage> results = busVoyageRepository.findWithFilters(gareDepart, gareArrivee, dateDepart, heureDepartMin);
        
        //*-- Then apply price filtering if specified
        if (prixMin != null || prixMax != null) {
            results = filterByPrice(results, prixMin, prixMax);
        }
        
        return results;
    }
    
    //?=== Filter results by MINIMUM base ClassePlace price (no age discounts)
    // This gives "starting from" pricing - the cheapest seat type on the bus
    private List<BusVoyage> filterByPrice(List<BusVoyage> busVoyages, Double prixMin, Double prixMax) {
        return busVoyages.stream()
                .filter(bv -> {
                    Double minBasePrice = getMinimumBasePlacePrice(bv);
                    
                    // If no place types configured, fall back to voyage/bus_voyage price
                    if (minBasePrice == null) {
                        minBasePrice = pricingService.calculatePrice(bv);
                    }
                    
                    boolean minOk = prixMin == null || minBasePrice >= prixMin;
                    boolean maxOk = prixMax == null || minBasePrice <= prixMax;
                    return minOk && maxOk;
                })
                .collect(Collectors.toList());
    }
    
    //?=== Get minimum BASE price (ClassePlace.prix_place) for this bus
    // Returns the cheapest seat type's base price (before any age discounts)
    private Double getMinimumBasePlacePrice(BusVoyage busVoyage) {
        try {
            // Get all place type prices (these are base ClassePlace prices)
            java.util.Map<String, Double> placeTypePrices = availabilityService.getPlaceTypePrices(busVoyage);
            
            if (placeTypePrices == null || placeTypePrices.isEmpty()) {
                return null;
            }
            
            // Return the minimum base price
            return placeTypePrices.values().stream()
                    .min(Double::compare)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    //?=== Get bus voyages with availability info
    public List<BusVoyageWithAvailability> searchWithAvailability(Gare gareDepart, Gare gareArrivee, LocalDate dateDepart, 
                                                                  LocalTime heureDepartMin, 
                                                                  Double prixMin, Double prixMax) {
        
        List<BusVoyage> busVoyages = searchBusVoyages(gareDepart, gareArrivee, dateDepart, heureDepartMin, prixMin, prixMax);
        
        return busVoyages.stream()
                .map(this::toBusVoyageWithAvailability)
                .collect(Collectors.toList());
    }

    //?=== Search bus voyages across a whole year (returns DTOs with availability)
    public List<BusVoyageWithAvailability> searchByYear(Gare gareDepart, Gare gareArrivee, Integer year,
                                                       LocalTime heureDepartMin, Double prixMin, Double prixMax) {
        if (year == null) {
            return searchWithAvailability(gareDepart, gareArrivee, LocalDate.now(), heureDepartMin, prixMin, prixMax);
        }

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        List<BusVoyage> voyages = busVoyageRepository.findByDateDepartBetween(start, end);

        List<BusVoyage> filtered = voyages.stream()
                .filter(bv -> {
                    boolean departOk = true;
                    if (gareDepart != null) {
                        departOk = bv.getVoyage() != null && gareDepart.equals(bv.getVoyage().getGareDepart());
                    }

                    boolean arriveeOk = true;
                    if (gareArrivee != null) {
                        arriveeOk = bv.getVoyage() != null && gareArrivee.equals(bv.getVoyage().getGareArrivee());
                    }

                    boolean heureOk = true;
                    if (heureDepartMin != null) {
                        heureOk = bv.getHeureDepart() != null && !bv.getHeureDepart().isBefore(heureDepartMin);
                    }

                    return departOk && arriveeOk && heureOk;
                })
                .collect(Collectors.toList());

        if (prixMin != null || prixMax != null) {
            filtered = filterByPrice(filtered, prixMin, prixMax);
        }

        return filtered.stream()
                .map(this::toBusVoyageWithAvailability)
                .collect(Collectors.toList());
    }
    
    //?=== Convert BusVoyage to DTO with availability info
    private BusVoyageWithAvailability toBusVoyageWithAvailability(BusVoyage busVoyage) {
        Double price = pricingService.calculatePrice(busVoyage);
        Integer capacity = availabilityService.getBusCapacity(busVoyage.getBus().getId());
        Integer availableSeats = availabilityService.getAvailableSeatCount(busVoyage);
        List<Integer> availableSeatNumbers = availabilityService.getAvailableSeats(busVoyage);
        
        // Get place type information
        java.util.Map<String, Integer> placeTypeCapacities = availabilityService.getPlaceTypeCapacities(busVoyage.getBus().getId());
        java.util.Map<String, Integer> placeTypeAvailableSeats = availabilityService.getAvailableSeatsByType(busVoyage);
        java.util.Map<String, Double> placeTypePrices = availabilityService.getPlaceTypePrices(busVoyage);
        
        return BusVoyageWithAvailability.builder()
                .busVoyage(busVoyage)
                .price(price)
                .capacity(capacity)
                .availableSeats(availableSeats)
                .availableSeatNumbers(availableSeatNumbers)
                .placeTypeCapacities(placeTypeCapacities)
                .placeTypeAvailableSeats(placeTypeAvailableSeats)
                .placeTypePrices(placeTypePrices)
                .build();
    }
    
    //?=== Find available bus voyages for a specific route and date
    public List<BusVoyageWithAvailability> findAvailableTrips(Gare gareDepart, Gare gareArrivee, LocalDate dateDepart) {
        
        return searchWithAvailability(
                gareDepart,
                gareArrivee,
                dateDepart,
                null, // No heure filter
                null, // No prix min
                null  // No prix max
        );
    }
}