package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.ClasseAgeConf;
import com.itu.taxi_brousse.entity.CategorieGroupeAge;
import com.itu.taxi_brousse.entity.ClassePlace;
import com.itu.taxi_brousse.entity.Voyage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClasseAgeConfRepository extends JpaRepository<ClasseAgeConf, Integer> {
    
    //?== Find price override for a specific voyage, age group and seat class on a specific date
    @Query("SELECT c FROM ClasseAgeConf c WHERE " +
           "c.voyage = :voyage AND " +
           "c.categorieGroupeAge = :ageGroup AND " +
           "c.classePlace = :classePlace AND " +
           "(c.dateDebut IS NULL OR c.dateDebut <= :date) AND " +
           "(c.dateFin IS NULL OR c.dateFin >= :date)")
    Optional<ClasseAgeConf> findActiveConfigByVoyage(
        @Param("voyage") Voyage voyage,
        @Param("ageGroup") CategorieGroupeAge ageGroup,
        @Param("classePlace") ClassePlace classePlace,
        @Param("date") LocalDate date
    );
    
    //?== Find all active configs for a specific voyage on a specific date
    @Query("SELECT c FROM ClasseAgeConf c WHERE " +
           "c.voyage = :voyage AND " +
           "(c.dateDebut IS NULL OR c.dateDebut <= :date) AND " +
           "(c.dateFin IS NULL OR c.dateFin >= :date)")
    List<ClasseAgeConf> findAllActiveConfigsByVoyage(
        @Param("voyage") Voyage voyage,
        @Param("date") LocalDate date
    );
    
    //?== LEGACY: Find price override without voyage (for backward compatibility)
    @Query("SELECT c FROM ClasseAgeConf c WHERE " +
           "c.voyage IS NULL AND " +
           "c.categorieGroupeAge = :ageGroup AND " +
           "c.classePlace = :classePlace AND " +
           "(c.dateDebut IS NULL OR c.dateDebut <= :date) AND " +
           "(c.dateFin IS NULL OR c.dateFin >= :date)")
    Optional<ClasseAgeConf> findActiveConfigWithoutVoyage(
        @Param("ageGroup") CategorieGroupeAge ageGroup,
        @Param("classePlace") ClassePlace classePlace,
        @Param("date") LocalDate date
    );
    
    List<ClasseAgeConf> findByCategorieGroupeAge(CategorieGroupeAge categorieGroupeAge);
    
    List<ClasseAgeConf> findByClassePlace(ClassePlace classePlace);
    
    List<ClasseAgeConf> findByVoyage(Voyage voyage);
    
    //?== Find all active configurations on a specific date
    @Query("SELECT c FROM ClasseAgeConf c WHERE " +
           "(c.dateDebut IS NULL OR c.dateDebut <= :date) AND " +
           "(c.dateFin IS NULL OR c.dateFin >= :date)")
    List<ClasseAgeConf> findAllActiveOn(@Param("date") LocalDate date);
}