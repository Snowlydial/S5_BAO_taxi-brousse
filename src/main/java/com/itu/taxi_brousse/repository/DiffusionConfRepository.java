package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.DiffusionConf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiffusionConfRepository extends JpaRepository<DiffusionConf, Integer> {
}
