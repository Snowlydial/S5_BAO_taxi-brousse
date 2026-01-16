package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.*;
import com.itu.taxi_brousse.repository.ClientRepository;
import com.itu.taxi_brousse.repository.CategorieGenreRepository;
import com.itu.taxi_brousse.repository.CategorieGroupeAgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {
    
    private final ClientRepository clientRepository;
    private final CategorieGenreRepository categorieGenreRepository;
    private final CategorieGroupeAgeRepository categorieGroupeAgeRepository;
    
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }
    
    public Optional<Client> getClientById(Integer id) {
        return clientRepository.findById(id);
    }
    
    @Transactional
    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }
    
    @Transactional
    public void deleteClient(Integer id) {
        clientRepository.deleteById(id);
    }
    
    public List<Client> searchClients(String search) {
        return clientRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(search, search);
    }
    
    // NEW: Helper methods for getting related entities
    public CategorieGenre getCategorieGenreById(Integer id) {
        return categorieGenreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre non trouvé"));
    }
    
    public CategorieGroupeAge getCategorieGroupeAgeById(Integer id) {
        return categorieGroupeAgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Groupe d'âge non trouvé"));
    }
}