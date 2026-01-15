package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Bus;
import com.itu.taxi_brousse.entity.BusClasse;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusRepository extends JpaRepository<Bus, Integer> {

    Optional<BusClasse> findByImmatriculation(String trim);
    // Optional: Find by immatriculation
    // Bus findByImmatriculation(String immatriculation);
}