package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.ProduitCommande;
import com.itu.taxi_brousse.repository.ProduitCommandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProduitCommandeService {
    
    private final ProduitCommandeRepository produitCommandeRepository;
    
    //?=== Get total revenue from product orders for a specific bus voyage
    public Double getChiffreAffaireProduitBusVoyage(Integer idBusVoyage) {
        if (idBusVoyage == null) return 0.0;
        
        // This will be called with a BusVoyage entity, but the method signature expects ID
        // We'll need to find BusVoyage from repository when calling from FactureService
        return 0.0;
    }
    
    //?=== Get total revenue from product orders for a specific bus voyage (entity version)
    public Double getChiffreAffaireProduitBusVoyage(BusVoyage busVoyage) {
        if (busVoyage == null) return 0.0;
        
        List<ProduitCommande> commandes = produitCommandeRepository.findByBusVoyage(busVoyage);
        
        return commandes.stream()
            .mapToDouble(cmd -> cmd.getProduitSociete().getPrixUnitaire() * cmd.getQuantite())
            .sum();
    }
    
    //?=== Get all product orders for a bus voyage
    public List<ProduitCommande> getCommandesByBusVoyage(BusVoyage busVoyage) {
        if (busVoyage == null) return List.of();
        return produitCommandeRepository.findByBusVoyage(busVoyage);
    }
}
