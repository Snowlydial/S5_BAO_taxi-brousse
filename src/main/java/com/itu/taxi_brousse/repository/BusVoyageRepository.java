package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.Gare;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BusVoyageRepository extends JpaRepository<BusVoyage, Integer> {
       List<BusVoyage> findByDateDepart(LocalDate date);
       List<BusVoyage> findByVoyageId(Integer voyageId);

       @Query("SELECT bv FROM BusVoyage bv " +
              "JOIN bv.voyage v " +
              "JOIN bv.bus b " +
              "WHERE (:#{#gareDepart == null} = true OR v.gareDepart = :gareDepart) " +
              "AND (:#{#gareArrivee == null} = true OR v.gareArrivee = :gareArrivee) " +
              "AND (:#{#dateDepart == null} = true OR bv.dateDepart = :dateDepart) " +
              "AND (:#{#heureDepartMin == null} = true OR bv.heureDepart >= :heureDepartMin)")
       List<BusVoyage> findWithFilters(@Param("gareDepart") Gare gareDepart, @Param("gareArrivee") Gare gareArrivee, 
                                          @Param("dateDepart") LocalDate dateDepart, @Param("heureDepartMin") LocalTime heureDepartMin);

       
       List<BusVoyage> findByDateDepartBetween(LocalDate start, LocalDate end);
}