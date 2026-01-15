package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.BusConf;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusConfRepository extends JpaRepository<BusConf, Integer> {
    // Standard CRUD methods
}