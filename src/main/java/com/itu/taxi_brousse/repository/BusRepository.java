package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusRepository extends JpaRepository<Bus, Integer> {
    // Optional: Find by immatriculation
    // Bus findByImmatriculation(String immatriculation);
}