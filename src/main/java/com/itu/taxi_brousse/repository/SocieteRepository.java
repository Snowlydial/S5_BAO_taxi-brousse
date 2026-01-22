package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Societe;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocieteRepository extends JpaRepository<Societe, Integer> {
    List<Societe> findByNomSocieteContaining(String keyword);
}
