package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.BusBusConf;
import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.Reservation;
import com.itu.taxi_brousse.repository.BusBusConfRepository;
import com.itu.taxi_brousse.repository.ReservationRepository;
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
    
    //?=== Get total capacity of a bus from BusConf
    public Integer getBusCapacity(Integer busId) {
        Optional<BusBusConf> capacityConfig = busBusConfRepository.findCapacityByBusId(busId);
        
        if (capacityConfig.isPresent()) {
            try { // convert String to Integer
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
}