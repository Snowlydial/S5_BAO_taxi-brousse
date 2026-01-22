package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.DiffusionPaiement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiffusionPaiementRepository extends JpaRepository<DiffusionPaiement, Integer> {
}
