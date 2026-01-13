package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByClientId(Integer clientId);
    List<Reservation> findByBusVoyageId(Integer busVoyageId);
    
}