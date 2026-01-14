package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.dto.BusVoyageWithAvailability;
import com.itu.taxi_brousse.entity.Gare;
import com.itu.taxi_brousse.repository.BusClasseRepository;
import com.itu.taxi_brousse.repository.GareRepository;
import com.itu.taxi_brousse.service.BusVoyageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/busvoyage")
@RequiredArgsConstructor
public class BusVoyageController {
    
    private final BusVoyageService busVoyageService;
    private final GareRepository gareRepository;
    private final BusClasseRepository busClasseRepository;
    
    @GetMapping("/search")
    public String searchPage(Model model) {
        model.addAttribute("pageTitle", "Rechercher un Voyage");
        model.addAttribute("gares", gareRepository.findAll());
        model.addAttribute("classes", busClasseRepository.findAll());
        return "busvoyage/search";
    }
    
    @GetMapping("/search/results")
    public String searchResults(
            @RequestParam(required = false) Integer gareDepart,
            @RequestParam(required = false) Integer gareArrivee,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureMin,
            @RequestParam(required = false) Integer classeId,
            @RequestParam(required = false) Double prixMin,
            @RequestParam(required = false) Double prixMax,
            Model model) {
        
        Gare depart = gareDepart != null ? gareRepository.findById(gareDepart).orElse(null) : null;
        Gare arrivee = gareArrivee != null ? gareRepository.findById(gareArrivee).orElse(null) : null;
        
        List<BusVoyageWithAvailability> results = busVoyageService.searchWithAvailability(
            depart, arrivee, dateDepart, heureMin, classeId, prixMin, prixMax
        );
        
        model.addAttribute("pageTitle", "Résultats de Recherche");
        model.addAttribute("gares", gareRepository.findAll());
        model.addAttribute("classes", busClasseRepository.findAll());
        model.addAttribute("results", results);
        model.addAttribute("searchPerformed", true);
        
        // Keep search params
        model.addAttribute("selectedGareDepart", gareDepart);
        model.addAttribute("selectedGareArrivee", gareArrivee);
        model.addAttribute("selectedDate", dateDepart);
        model.addAttribute("selectedHeureMin", heureMin);
        model.addAttribute("selectedClasse", classeId);
        model.addAttribute("selectedPrixMin", prixMin);
        model.addAttribute("selectedPrixMax", prixMax);
        
        return "busvoyage/search";
    }
}