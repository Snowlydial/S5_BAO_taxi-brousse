package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.ProduitSociete;
import com.itu.taxi_brousse.entity.Produit;
import com.itu.taxi_brousse.entity.Societe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitSocieteRepository extends JpaRepository<ProduitSociete, Integer> {
    
    //?=== Find all products sold by a specific society
    List<ProduitSociete> findBySociete(Societe societe);
    
    //?=== Find specific product-society combination
    Optional<ProduitSociete> findByProduitAndSociete(Produit produit, Societe societe);
}
