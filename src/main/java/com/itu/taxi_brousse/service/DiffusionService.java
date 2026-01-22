package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.Diffusion;
import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.Societe;
import com.itu.taxi_brousse.repository.DiffusionRepository;
import com.itu.taxi_brousse.repository.BusVoyageRepository;
import com.itu.taxi_brousse.repository.SocieteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class DiffusionService {
    
    private final DiffusionRepository diffusionRepository;
    private final BusVoyageRepository busVoyageRepository;
    private final SocieteRepository societeRepository;
    
    //?=== Get all diffusions
    public List<Diffusion> getAllDiffusions() {
        return diffusionRepository.findAll();
    }
    
    //?=== Get diffusion by ID
    public Optional<Diffusion> getDiffusionById(Integer id) {
        return diffusionRepository.findById(id);
    }
    
    //?=== Create or update diffusion
    @Transactional
    public Diffusion saveDiffusion(Diffusion diffusion) {
        // Ensure relationships are properly loaded
        if (diffusion.getSociete() != null && diffusion.getSociete().getId() != null) {
            Societe societe = societeRepository.findById(diffusion.getSociete().getId())
                    .orElseThrow(() -> new RuntimeException("Société non trouvée"));
            diffusion.setSociete(societe);
        }
        
        if (diffusion.getBusVoyage() != null && diffusion.getBusVoyage().getId() != null) {
            BusVoyage busVoyage = busVoyageRepository.findById(diffusion.getBusVoyage().getId())
                    .orElseThrow(() -> new RuntimeException("Bus voyage non trouvé"));
            diffusion.setBusVoyage(busVoyage);
        }
        
        return diffusionRepository.save(diffusion);
    }
    
    //?=== Delete diffusion
    public void deleteDiffusion(Integer id) {
        diffusionRepository.deleteById(id);
    }
    
    //?=== Get diffusions by date
    public List<Diffusion> getDiffusionsByDate(LocalDate date) {
        return diffusionRepository.findByDateDiffusion(date);
    }
    
    //?=== Get diffusions by societe
    public List<Diffusion> getDiffusionsBySociete(Integer societeId) {
        return diffusionRepository.findBySocieteId(societeId);
    }
    
    //?=== Get diffusions by bus voyage
    public List<Diffusion> getDiffusionsByBusVoyage(Integer busVoyageId) {
        return diffusionRepository.findByBusVoyageId(busVoyageId);
    }
    
    //?=== Get diffusions by societe and date
    public List<Diffusion> getDiffusionsBySocieteAndDate(Integer societeId, LocalDate date) {
        return diffusionRepository.findBySocieteIdAndDateDiffusion(societeId, date);
    }
}
