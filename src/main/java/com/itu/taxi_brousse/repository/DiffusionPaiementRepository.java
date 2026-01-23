package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.DiffusionPaiement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiffusionPaiementRepository extends JpaRepository<DiffusionPaiement, Integer> {
	List<DiffusionPaiement> findByDiffusionIdIn(java.util.Collection<Integer> ids);
	List<DiffusionPaiement> findByDiffusionId(Integer id);
	List<DiffusionPaiement> findBySocieteIdAndDiffusionIsNull(Integer societeId);
}
