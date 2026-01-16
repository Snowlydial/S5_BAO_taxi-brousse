package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Client;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    // List<Client> findByNomContaining(String keyword);
    // List<Client> findByCategorieGenreId(Integer genreId);
    List<Client> findByCategorieGroupeAgeId(Integer ageGroupId);
    List<Client> findByNomContainingOrPrenomContaining(String nom, String prenom);

    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.categorieGroupeAge")
    List<Client> findAllWithCategorieGroupeAge();

    List<Client> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);
    
    // Get client by ID with all relationships loaded
    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.categorieGroupeAge LEFT JOIN FETCH c.categorieGenre WHERE c.id = :id")
    Optional<Client> findByIdWithDetails(Integer id);
}