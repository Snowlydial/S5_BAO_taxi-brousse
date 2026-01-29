package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.*;
import com.itu.taxi_brousse.repository.ClasseAgeConfRepository;
import com.itu.taxi_brousse.repository.HistoriquePrixSpecifiqueRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PricingService {
    
    private final HistoriquePrixSpecifiqueRepository historiquePrixSpecifiqueRepository;
    private final ClasseAgeConfRepository classeAgeConfRepository;
    private final String DEFAULT_BASELINE_AGE_GROUP_LIBELLE = "Adulte";
    
    //?== Calculate price for a reservation using new hierarchy
    public Double calculatePrice(Reservation reservation) {
        return calculatePrice(reservation, LocalDate.now());
    }
    
    //?== Calculate price for a reservation on a specific date using new hierarchy
    public Double calculatePrice(Reservation reservation, LocalDate pricingDate) {
        if (reservation.getClient() == null || 
            reservation.getClient().getCategorieGroupeAge() == null ||
            reservation.getClassePlace() == null ||
            reservation.getBusVoyage() == null) {
            return 0.0;
        }
        
        CategorieGroupeAge ageGroup = reservation.getClient().getCategorieGroupeAge();
        ClassePlace classePlace = reservation.getClassePlace();
        Voyage voyage = reservation.getBusVoyage().getVoyage();
        BusVoyage busVoyage = reservation.getBusVoyage();
        
        //*-- Priority 1: Check ClasseAgeConf for this specific voyage
        Optional<ClasseAgeConf> voyageConfig = classeAgeConfRepository.findActiveConfigByVoyage(
            voyage, ageGroup, classePlace, pricingDate
        );
        
        if (voyageConfig.isPresent()) {
            ClasseAgeConf config = voyageConfig.get();
            
            if (config.getEstPourcentage()) {
                //*-- Percentage-based: need base price
                Double basePrice = getBasePriceForVoyage(voyage, classePlace, pricingDate);
                return config.calculatePrice(basePrice);
            } else {
                //*-- Absolute price
                return config.getValeurOverride();
            }
        }
        
        //*-- Priority 2: BusVoyage specific price
        if (busVoyage.getPrixSpecifique() != null) {
            return busVoyage.getPrixSpecifique();
        }
        
        //*-- Priority 3: Voyage price
        if (voyage.getPrixVoyage() != null) {
            return voyage.getPrixVoyage();
        }
        
        //*-- Priority 4: ClassePlace fallback price
        if (classePlace.getPrixPlace() != null) {
            return classePlace.getPrixPlace();
        }
        
        //*-- Final fallback
        return 0.0;
    }
    
    //?== Get base price for percentage calculations (for a specific voyage)
    private Double getBasePriceForVoyage(Voyage voyage, ClassePlace classePlace, LocalDate date) {
        //*-- Look for adult (baseline) absolute price config for this voyage
        List<ClasseAgeConf> allConfigs = classeAgeConfRepository.findAllActiveConfigsByVoyage(voyage, date);
        
        Optional<ClasseAgeConf> baseConfig = allConfigs.stream()
            .filter(c -> !c.getEstPourcentage())
            .filter(c -> c.getClassePlace().equals(classePlace))
            .filter(c -> c.getCategorieGroupeAge().getLibelle().contains(DEFAULT_BASELINE_AGE_GROUP_LIBELLE))
            .findFirst();
        
        if (baseConfig.isPresent()) {
            return baseConfig.get().getValeurOverride();
        }
        
        //*-- Fallback to voyage price
        if (voyage.getPrixVoyage() != null) {
            return voyage.getPrixVoyage();
        }
        
        //*-- Fallback to ClassePlace price
        if (classePlace.getPrixPlace() != null) {
            return classePlace.getPrixPlace();
        }
        
        return 0.0;
    }
    
    //?== Calculate price using hierarchy: Bus_Voyage → Voyage → 0.0
    public Double calculatePrice(BusVoyage busVoyage) {
        //*-- Check Bus_Voyage specific price
        if (busVoyage.getPrixSpecifique() != null) {
            return busVoyage.getPrixSpecifique();
        }
        
        //*-- Check Voyage price
        if (busVoyage.getVoyage().getPrixVoyage() != null) {
            return busVoyage.getVoyage().getPrixVoyage();
        }
        
        return 0.0;
    }

    //?== Get the effective price for a client on a specific seat class for a specific voyage
    // Used for UI price display before reservation is created
    public Double getEffectivePrice(Voyage voyage, CategorieGroupeAge ageGroup, ClassePlace classePlace, LocalDate date) {
        if (voyage == null || ageGroup == null || classePlace == null) {
            return null;
        }
        
        //*-- Priority 1: Check voyage-specific config
        Optional<ClasseAgeConf> voyageConfig = classeAgeConfRepository.findActiveConfigByVoyage(
            voyage, ageGroup, classePlace, date != null ? date : LocalDate.now()
        );
        
        if (voyageConfig.isPresent()) {
            ClasseAgeConf config = voyageConfig.get();
            
            if (config.getEstPourcentage()) {
                Double basePrice = getBasePriceForVoyage(voyage, classePlace, date);
                return config.calculatePrice(basePrice);
            } else {
                return config.getValeurOverride();
            }
        }
        
        //*-- Priority 2-4: Fallback chain
        if (voyage.getPrixVoyage() != null) {
            return voyage.getPrixVoyage();
        }
        
        if (classePlace.getPrixPlace() != null) {
            return classePlace.getPrixPlace();
        }
        
        return 0.0;
    }
    
    //?== LEGACY: Get effective price without voyage (backward compatibility)
    public Double getEffectivePrice(CategorieGroupeAge ageGroup, ClassePlace classePlace, LocalDate date) {
        if (ageGroup == null || classePlace == null) {
            return null;
        }
        
        //*-- Try to find config without voyage
        Optional<ClasseAgeConf> config = classeAgeConfRepository.findActiveConfigWithoutVoyage(
            ageGroup, classePlace, date != null ? date : LocalDate.now()
        );
        
        if (config.isPresent()) {
            ClasseAgeConf ageConfig = config.get();
            
            if (ageConfig.getEstPourcentage()) {
                if (classePlace.getPrixPlace() != null) {
                    return ageConfig.calculatePrice(classePlace.getPrixPlace());
                }
                return 0.0;
            } else {
                return ageConfig.getValeurOverride();
            }
        }
        
        //*-- Fallback to ClassePlace price
        return classePlace.getPrixPlace() != null ? classePlace.getPrixPlace() : 0.0;
    }
    
    //?== Check if a discount applies for a given client and seat class on a voyage
    public boolean hasDiscount(Voyage voyage, CategorieGroupeAge ageGroup, ClassePlace classePlace, LocalDate date) {
        if (voyage == null || ageGroup == null || classePlace == null) {
            return false;
        }

        Double effectivePrice = getEffectivePrice(voyage, ageGroup, classePlace, date);
        Double basePrice = getBasePriceForVoyage(voyage, classePlace, date);
        
        if (effectivePrice == null || basePrice == null) {
            return false;
        }
        
        return effectivePrice < basePrice;
    }
    
    //?== LEGACY: Check if discount applies without voyage
    public boolean hasDiscount(CategorieGroupeAge ageGroup, ClassePlace classePlace, LocalDate date) {
        if (ageGroup == null || classePlace == null) {
            return false;
        }

        Optional<ClasseAgeConf> config = classeAgeConfRepository.findActiveConfigWithoutVoyage(
            ageGroup, classePlace, date != null ? date : LocalDate.now()
        );
        
        if (!config.isPresent()) {
            return false;
        }
        
        ClasseAgeConf ageConfig = config.get();
        if (ageConfig.getEstPourcentage()) {
            return ageConfig.getValeurOverride() < 0;
        } else {
            if (classePlace.getPrixPlace() != null) {
                return ageConfig.getValeurOverride() < classePlace.getPrixPlace();
            }
            return false;
        }
    }
    
    //?== Get discount percentage based on baseline price (for display)
    public Double getDiscountPercentage(Voyage voyage, CategorieGroupeAge ageGroup, ClassePlace classePlace, LocalDate date) {
        Double effectivePrice = getEffectivePrice(voyage, ageGroup, classePlace, date);
        if (effectivePrice == null) {
            return 0.0;
        }
        
        Double basePrice = getBasePriceForVoyage(voyage, classePlace, date);
        
        if (effectivePrice.equals(basePrice)) {
            return 0.0;
        }
        
        return ((basePrice - effectivePrice) / basePrice) * 100;
    }
    
    //?== LEGACY: Get discount percentage without voyage
    public Double getDiscountPercentage(CategorieGroupeAge ageGroup, ClassePlace classePlace, LocalDate date) {
        Double effectivePrice = getEffectivePrice(ageGroup, classePlace, date);
        if (effectivePrice == null) {
            return 0.0;
        }
        
        if (classePlace.getPrixPlace() == null) {
            return 0.0;
        }
        
        if (effectivePrice.equals(classePlace.getPrixPlace())) {
            return 0.0;
        }
        
        return ((classePlace.getPrixPlace() - effectivePrice) / classePlace.getPrixPlace()) * 100;
    }

    //?=== Check if a booking is allowed (config exists for voyage)
    public boolean isBookingAllowed(Voyage voyage, CategorieGroupeAge ageGroup, ClassePlace classePlace, LocalDate date) {
        return getEffectivePrice(voyage, ageGroup, classePlace, date) != null;
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

    //?=== Get the baseline price for a voyage
    public Double getBaselinePrice(Voyage voyage, ClassePlace classePlace, LocalDate date) {
        return getBasePriceForVoyage(voyage, classePlace, date);
    }
    
    //?=== LEGACY: Get baseline price without voyage
    public Double getBaselinePrice(ClassePlace classePlace, LocalDate date) {
        return classePlace.getPrixPlace() != null ? classePlace.getPrixPlace() : 0.0;
    }
}