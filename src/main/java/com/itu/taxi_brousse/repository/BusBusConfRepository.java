package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Bus;
import com.itu.taxi_brousse.entity.BusBusConf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusBusConfRepository extends JpaRepository<BusBusConf, Integer> {
    
    //?=== Find capacity configuration for a specific bus
    @Query("SELECT bbc FROM BusBusConf bbc " +
           "JOIN bbc.busConf bc " +
           "WHERE bbc.bus.id = :busId AND bc.libelle = 'capacite'")
    Optional<BusBusConf> findCapacityByBusId(@Param("busId") Integer busId);

    List<BusBusConf> findByBus(Bus bus);
    List<BusBusConf> findByBusId(Integer busId);
}