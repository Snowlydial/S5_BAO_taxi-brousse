package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.BusBusConf;
import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.Reservation;
import com.itu.taxi_brousse.entity.ClassePlace;
import com.itu.taxi_brousse.repository.BusBusConfRepository;
import com.itu.taxi_brousse.repository.ReservationRepository;
import com.itu.taxi_brousse.repository.ClassePlaceRepository;
import com.itu.taxi_brousse.util.exception.CapacityConfigurationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisponibilitePlaceService {
    
    private final ReservationRepository reservationRepository;
    private final BusBusConfRepository busBusConfRepository;
    private final ClassePlaceRepository classePlaceRepository;
    
    //?=== Get total capacity of a bus from BusConf
    public Integer getBusCapacity(Integer busId) {
        Optional<BusBusConf> capacityConfig = busBusConfRepository.findCapacityByBusId(busId);
        
        if (capacityConfig.isPresent()) {
            try {
                return Integer.parseInt(capacityConfig.get().getBusConf().getValeur());
            } catch (NumberFormatException e) {
                throw new CapacityConfigurationException(
                    "Invalid capacity value '" + capacityConfig.get().getBusConf().getValeur() + "' for bus ID: " + busId
                );
            }
        }
        
        // Fallback capacity if not configured
        return 20;
    }
    
    //?=== Get number of premium seats for a bus
    public Integer getNbPlacePremium(Integer busId) {
        return busBusConfRepository.findByBusId(busId).stream()
            .filter(link -> "nb_place_premium".equalsIgnoreCase(link.getBusConf().getLibelle()))
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
    
    //?=== Get number of standard seats for a bus
    public Integer getNbPlaceStandard(Integer busId) {
        return busBusConfRepository.findByBusId(busId).stream()
            .filter(link -> "nb_place_standard".equalsIgnoreCase(link.getBusConf().getLibelle()))
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
    
    //?=== Get available Premium seats count
    public Integer getAvailablePremiumSeats(BusVoyage busVoyage) {
        Integer totalPremium = getNbPlacePremium(busVoyage.getBus().getId());
        Integer occupiedPremium = getOccupiedPremiumSeats(busVoyage);
        return totalPremium - occupiedPremium;
    }
    
    //?=== Get available Standard seats count
    public Integer getAvailableStandardSeats(BusVoyage busVoyage) {
        Integer totalStandard = getNbPlaceStandard(busVoyage.getBus().getId());
        Integer occupiedStandard = getOccupiedStandardSeats(busVoyage);
        return totalStandard - occupiedStandard;
    }
    
    //?=== Get occupied Premium seats count
    private Integer getOccupiedPremiumSeats(BusVoyage busVoyage) {
        ClassePlace premium = classePlaceRepository.findByLibelleIgnoreCase("Premium").orElse(null);
        if (premium == null) return 0;
        
        return (int) reservationRepository.findByBusVoyage(busVoyage).stream()
            .filter(r -> r.getClassePlace() != null && r.getClassePlace().getId().equals(premium.getId()))
            .count();
    }
    
    //?=== Get occupied Standard seats count
    private Integer getOccupiedStandardSeats(BusVoyage busVoyage) {
        ClassePlace standard = classePlaceRepository.findByLibelleIgnoreCase("Standard").orElse(null);
        if (standard == null) return 0;
        
        return (int) reservationRepository.findByBusVoyage(busVoyage).stream()
            .filter(r -> r.getClassePlace() != null && r.getClassePlace().getId().equals(standard.getId()))
            .count();
    }
    
    //?=== Calculate potential maximum revenue for a bus
    public Double calculatePotentialRevenue(Integer busId) {
        Integer nbPremium = getNbPlacePremium(busId);
        Integer nbStandard = getNbPlaceStandard(busId);
        
        ClassePlace premium = classePlaceRepository.findByLibelleIgnoreCase("Premium").orElse(null);
        ClassePlace standard = classePlaceRepository.findByLibelleIgnoreCase("Standard").orElse(null);
        
        Double prixPremium = (premium != null && premium.getPrixPlace() != null) ? premium.getPrixPlace() : 0.0;
        Double prixStandard = (standard != null && standard.getPrixPlace() != null) ? standard.getPrixPlace() : 0.0;
        
        return (nbPremium * prixPremium) + (nbStandard * prixStandard);
    }
}