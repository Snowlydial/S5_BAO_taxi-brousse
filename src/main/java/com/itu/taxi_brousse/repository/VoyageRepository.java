package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Voyage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoyageRepository extends JpaRepository<Voyage, Integer> {
    // Optional: Find voyages between two gares
    // List<Voyage> findByGareDepartIdAndGareArriveeId(Integer departId, Integer arriveeId);
}