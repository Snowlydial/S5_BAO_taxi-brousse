package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Caisse;
import com.itu.taxi_brousse.entity.Paiement;
import com.itu.taxi_brousse.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {

    List<Paiement> findByReservation(Reservation reservation);

    List<Paiement> findByDatePaiementBetween(LocalDateTime start, LocalDateTime end);
    List<Paiement> findByCaisseAndDatePaiementBetween(Caisse caisse, LocalDateTime start, LocalDateTime end);
    List<Paiement> findByReservationAndDatePaiementBetween(Reservation reservation, LocalDateTime start, LocalDateTime end);
}