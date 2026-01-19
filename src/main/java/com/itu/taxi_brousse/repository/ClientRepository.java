package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Client;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    // List<Client> findByNomContaining(String keyword);
    // List<Client> findByCategorieGenreId(Integer genreId);
    List<Client> findByCategorieGroupeAgeId(Integer ageGroupId);
    List<Client> findByNomContainingOrPrenomContaining(String nom, String prenom);
    Optional<Client> findById(Integer id);
}