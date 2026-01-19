package com.itu.taxi_brousse.controller.api;

import com.itu.taxi_brousse.entity.Client;
import com.itu.taxi_brousse.service.ClientService;
import com.itu.taxi_brousse.repository.CategorieGenreRepository;
import com.itu.taxi_brousse.repository.CategorieGroupeAgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientApiController {
    
    private final ClientService clientService;
    private final CategorieGenreRepository categorieGenreRepository;
    private final CategorieGroupeAgeRepository categorieGroupeAgeRepository;
    
    @PostMapping("/create")
    public ResponseEntity<Client> createClient(@RequestBody ClientCreateRequest request) {
        Client client = Client.builder()
            .nom(request.getNom())
            .prenom(request.getPrenom())
            .categorieGenre(categorieGenreRepository.findById(request.getCategorieGenreId()).orElseThrow())
            .categorieGroupeAge(categorieGroupeAgeRepository.findById(request.getCategorieGroupeAgeId()).orElseThrow())
            .build();
        
        Client saved = clientService.saveClient(client);
        return ResponseEntity.ok(saved);
    }
}

@lombok.Data
class ClientCreateRequest {
    private String nom;
    private String prenom;
    private Integer categorieGenreId;
    private Integer categorieGroupeAgeId;
}