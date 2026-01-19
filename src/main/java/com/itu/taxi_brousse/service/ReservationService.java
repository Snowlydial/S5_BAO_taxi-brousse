package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.*;
import com.itu.taxi_brousse.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final DisponibilitePlaceService availabilityService;
    private final ReservationStatutService reservationStatutService;
    
    //?=== Create a single reservation with seat selection
    @Transactional
    public Reservation createReservation(Client client, BusVoyage busVoyage, Integer seatNumber) {
        //*-- Validate seat availability
        if (!availabilityService.isSeatAvailable(busVoyage, seatNumber)) {
            throw new RuntimeException("Seat " + seatNumber + " is not available");
        }
        
        //*-- Create reservation
        Reservation reservation = Reservation.builder()
                .client(client)
                .busVoyage(busVoyage)
                .numeroPlace(seatNumber)
                .build();
        
        reservation = reservationRepository.save(reservation);
        
        //*-- Create initial active status
        reservationStatutService.createActiveStatus(reservation);
        
        return reservation;
    }
    
    //?=== Create multiple reservations (group booking)
    @Transactional
    public List<Reservation> createMultipleReservations(Client client, BusVoyage busVoyage, List<Integer> seatNumbers) {
        List<Reservation> reservations = new ArrayList<>();
        
        for (Integer seatNumber : seatNumbers) {
            //*-- Validate each seat
            if (!availabilityService.isSeatAvailable(busVoyage, seatNumber)) {
                throw new RuntimeException("Seat " + seatNumber + " is not available");
            }
            
            Reservation reservation = Reservation.builder()
                    .client(client)
                    .busVoyage(busVoyage)
                    .numeroPlace(seatNumber)
                    .build();
            
            reservations.add(reservation);
        }
        
        //*-- Save all reservations
        reservations = reservationRepository.saveAll(reservations);
        
        //*-- Create active status for each reservation
        for (Reservation reservation : reservations) {
            reservationStatutService.createActiveStatus(reservation);
        }
        
        return reservations;
    }
    
    //?=== Create multiple reservations with ClassePlace
    @Transactional
    public List<Reservation> createMultipleReservations(Client client, BusVoyage busVoyage, List<Integer> seatNumbers, Map<Integer, ClassePlace> seatClasseMap) {
        List<Reservation> reservations = new ArrayList<>();
        
        for (Integer seatNumber : seatNumbers) {
            //*-- Validate each seat
            if (!availabilityService.isSeatAvailable(busVoyage, seatNumber)) {
                throw new RuntimeException("Seat " + seatNumber + " is not available");
            }
            
            ClassePlace classePlace = seatClasseMap.get(seatNumber);
            
            Reservation reservation = Reservation.builder()
                    .numeroPlace(seatNumber)
                    .classePlace(classePlace)
                    .client(client)
                    .busVoyage(busVoyage)
                    .build();
            
            reservations.add(reservation);
        }
        
        //*-- Save all reservations
        reservations = reservationRepository.saveAll(reservations);
        
        //*-- Create active status for each reservation
        for (Reservation reservation : reservations) {
            reservationStatutService.createActiveStatus(reservation);
        }
        
        return reservations;
    }
    
    //?=== Cancel a reservation with specific date
    @Transactional
    public void cancelReservation(Integer reservationId, LocalDate dateAnnulation) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        //*-- Create cancellation status
        reservationStatutService.cancelReservation(reservation, dateAnnulation);
    }
    
    //?=== Get all reservations for a client
    public List<Reservation> getClientReservations(Client client) {
        return reservationRepository.findByClient(client);
    }
    
    //?=== Get all reservations for a bus voyage
    public List<Reservation> getBusVoyageReservations(BusVoyage busVoyage) {
        return reservationRepository.findByBusVoyage(busVoyage);
    }
    
    //?=== Change seat for an existing reservation
    @Transactional
    public Reservation changeSeat(Integer reservationId, Integer newSeatNumber) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        BusVoyage busVoyage = reservation.getBusVoyage();
        
        //*-- Check if new seat is available (excluding current seat)
        if (!newSeatNumber.equals(reservation.getNumeroPlace()) && 
            !availabilityService.isSeatAvailable(busVoyage, newSeatNumber)) {
            throw new RuntimeException("Seat " + newSeatNumber + " is not available");
        }
        
        reservation.setNumeroPlace(newSeatNumber);
        return reservationRepository.save(reservation);
    }
}