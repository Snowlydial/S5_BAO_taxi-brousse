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
                                            Integer busClasseId, Double prixMin, Double prixMax) {
        
        //*-- First filter by basic criteria
        List<BusVoyage> results = busVoyageRepository.findWithFilters(gareDepart, gareArrivee, dateDepart, heureDepartMin, busClasseId);
        
        //*-- Then apply price filtering if specified
        if (prixMin != null || prixMax != null) {
            results = filterByPrice(results, prixMin, prixMax);
        }
        
        return results;
    }
    
    //?=== Filter results by price range
    private List<BusVoyage> filterByPrice(List<BusVoyage> busVoyages, Double prixMin, Double prixMax) {
        return busVoyages.stream()
                .filter(bv -> {
                    Double price = pricingService.calculatePrice(bv);
                    boolean minOk = prixMin == null || price >= prixMin;
                    boolean maxOk = prixMax == null || price <= prixMax;
                    return minOk && maxOk;
                })
                .collect(Collectors.toList());
    }
    
    //?=== Get bus voyages with availability info
    public List<BusVoyageWithAvailability> searchWithAvailability(Gare gareDepart, Gare gareArrivee, LocalDate dateDepart, 
                                                                  LocalTime heureDepartMin, Integer busClasseId, 
                                                                  Double prixMin, Double prixMax) {
        
        List<BusVoyage> busVoyages = searchBusVoyages(
                gareDepart, gareArrivee, dateDepart, heureDepartMin, busClasseId, prixMin, prixMax);
        
        return busVoyages.stream()
                .map(this::toBusVoyageWithAvailability)
                .collect(Collectors.toList());
    }
    
    //?=== Convert BusVoyage to DTO with availability info
    private BusVoyageWithAvailability toBusVoyageWithAvailability(BusVoyage busVoyage) {
        Double price = pricingService.calculatePrice(busVoyage);
        Integer availableSeats = availabilityService.getAvailableSeatCount(busVoyage);
        List<Integer> availableSeatNumbers = availabilityService.getAvailableSeats(busVoyage);
        
        return BusVoyageWithAvailability.builder()
                .busVoyage(busVoyage)
                .price(price)
                .availableSeats(availableSeats)
                .availableSeatNumbers(availableSeatNumbers)
                .busClasse(busVoyage.getBus().getBusClasse())
                .build();
    }
    
    //?=== Find available bus voyages for a specific route and date
    public List<BusVoyageWithAvailability> findAvailableTrips(Gare gareDepart, Gare gareArrivee, LocalDate dateDepart) {
        
        return searchWithAvailability(
                gareDepart,
                gareArrivee,
                dateDepart,
                null, // No heure filter
                null, // No classe filter
                null, // No prix min
                null  // No prix max
        );
    }
}