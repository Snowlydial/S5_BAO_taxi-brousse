package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.ClasseAgeConf;
import com.itu.taxi_brousse.entity.CategorieGroupeAge;
import com.itu.taxi_brousse.entity.ClassePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClasseAgeConfRepository extends JpaRepository<ClasseAgeConf, Integer> {
    
    //?== Find price override for a specific age group and seat class on a specific date
    @Query("SELECT c FROM ClasseAgeConf c WHERE " +
           "c.categorieGroupeAge = :ageGroup AND " +
           "c.classePlace = :classePlace AND " +
           "(c.dateDebut IS NULL OR c.dateDebut <= :date) AND " +
           "(c.dateFin IS NULL OR c.dateFin >= :date)")
    Optional<ClasseAgeConf> findActiveConfig(@Param("ageGroup") CategorieGroupeAge ageGroup, @Param("classePlace") ClassePlace classePlace,
                                             @Param("date") LocalDate date);
    
    List<ClasseAgeConf> findByCategorieGroupeAge(CategorieGroupeAge categorieGroupeAge);
    
    List<ClasseAgeConf> findByClassePlace(ClassePlace classePlace);
    
    //?== Find all active configurations on a specific date
    @Query("SELECT c FROM ClasseAgeConf c WHERE " +
           "(c.dateDebut IS NULL OR c.dateDebut <= :date) AND " +
           "(c.dateFin IS NULL OR c.dateFin >= :date)")
    List<ClasseAgeConf> findAllActiveOn(@Param("date") LocalDate date);
}