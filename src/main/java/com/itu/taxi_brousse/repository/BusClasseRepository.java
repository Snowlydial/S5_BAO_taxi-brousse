package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.BusClasse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusClasseRepository extends JpaRepository<BusClasse, Integer> {
}