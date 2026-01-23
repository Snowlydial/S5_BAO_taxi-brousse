package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.DiffusionConf;
import com.itu.taxi_brousse.repository.DiffusionConfRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class DiffusionConfService {
    
    private final DiffusionConfRepository diffusionConfRepository;
    
    //?=== Get all diffusion configurations
    public List<DiffusionConf> getAllDiffusionConfs() {
        return diffusionConfRepository.findAll();
    }
    
    //?=== Get diffusion configuration by ID
    public Optional<DiffusionConf> getDiffusionConfById(Integer id) {
        return diffusionConfRepository.findById(id);
    }
    
    //?=== Create or update diffusion configuration
    @Transactional
    public DiffusionConf saveDiffusionConf(DiffusionConf diffusionConf) {
        return diffusionConfRepository.save(diffusionConf);
    }
    
    //?=== Delete diffusion configuration
    public void deleteDiffusionConf(Integer id) {
        diffusionConfRepository.deleteById(id);
    }
}
