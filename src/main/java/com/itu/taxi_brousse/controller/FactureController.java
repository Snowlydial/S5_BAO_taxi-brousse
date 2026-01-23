package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Facture;
import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.service.FactureService;
import com.itu.taxi_brousse.repository.BusVoyageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/facture")
@RequiredArgsConstructor
public class FactureController {
    
    private final FactureService factureService;
    private final BusVoyageRepository busVoyageRepository;
    
    //?=== List all factures
    @GetMapping({"/list", ""})
    public String listFactures(Model model) {
        List<Facture> factures = factureService.getAllFactures();
        
        //*-- Calculate global totals
        Double totalCAReservations = factures.stream()
            .mapToDouble(f -> f.getCaReservations() != null ? f.getCaReservations() : 0.0)
            .sum();
        
        Double totalCADiffusions = factures.stream()
            .mapToDouble(f -> f.getCaDiffusions() != null ? f.getCaDiffusions() : 0.0)
            .sum();
        
        Double totalGeneral = totalCAReservations + totalCADiffusions;
        
        model.addAttribute("pageTitle", "Liste des Factures");
        model.addAttribute("factures", factures);
        model.addAttribute("totalCAReservations", totalCAReservations);
        model.addAttribute("totalCADiffusions", totalCADiffusions);
        model.addAttribute("totalGeneral", totalGeneral);
        
        return "facture/list";
    }
    
    //?=== Generate facture for a specific bus voyage
    @PostMapping("/generate/{busVoyageId}")
    public String generateFacture(@PathVariable Integer busVoyageId, 
                                 RedirectAttributes redirectAttributes) {
        try {
            BusVoyage busVoyage = busVoyageRepository.findById(busVoyageId)
                .orElseThrow(() -> new RuntimeException("Bus voyage not found"));
            
            Facture facture = factureService.generateFacture(busVoyage);
            
            redirectAttributes.addFlashAttribute("success", 
                "Facture " + facture.getNumeroFacture() + " générée avec succès!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la génération: " + e.getMessage());
        }
        
        return "redirect:/facture/list";
    }
    
    //?=== Generate all factures
    @PostMapping("/generate-all")
    public String generateAllFactures(RedirectAttributes redirectAttributes) {
        try {
            List<BusVoyage> allBusVoyages = busVoyageRepository.findAll();
            
            int count = 0;
            for (BusVoyage busVoyage : allBusVoyages) {
                factureService.generateFacture(busVoyage);
                count++;
            }
            
            redirectAttributes.addFlashAttribute("success", 
                count + " facture(s) générée(s) avec succès!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la génération: " + e.getMessage());
        }
        
        return "redirect:/facture/list";
    }
    
    //?=== View facture details
    @GetMapping("/view/{id}")
    public String viewFacture(@PathVariable Integer id, Model model) {
        Facture facture = factureService.getFactureById(id)
            .orElseThrow(() -> new RuntimeException("Facture not found"));
        
        model.addAttribute("pageTitle", "Détails Facture - " + facture.getNumeroFacture());
        model.addAttribute("facture", facture);
        
        return "facture/view";
    }
    
    //?=== Refresh facture (recalculate)
    @PostMapping("/refresh/{id}")
    public String refreshFacture(@PathVariable Integer id, 
                                RedirectAttributes redirectAttributes) {
        try {
            Facture facture = factureService.refreshFacture(id);
            
            redirectAttributes.addFlashAttribute("success", 
                "Facture " + facture.getNumeroFacture() + " actualisée avec succès!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de l'actualisation: " + e.getMessage());
        }
        
        return "redirect:/facture/list";
    }
}