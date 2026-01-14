package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Client;
import com.itu.taxi_brousse.repository.CategorieGenreRepository;
import com.itu.taxi_brousse.repository.CategorieGroupeAgeRepository;
import com.itu.taxi_brousse.service.ClientService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class DashboardController {
    
    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Tableau de Bord");
        return "dashboard";
    }
}

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
class ClientApiController {
    
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