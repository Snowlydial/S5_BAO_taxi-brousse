package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Reservation;
import com.itu.taxi_brousse.entity.ReservationStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationStatutRepository extends JpaRepository<ReservationStatut, Integer> {
    
    @Query("SELECT rs FROM ReservationStatut rs WHERE rs.reservation = :reservation ORDER BY rs.id DESC")
    List<ReservationStatut> findByReservationOrderByIdDesc(@Param("reservation") Reservation reservation);
    
    @Query("SELECT rs FROM ReservationStatut rs WHERE rs.reservation = :reservation ORDER BY rs.id DESC LIMIT 1")
    Optional<ReservationStatut> findLatestByReservation(@Param("reservation") Reservation reservation);
    
    List<ReservationStatut> findByReservation(Reservation reservation);
}