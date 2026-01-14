package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.CategorieGroupeAge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategorieGroupeAgeRepository extends JpaRepository<CategorieGroupeAge, Integer> {
}