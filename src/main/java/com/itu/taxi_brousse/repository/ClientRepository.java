package com.itu.taxi_brousse.repository;

import com.itu.taxi_brousse.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    // Optional custom queries
    // List<Client> findByNomContaining(String keyword);
    // List<Client> findByCategorieGenreId(Integer genreId);
}