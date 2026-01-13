package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.CategorieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategorieGenreRepository extends JpaRepository<CategorieGenre, Integer> {
}