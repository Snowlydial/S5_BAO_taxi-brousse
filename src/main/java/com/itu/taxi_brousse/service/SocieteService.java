package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.Societe;
import com.itu.taxi_brousse.repository.SocieteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SocieteService {
    
    private final SocieteRepository societeRepository;
    
    //?=== Get all societes
    public List<Societe> getAllSocietes() {
        return societeRepository.findAll();
    }
    
    //?=== Get societe by ID
    public Optional<Societe> getSocieteById(Integer id) {
        return societeRepository.findById(id);
    }
    
    //?=== Create or update societe
    @Transactional
    public Societe saveSociete(Societe societe) {
        return societeRepository.save(societe);
    }
    
    //?=== Delete societe
    public void deleteSociete(Integer id) {
        societeRepository.deleteById(id);
    }
    
    //?=== Search societes by name
    public List<Societe> searchSocietes(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllSocietes();
        }
        return societeRepository.findByNomContaining(keyword);
    }
}
