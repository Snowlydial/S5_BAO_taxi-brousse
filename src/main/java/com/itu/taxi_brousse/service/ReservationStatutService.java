package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.Reservation;
import com.itu.taxi_brousse.entity.ReservationStatut;
import com.itu.taxi_brousse.repository.ReservationStatutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationStatutService {
    
    private final ReservationStatutRepository reservationStatutRepository;
    
    //?=== Create initial active status for a new reservation
    @Transactional
    public ReservationStatut createActiveStatus(Reservation reservation) {
        ReservationStatut statut = ReservationStatut.builder()
                .reservation(reservation)
                .dateAnnulation(null) // null == active
                .build();
        
        return reservationStatutRepository.save(statut);
    }
    
    //?=== Cancel a reservation with a specific date
    @Transactional
    public ReservationStatut cancelReservation(Reservation reservation, LocalDate dateAnnulation) {
        //*-- Validate cancellation date is before departure
        LocalDate dateDepart = reservation.getBusVoyage().getDateDepart();
        if (dateAnnulation.isAfter(dateDepart)) {
            throw new RuntimeException("La date d'annulation ne peut pas être après la date de départ");
        }
        
        //*-- Create cancellation status
        ReservationStatut statut = ReservationStatut.builder()
                .reservation(reservation)
                .dateAnnulation(dateAnnulation)
                .build();
        
        return reservationStatutRepository.save(statut);
    }
    
    //?=== Get the latest status for a reservation
    public ReservationStatut getLatestStatut(Reservation reservation) {
        Optional<ReservationStatut> latestStatut = 
            reservationStatutRepository.findLatestByReservation(reservation);
        
        return latestStatut.orElse(null);
    }
    
    //?=== Check if a reservation is currently active
    public boolean isReservationActive(Reservation reservation) {
        ReservationStatut latestStatut = getLatestStatut(reservation);
        return latestStatut != null && !latestStatut.isAnnule();
    }
    
    //?=== Check if a reservation is cancelled
    public boolean isReservationCancelled(Reservation reservation) {
        ReservationStatut latestStatut = getLatestStatut(reservation);
        return latestStatut != null && latestStatut.isAnnule();
    }
}