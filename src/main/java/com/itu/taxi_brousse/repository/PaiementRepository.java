package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Paiement;
import com.itu.taxi_brousse.entity.Reservation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {

    List<Paiement> findByReservation(Reservation reservation);

}