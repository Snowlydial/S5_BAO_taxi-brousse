package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.Reservation;
import com.itu.taxi_brousse.entity.HistoriquePrixSpecifique;
import com.itu.taxi_brousse.entity.CategorieGroupeAgeClassePlaceOverride;
import com.itu.taxi_brousse.repository.HistoriquePrixSpecifiqueRepository;
import com.itu.taxi_brousse.repository.CategorieGroupeAgeClassePlaceOverrideRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PricingService {
    
    private final HistoriquePrixSpecifiqueRepository historiquePrixSpecifiqueRepository;
    private final CategorieGroupeAgeClassePlaceOverrideRepository overrideRepository;
    
    //?=== Calculate price for a reservation with age group + seat class discounts
    public Double calculatePrice(Reservation reservation) {
        // Priority 1: Check for age group + seat class override price
        if (reservation.getClient() != null && 
            reservation.getClient().getCategorieGroupeAge() != null &&
            reservation.getClassePlace() != null) {
            
            Optional<CategorieGroupeAgeClassePlaceOverride> override = overrideRepository.findByCategorieGroupeAgeAndClassePlace(
                reservation.getClient().getCategorieGroupeAge(),
                reservation.getClassePlace()
            );
            
            if (override.isPresent()) {
                return override.get().getPrixOverride();
            }
        }
        
        // Priority 2: ClassePlace price (if no override found)
        if (reservation.getClassePlace() != null && 
            reservation.getClassePlace().getPrixPlace() != null && 
            reservation.getClassePlace().getPrixPlace() > 0) {
            return reservation.getClassePlace().getPrixPlace();
        }
        
        // Fallback to BusVoyage price calculation
        return calculatePrice(reservation.getBusVoyage());
    }
    
    //?=== Get override price for a specific age group and seat class
    public Double getOverridePrice(Integer categorieGroupeAgeId, Integer classePlaceId) {
        Optional<CategorieGroupeAgeClassePlaceOverride> override = overrideRepository
            .findByCategorieGroupeAge_IdAndClassePlace_Id(categorieGroupeAgeId, classePlaceId);
        
        return override.map(CategorieGroupeAgeClassePlaceOverride::getPrixOverride).orElse(null);
    }
    
    //?=== Check if there's an override price for a specific age group and seat class
    public boolean hasOverridePrice(Integer categorieGroupeAgeId, Integer classePlaceId) {
        return overrideRepository
            .findByCategorieGroupeAge_IdAndClassePlace_Id(categorieGroupeAgeId, classePlaceId)
            .isPresent();
    }
    
    //?=== Calculate price using hierarchy: Bus_Voyage → Voyage → BusClasse
    public Double calculatePrice(BusVoyage busVoyage) {
        //*-- Check Bus_Voyage specific price
        if (busVoyage.getPrixSpecifique() != null) {
            return busVoyage.getPrixSpecifique();
        }
        
        //*-- Check Voyage price
        if (busVoyage.getVoyage().getPrixVoyage() != null) {
            return busVoyage.getVoyage().getPrixVoyage();
        }
        
        //*-- Fallback to BusClasse price
        return busVoyage.getBus().getBusClasse().getPrixClasse();
    }
    
    //?=== Record initial price when BusVoyage is created
    @Transactional
    public void recordInitialPrice(BusVoyage busVoyage) {
        if (busVoyage.getPrixSpecifique() == null) {
            return; // No price to record
        }
        
        HistoriquePrixSpecifique historique = HistoriquePrixSpecifique.builder()
                .dateEcriture(LocalDateTime.now())
                .prixSpecifique(busVoyage.getPrixSpecifique())
                .busVoyage(busVoyage)
                .build();
        
        historiquePrixSpecifiqueRepository.save(historique);
    }

    //?=== Update Bus_Voyage price and create historical record
    @Transactional
    public void updateBusVoyagePrice(BusVoyage busVoyage, Double newPrice, LocalDateTime updateDate) {
        if (updateDate == null) {
            updateDate = LocalDateTime.now();
        }

        Double oldPrice = busVoyage.getPrixSpecifique();
        
        //*-- Check if price actually changed
        boolean priceChanged = false;
        
        if (oldPrice == null && newPrice != null) {
            priceChanged = true; // From null to value
        } else if (oldPrice != null && newPrice == null) {
            priceChanged = true; // From value to null
        } else if (oldPrice != null && newPrice != null && 
                Math.abs(oldPrice - newPrice) > 0.001) {
            priceChanged = true; // Value changed
        }
        
        if (!priceChanged) {
            return; // No change, no historical record
        }
        
        //*-- Update the price
        busVoyage.setPrixSpecifique(newPrice);
        
        HistoriquePrixSpecifique historique = HistoriquePrixSpecifique.builder()
                .dateEcriture(updateDate)
                .prixSpecifique(newPrice)
                .busVoyage(busVoyage)
                .build();
        
        historiquePrixSpecifiqueRepository.save(historique);
    }

    //?=== Get historical prices for a specific Bus_Voyage
    public List<HistoriquePrixSpecifique> getPriceHistory(BusVoyage busVoyage) {
        return historiquePrixSpecifiqueRepository.findByBusVoyageOrderByDateEcritureDesc(busVoyage);
    }
}