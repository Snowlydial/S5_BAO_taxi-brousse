package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.CategorieGroupeAgeClassePlaceOverride;
import com.itu.taxi_brousse.entity.CategorieGroupeAge;
import com.itu.taxi_brousse.entity.ClassePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategorieGroupeAgeClassePlaceOverrideRepository extends JpaRepository<CategorieGroupeAgeClassePlaceOverride, Integer> {
    
    Optional<CategorieGroupeAgeClassePlaceOverride> findByCategorieGroupeAgeAndClassePlace(CategorieGroupeAge categorieGroupeAge, ClassePlace classePlace);
    
    Optional<CategorieGroupeAgeClassePlaceOverride> findByCategorieGroupeAge_IdAndClassePlace_Id(Integer categorieGroupeAgeId, Integer classePlaceId);
}