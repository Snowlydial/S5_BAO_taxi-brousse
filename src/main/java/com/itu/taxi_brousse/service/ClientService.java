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
        return clientRepository.findAll();
    }
    
    //?=== Get client by ID
    public Optional<Client> getClientById(Integer id) {
        return clientRepository.findById(id);
    }
    
    //?=== Create or update client
    public Client saveClient(Client client) {
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
}