package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Societe;
import com.itu.taxi_brousse.service.SocieteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/societes")
@RequiredArgsConstructor
public class SocieteController {
    
    private final SocieteService societeService;
    
    @GetMapping
    public String listSocietes(@RequestParam(required = false) String search, Model model) {
        List<Societe> societes;
        
        if (search != null && !search.trim().isEmpty()) {
            societes = societeService.searchSocietes(search);
            model.addAttribute("search", search);
        } else {
            societes = societeService.getAllSocietes();
        }
        
        model.addAttribute("pageTitle", "Liste des Sociétés");
        model.addAttribute("societes", societes);
        return "societe/list";
    }
    
    @GetMapping("/new")
    public String createSocieteForm(Model model) {
        model.addAttribute("pageTitle", "Nouvelle Société");
        model.addAttribute("societe", new Societe());
        return "societe/create";
    }
    
    @PostMapping("/new")
    public String createSociete(@ModelAttribute Societe societe) {
        societeService.saveSociete(societe);
        return "redirect:/societes";
    }
    
    @GetMapping("/{id}")
    public String viewSociete(@PathVariable Integer id, Model model) {
        Societe societe = societeService.getSocieteById(id)
                .orElseThrow(() -> new RuntimeException("Société non trouvée"));
        
        model.addAttribute("pageTitle", "Détails de la Société");
        model.addAttribute("societe", societe);
        return "societe/details";
    }
    
    @GetMapping("/{id}/edit")
    public String editSocieteForm(@PathVariable Integer id, Model model) {
        Societe societe = societeService.getSocieteById(id)
                .orElseThrow(() -> new RuntimeException("Société non trouvée"));
        
        model.addAttribute("pageTitle", "Modifier la Société");
        model.addAttribute("societe", societe);
        return "societe/edit";
    }
    
    @PostMapping("/{id}/edit")
    public String updateSociete(@PathVariable Integer id, @ModelAttribute Societe societe) {
        societe.setId(id);
        societeService.saveSociete(societe);
        return "redirect:/societes";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteSociete(@PathVariable Integer id) {
        societeService.deleteSociete(id);
        return "redirect:/societes";
    }
}
