package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Diffusion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiffusionRepository extends JpaRepository<Diffusion, Integer> {
    List<Diffusion> findByDateDiffusion(LocalDate dateDiffusion);
    List<Diffusion> findBySocieteId(Integer societeId);
    List<Diffusion> findByBusVoyageId(Integer busVoyageId);
    List<Diffusion> findBySocieteIdAndDateDiffusion(Integer societeId, LocalDate dateDiffusion);
}
