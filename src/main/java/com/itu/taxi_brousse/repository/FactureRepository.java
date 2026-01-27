package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Facture;
import com.itu.taxi_brousse.entity.BusVoyage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Integer> {
    
    Optional<Facture> findByBusVoyage(BusVoyage busVoyage);
    
    List<Facture> findAllByOrderByDateEmissionDesc();
    
    List<Facture> findAllByOrderByIdDesc();
    
    Optional<Facture> findByNumeroFacture(String numeroFacture);
}