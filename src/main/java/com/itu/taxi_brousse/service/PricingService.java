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
    
    //?== Calculate price for a reservation: ClasseAgeConf -> ClassePlace -> BusVoyage
    public Double calculatePrice(Reservation reservation) {
        return calculatePrice(reservation, LocalDate.now());
    }
    
    //?== Calculate price for a reservation on a specific date: ClasseAgeConf -> ClassePlace -> BusVoyage
    public Double calculatePrice(Reservation reservation, LocalDate pricingDate) {
        if (reservation.getClient() == null || 
            reservation.getClient().getCategorieGroupeAge() == null ||
            reservation.getClassePlace() == null) {
            // Fallback to BusVoyage price if no proper config
            return calculatePrice(reservation.getBusVoyage());
        }
        
        Optional<ClasseAgeConf> config = classeAgeConfRepository.findActiveConfig(
            reservation.getClient().getCategorieGroupeAge(),
            reservation.getClassePlace(),
            pricingDate
        );
        
        if (!config.isPresent()) {
            throw new RuntimeException(
                String.format("Aucune configuration de prix trouvée pour %s sur siège %s", 
                    reservation.getClient().getCategorieGroupeAge().getLibelle(),
                    reservation.getClassePlace().getLibelle())
            );
        }
        
        ClasseAgeConf ageConfig = config.get();
        
        // If percentage-based, we need a baseline price as reference
        if (ageConfig.getEstPourcentage()) {
            Double basePrice = getCategorieGroupeAgeUsedForBaselinePrice(reservation.getClassePlace(), pricingDate, DEFAULT_BASELINE_AGE_GROUP_LIBELLE);
            return ageConfig.calculatePrice(basePrice);
        } else {
            return ageConfig.getValeurOverride(); // Absolute price
        }
    }

    private Double getCategorieGroupeAgeUsedForBaselinePrice(ClassePlace classePlace, LocalDate date, String libelleAgeGroup) {
        if(date == null) {
            date = LocalDate.now();
        }
        
        List<ClasseAgeConf> allConfigs = classeAgeConfRepository.findByClassePlace(classePlace);

        // Look for absolute price configs (aka non-percentage)
        Optional<ClasseAgeConf> baseConfig = allConfigs.stream()
            .filter(c -> !c.getEstPourcentage())
            .filter(c -> c.isActiveOn(LocalDate.now()))
            .filter(c -> c.getCategorieGroupeAge().getLibelle().contains(libelleAgeGroup))
            .findFirst();

        if (baseConfig.isPresent()) {
            return baseConfig.get().getValeurOverride();
        }
        
        // Fallback: use ClassePlace.prix_place if not NULL
        if (classePlace.getPrixPlace() != null) {
            return classePlace.getPrixPlace();
        }
        
        throw new RuntimeException(
            "Impossible de déterminer le prix de base pour " + classePlace.getLibelle()
        );
    }
    
    //?== Calculate price using hierarchy: Bus_Voyage → Voyage
    public Double calculatePrice(BusVoyage busVoyage) {
        // Check Bus_Voyage specific price
        if (busVoyage.getPrixSpecifique() != null) {
            return busVoyage.getPrixSpecifique();
        }
        
        // Check Voyage price
        if (busVoyage.getVoyage().getPrixVoyage() != null) {
            return busVoyage.getVoyage().getPrixVoyage();
        }
        
        return 0.0;
    }

    //?== Get the effective price for a client on a specific seat class
    // Used for UI price display before reservation is created
    public Double getEffectivePrice(CategorieGroupeAge ageGroup, ClassePlace classePlace, LocalDate date) {
        if (ageGroup == null || classePlace == null) {
            return null;
        }
        
        Optional<ClasseAgeConf> config = classeAgeConfRepository.findActiveConfig(
            ageGroup, classePlace, date != null ? date : LocalDate.now()
        );
        
        if (!config.isPresent()) { // use the default classeplace price if no valid config
            return classePlace.getPrixPlace();
        }
        
        ClasseAgeConf ageConfig = config.get();
        
        if (ageConfig.getEstPourcentage()) {
            try {
                Double basePrice = getCategorieGroupeAgeUsedForBaselinePrice(classePlace, date, DEFAULT_BASELINE_AGE_GROUP_LIBELLE);
                return ageConfig.calculatePrice(basePrice);
            } catch (RuntimeException e) {
                return null;
            }
        } else {
            return ageConfig.getValeurOverride();
        }
    }
    
    //?== Check if a discount applies for a given client and seat class
    public boolean hasDiscount(CategorieGroupeAge ageGroup, ClassePlace classePlace, LocalDate date) {
        if (ageGroup == null || classePlace == null) {
            return false;
        }

        Optional<ClasseAgeConf> config = classeAgeConfRepository.findActiveConfig(
            ageGroup, classePlace, date != null ? date : LocalDate.now()
        );
        
        if (!config.isPresent()) {
            return false;
        }
        
        ClasseAgeConf ageConfig = config.get();
        if (ageConfig.getEstPourcentage()) {
            return ageConfig.getValeurOverride() < 0;
        } else {
            try {
                Double basePrice = getCategorieGroupeAgeUsedForBaselinePrice(classePlace, date, DEFAULT_BASELINE_AGE_GROUP_LIBELLE);
                return ageConfig.getValeurOverride() < basePrice;
            } catch (RuntimeException e) {
                return false;
            }
        }
    }
    
    //?== Get discount percentage (for display)
    public Double getDiscountPercentage(CategorieGroupeAge ageGroup, ClassePlace classePlace, LocalDate date) {
        Double effectivePrice = getEffectivePrice(ageGroup, classePlace, date);
        if (effectivePrice == null) {
            return 0.0;
        }
        
        try {
            Double basePrice = getCategorieGroupeAgeUsedForBaselinePrice(classePlace, date, DEFAULT_BASELINE_AGE_GROUP_LIBELLE);
            
            if (effectivePrice.equals(basePrice)) {
                return 0.0;
            }
            
            return ((basePrice - effectivePrice) / basePrice) * 100;
        } catch (RuntimeException e) {
            return 0.0;
        }
    }

    //?=== Check if a booking is allowed (config exists)
    public boolean isBookingAllowed(CategorieGroupeAge ageGroup, ClassePlace classePlace, LocalDate date) {
        return getEffectivePrice(ageGroup, classePlace, date) != null;
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