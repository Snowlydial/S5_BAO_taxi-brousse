package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.ProduitCommande;
import com.itu.taxi_brousse.entity.BusVoyage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitCommandeRepository extends JpaRepository<ProduitCommande, Integer> {
    
    //?=== Find all product orders for a specific bus voyage
    List<ProduitCommande> findByBusVoyage(BusVoyage busVoyage);
}
