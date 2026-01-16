package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.Reservation;
import com.itu.taxi_brousse.entity.HistoriquePrixSpecifique;
import com.itu.taxi_brousse.repository.HistoriquePrixSpecifiqueRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {
    
    private final HistoriquePrixSpecifiqueRepository historiquePrixSpecifiqueRepository;
    
    //?=== Calculate price for a reservation with age category discounts
    public Double calculatePrice(Reservation reservation) {
        // Priority 1: Check for enfant discount on standard place
        if (shouldApplyEnfantDiscount(reservation)) {
            return reservation.getClient().getCategorieGroupeAge().getPrixStandardOverride();
        }
        
        // Priority 2: Check for senior discount (20% off for all ClassePlace types)
        if (shouldApplySeniorDiscount(reservation)) {
            Double basePrice = getBasePriceForReservation(reservation);
            Double discountRate = reservation.getClient().getCategorieGroupeAge().getPrixStandardOverride();
            return basePrice * (1 + discountRate); // discountRate is -0.20, so 1 - 0.20 = 0.80
        }
        
        // Priority 3: ClassePlace price
        if (reservation.getClassePlace() != null && 
            reservation.getClassePlace().getPrixPlace() != null && 
            reservation.getClassePlace().getPrixPlace() > 0) {
            return reservation.getClassePlace().getPrixPlace();
        }
        
        // Fallback to BusVoyage price calculation
        return calculatePrice(reservation.getBusVoyage());
    }
    
    //?=== Check if enfant discount should be applied
    private boolean shouldApplyEnfantDiscount(Reservation reservation) {
        // Check if client is enfant
        boolean isEnfant = reservation.getClient() != null && 
                          reservation.getClient().getCategorieGroupeAge() != null &&
                          "Enfant (0-12 ans)".equals(reservation.getClient().getCategorieGroupeAge().getLibelle());
        
        // Check if place is standard
        boolean isStandardPlace = reservation.getClassePlace() != null && 
                                 "Standard".equals(reservation.getClassePlace().getLibelle());
        
        // Check if override price is set
        boolean hasOverridePrice = reservation.getClient().getCategorieGroupeAge().getPrixStandardOverride() != null;
        
        return isEnfant && isStandardPlace && hasOverridePrice;
    }
    
    //?=== Check if senior discount should be applied
    private boolean shouldApplySeniorDiscount(Reservation reservation) {
        // Check if client is senior
        boolean isSenior = reservation.getClient() != null && 
                          reservation.getClient().getCategorieGroupeAge() != null &&
                          "Senior (60+ ans)".equals(reservation.getClient().getCategorieGroupeAge().getLibelle());
        
        // Check if discount rate is set (should be -0.20)
        boolean hasDiscountRate = reservation.getClient().getCategorieGroupeAge().getPrixStandardOverride() != null;
        
        return isSenior && hasDiscountRate;
    }
    
    //?=== Get base price for reservation (for senior discount calculation)
    private Double getBasePriceForReservation(Reservation reservation) {
        // First check ClassePlace price
        if (reservation.getClassePlace() != null && 
            reservation.getClassePlace().getPrixPlace() != null && 
            reservation.getClassePlace().getPrixPlace() > 0) {
            return reservation.getClassePlace().getPrixPlace();
        }
        
        // Fallback to BusVoyage price
        return calculatePrice(reservation.getBusVoyage());
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