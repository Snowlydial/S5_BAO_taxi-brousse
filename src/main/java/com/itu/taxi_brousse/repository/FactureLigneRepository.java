package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.FactureLigne;
import com.itu.taxi_brousse.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactureLigneRepository extends JpaRepository<FactureLigne, Integer> {
    
    List<FactureLigne> findByFacture(Facture facture);
    
    List<FactureLigne> findByFactureAndTypeLigne(Facture facture, String typeLigne);
}