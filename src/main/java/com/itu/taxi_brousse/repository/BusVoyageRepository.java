package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.BusVoyage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BusVoyageRepository extends JpaRepository<BusVoyage, Integer> {
    List<BusVoyage> findByDateDepart(LocalDate date);
    List<BusVoyage> findByVoyageId(Integer voyageId);
}