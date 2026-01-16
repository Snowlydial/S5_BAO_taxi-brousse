package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.Client;
import com.itu.taxi_brousse.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {
    
    private final ClientRepository clientRepository;
    
    //?=== Get all clients
    public List<Client> getAllClients() {
        return clientRepository.findAllWithCategorieGroupeAge();
    }
    
    //?=== Get client by ID
    public Optional<Client> getClientById(Integer id) {
        return clientRepository.findByIdWithDetails(id);
    }
    
    //?=== Create or update client
    public Client saveClient(Client client) {
        // Ensure relationships are properly loaded
        if (client.getCategorieGenre() != null && client.getCategorieGenre().getId() != null) {
            CategorieGenre genre = categorieGenreRepository.findById(client.getCategorieGenre().getId())
                    .orElseThrow(() -> new RuntimeException("Genre non trouvé"));
            client.setCategorieGenre(genre);
        }
        
        if (client.getCategorieGroupeAge() != null && client.getCategorieGroupeAge().getId() != null) {
            CategorieGroupeAge ageGroup = categorieGroupeAgeRepository.findById(client.getCategorieGroupeAge().getId())
                    .orElseThrow(() -> new RuntimeException("Groupe d'âge non trouvé"));
            client.setCategorieGroupeAge(ageGroup);
        }
        
        return clientRepository.save(client);
    }
    
    //?=== Delete client
    public void deleteClient(Integer id) {
        clientRepository.deleteById(id);
    }
    
    //?=== Search clients by name
    public List<Client> searchClients(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllClients();
        }
        
        String searchTerm = "%" + keyword.trim() + "%";
        return clientRepository.findByNomContainingOrPrenomContaining(searchTerm, searchTerm);
    }
    
    //?=== Get clients by age group category
    public List<Client> getClientsByAgeGroup(Integer ageGroupId) {
        return clientRepository.findByCategorieGroupeAgeId(ageGroupId);
    }
    
    // Create client from data map (for API endpoint)
    @Transactional
    public Client createClientFromMap(String nom, String prenom, Integer categorieGenreId, Integer categorieGroupeAgeId) {
        Client client = new Client();
        client.setNom(nom);
        client.setPrenom(prenom);
        
        if (categorieGenreId != null) {
            client.setCategorieGenre(getCategorieGenreById(categorieGenreId));
        }
        
        if (categorieGroupeAgeId != null) {
            client.setCategorieGroupeAge(getCategorieGroupeAgeById(categorieGroupeAgeId));
        }
        
        return saveClient(client);
    }
}