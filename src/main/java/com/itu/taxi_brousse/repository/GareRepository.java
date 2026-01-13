package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Gare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GareRepository extends JpaRepository<Gare, Integer> {
}