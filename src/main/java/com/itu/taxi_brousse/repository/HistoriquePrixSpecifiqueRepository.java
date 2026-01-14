package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.HistoriquePrixSpecifique;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoriquePrixSpecifiqueRepository extends JpaRepository<HistoriquePrixSpecifique, Integer> {
    List<HistoriquePrixSpecifique> findByBusVoyageOrderByDateEcritureDesc(BusVoyage busVoyage);
    
}
