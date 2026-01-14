package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.dto.BusVoyageWithAvailability;
import com.itu.taxi_brousse.entity.Gare;
import com.itu.taxi_brousse.entity.BusClasse;
import com.itu.taxi_brousse.service.BusVoyageService;
import com.itu.taxi_brousse.repository.GareRepository;
import com.itu.taxi_brousse.repository.BusClasseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/bus-voyages")
@RequiredArgsConstructor
public class BusVoyageController {
    
    private final BusVoyageService busVoyageService;
    private final GareRepository gareRepository;
    private final BusClasseRepository busClasseRepository;
    
    @GetMapping("/search")
    public String searchPage(Model model) {
        List<Gare> gares = gareRepository.findAll();
        List<BusClasse> classes = busClasseRepository.findAll();
        
        model.addAttribute("pageTitle", "Rechercher Bus Voyage");
        model.addAttribute("gares", gares);
        model.addAttribute("classes", classes);
        model.addAttribute("today", LocalDate.now());
        return "busvoyage/search";
    }
    
    @PostMapping("/search")
    public String search(@RequestParam(required = false) Integer gareDepartId, @RequestParam(required = false) Integer gareArriveeId,
                         @RequestParam(required = false) String dateDepartStr, @RequestParam(required = false) String heureDepartStr,
                         @RequestParam(required = false) Integer busClasseId, @RequestParam(required = false) Double prixMin,
                         @RequestParam(required = false) Double prixMax, Model model) {
        
        // Parse date and time
        LocalDate dateDepart = null;
        LocalTime heureDepart = null;
        
        if (dateDepartStr != null && !dateDepartStr.isEmpty()) {
            dateDepart = LocalDate.parse(dateDepartStr);
        }
        
        if (heureDepartStr != null && !heureDepartStr.isEmpty()) {
            heureDepart = LocalTime.parse(heureDepartStr);
        }
        
        // Get gares
        Gare gareDepart = null;
        Gare gareArrivee = null;
        
        if (gareDepartId != null) {
            gareDepart = gareRepository.findById(gareDepartId).orElse(null);
        }
        
        if (gareArriveeId != null) {
            gareArrivee = gareRepository.findById(gareArriveeId).orElse(null);
        }
        
        // Search bus voyages
        List<BusVoyageWithAvailability> results = busVoyageService.searchWithAvailability(
                gareDepart, gareArrivee, dateDepart, heureDepart, busClasseId, prixMin, prixMax);
        
        // Get all gares and classes for the form
        List<Gare> gares = gareRepository.findAll();
        List<BusClasse> classes = busClasseRepository.findAll();
        
        model.addAttribute("pageTitle", "Résultats de Recherche");
        model.addAttribute("results", results);
        model.addAttribute("gares", gares);
        model.addAttribute("classes", classes);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("selectedGareDepartId", gareDepartId);
        model.addAttribute("selectedGareArriveeId", gareArriveeId);
        model.addAttribute("selectedDateDepart", dateDepartStr);
        model.addAttribute("selectedHeureDepart", heureDepartStr);
        model.addAttribute("selectedBusClasseId", busClasseId);
        model.addAttribute("selectedPrixMin", prixMin);
        model.addAttribute("selectedPrixMax", prixMax);
        
        return "busvoyage/search";
    }
    
    @GetMapping("/{id}")
    public String viewDetails(@PathVariable Integer id, Model model) {
        // This would fetch a specific bus voyage details
        // For now, we'll just show a placeholder
        model.addAttribute("pageTitle", "Détails du Voyage");
        model.addAttribute("busVoyageId", id);
        return "busvoyage/details";
    }
}