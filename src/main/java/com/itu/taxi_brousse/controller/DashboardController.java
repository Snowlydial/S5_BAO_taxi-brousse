package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.dto.stats.*;
import com.itu.taxi_brousse.entity.Client;
import com.itu.taxi_brousse.repository.CategorieGenreRepository;
import com.itu.taxi_brousse.repository.CategorieGroupeAgeRepository;
import com.itu.taxi_brousse.service.ClientService;
import com.itu.taxi_brousse.service.StatService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    
    private final StatService statService;

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Tableau de Bord - Statistiques");
        model.addAttribute("currentYear", LocalDate.now().getYear());
        return "dashboard";
    }
    
    //?=== API endpoint for fetching stats
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<DashboardStatsDTO> getStats(@RequestParam String periodType, @RequestParam(required = false) Integer year,
                                                      @RequestParam(required = false) Integer yearMin, @RequestParam(required = false) Integer yearMax) {
        
        DashboardStatsDTO stats;
        
        if ("monthly".equals(periodType)) {
            if (year == null) {
                year = LocalDate.now().getYear();
            }
            
            stats = DashboardStatsDTO.builder()
                    .periodType("monthly")
                    .year(year)
                    .globalRevenue(statService.getMonthlyGlobalRevenue(year))
                    .revenueByCaisse(statService.getMonthlyRevenueByCaisse(year))
                    .topVoyages(statService.getMostReservedVoyages(year, year, 5))
                    .topClients(statService.getMostLucrativeClients(year, year, 10))
                    .genderUsage(statService.getUsageByGender(year, year))
                    .ageGroupUsage(statService.getUsageByAgeGroup(year, year))
                    .build();
        } else {
            if (yearMin == null) yearMin = LocalDate.now().getYear() - 5;
            if (yearMax == null) yearMax = LocalDate.now().getYear();
            
            stats = DashboardStatsDTO.builder()
                    .periodType("yearly")
                    .yearMin(yearMin)
                    .yearMax(yearMax)
                    .globalRevenue(statService.getYearlyGlobalRevenue(yearMin, yearMax))
                    .revenueByCaisse(statService.getYearlyRevenueByCaisse(yearMin, yearMax))
                    .topVoyages(statService.getMostReservedVoyages(yearMin, yearMax, 5))
                    .topClients(statService.getMostLucrativeClients(yearMin, yearMax, 10))
                    .genderUsage(statService.getUsageByGender(yearMin, yearMax))
                    .ageGroupUsage(statService.getUsageByAgeGroup(yearMin, yearMax))
                    .build();
        }
        
        return ResponseEntity.ok(stats);
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