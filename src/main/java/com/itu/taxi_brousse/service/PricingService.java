package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.CategorieGroupeAge;
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
        if (reservation.getClassePlace() == null || reservation.getClient() == null) {
            return calculatePrice(reservation.getBusVoyage());
        }
        
        String classePlaceType = reservation.getClassePlace().getLibelle();
        String ageCategory = reservation.getClient().getCategorieGroupeAge().getLibelle();
        
        // For adult and enfant: use specific override prices
        if (ageCategory.equals("Adulte (18-59 ans)") || ageCategory.equals("Enfant (0-12 ans)")) {
            Double overridePrice = getOverridePrice(reservation.getClient().getCategorieGroupeAge(), classePlaceType);
            if (overridePrice != null && overridePrice > 0) {
                return overridePrice;
            }
        }
        // For senior: apply percentage discount to adult prices
        else if (ageCategory.equals("Senior (60+ ans)")) {
            Double adultPrice = getAdultPriceForClassePlace(classePlaceType);
            Double discountRate = reservation.getClient().getCategorieGroupeAge().getPrixStandardOverride(); // -0.20
            if (adultPrice != null && discountRate != null) {
                return adultPrice * (1 + discountRate); // discountRate is negative, e.g., -0.20
            }
        }
        
        // Fallback to ClassePlace price or BusVoyage price
        if (reservation.getClassePlace().getPrixPlace() != null && 
            reservation.getClassePlace().getPrixPlace() > 0) {
            return reservation.getClassePlace().getPrixPlace();
        }
        
        return calculatePrice(reservation.getBusVoyage());
    }
    
    //?=== Get override price for specific classe place type
    private Double getOverridePrice(CategorieGroupeAge ageCategory, String classePlaceType) {
        switch (classePlaceType) {
            case "Standard":
                return ageCategory.getPrixStandardOverride();
            case "Premium":
                return ageCategory.getPrixPremiumOverride();
            case "VIP":
                return ageCategory.getPrixVipOverride();
            default:
                return null;
        }
    }
    
    //?=== Get adult price for specific classe place type
    private Double getAdultPriceForClassePlace(String classePlaceType) {
        // This should come from database - you might need to inject a repository
        // For now, return the base ClassePlace prices or hardcoded adult prices
        switch (classePlaceType) {
            case "Standard":
                return 50000.0; // Adult Standard price
            case "Premium":
                return 60000.0; // Adult Premium price
            case "VIP":
                return 70000.0; // Adult VIP price
            default:
                return null;
        }
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