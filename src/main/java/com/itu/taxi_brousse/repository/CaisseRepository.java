package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Caisse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaisseRepository extends JpaRepository<Caisse, Integer> {
}