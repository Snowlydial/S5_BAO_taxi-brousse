package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Diffusion;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiffusionRepository extends JpaRepository<Diffusion, Integer> {
    List<Diffusion> findBySocieteId(Integer idSociete);

    List<Diffusion> findByDateDiffusionBetween(LocalDate start, LocalDate end);

    @Query("SELECT d FROM Diffusion d WHERE d.heureDiffusion BETWEEN :start AND :end")
    List<Diffusion> findByHeureDiffusionBetween(@Param("start") LocalTime start, @Param("end") LocalTime end);

    List<Diffusion> findBySocieteIdOrderByIdAsc(Integer societeId);
}
