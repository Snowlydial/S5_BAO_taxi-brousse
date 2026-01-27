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
import java.util.Map;
import java.util.HashMap;

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
        
        //*-- Calculate diffusion payment totals
        Map<Integer, Double> diffusionPaidMap = new HashMap<>();
        Map<Integer, Double> diffusionRemainingMap = new HashMap<>();
        Double totalRemainingDiffusions = 0.0;
        
        for (Facture facture : factures) {
            Double totalDue = factureService.getTotalDueForDiffusions(facture);
            Double totalPaid = factureService.getTotalPaidForDiffusions(facture);
            Double remaining = factureService.getRemainingForDiffusions(facture);
            
            //*-- DEBUG LOGGING
            System.out.println("=== FACTURE #" + facture.getId() + " - " + facture.getNumeroFacture() + " ===");
            System.out.println("Total Due:  " + totalDue);
            System.out.println("Total Paid: " + totalPaid);
            System.out.println("Remaining:  " + remaining);
            System.out.println("facture.caDiffusions: " + facture.getCaDiffusions());
            System.out.println("BusVoyage ID: " + facture.getBusVoyage().getId());
            
            diffusionPaidMap.put(facture.getId(), totalPaid != null ? totalPaid : 0.0);
            diffusionRemainingMap.put(facture.getId(), remaining != null ? remaining : 0.0);
            totalRemainingDiffusions += (remaining != null ? remaining : 0.0);
        }
        
        model.addAttribute("pageTitle", "Liste des Factures");
        model.addAttribute("factures", factures);
        model.addAttribute("totalCAReservations", totalCAReservations);
        model.addAttribute("totalCADiffusions", totalCADiffusions);
        model.addAttribute("totalGeneral", totalGeneral);
        model.addAttribute("diffusionPaidMap", diffusionPaidMap);
        model.addAttribute("diffusionRemainingMap", diffusionRemainingMap);
        model.addAttribute("totalRemainingDiffusions", totalRemainingDiffusions);
        
        return "facture/list";
    }

    // API endpoint: return current summary for paid/remaining amounts (used by AJAX polling)
    @GetMapping("/api/summary")
    @ResponseBody
    public Map<String, Object> getFactureSummary() {
        List<Facture> factures = factureService.getAllFactures();

        Double totalCAReservations = factures.stream()
            .mapToDouble(f -> f.getCaReservations() != null ? f.getCaReservations() : 0.0)
            .sum();

        Double totalCADiffusions = factures.stream()
            .mapToDouble(f -> f.getCaDiffusions() != null ? f.getCaDiffusions() : 0.0)
            .sum();

        Double totalGeneral = totalCAReservations + totalCADiffusions;

        Map<Integer, Double> diffusionPaidMap = new HashMap<>();
        Map<Integer, Double> diffusionRemainingMap = new HashMap<>();
        Double totalRemainingDiffusions = 0.0;

        for (Facture facture : factures) {
            Double totalPaid = factureService.getTotalPaidForDiffusions(facture);
            Double remaining = factureService.getRemainingForDiffusions(facture);

            diffusionPaidMap.put(facture.getId(), totalPaid != null ? totalPaid : 0.0);
            diffusionRemainingMap.put(facture.getId(), remaining != null ? remaining : 0.0);
            totalRemainingDiffusions += (remaining != null ? remaining : 0.0);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalCAReservations", totalCAReservations);
        result.put("totalCADiffusions", totalCADiffusions);
        result.put("totalGeneral", totalGeneral);
        result.put("totalRemainingDiffusions", totalRemainingDiffusions);
        result.put("diffusionPaidMap", diffusionPaidMap);
        result.put("diffusionRemainingMap", diffusionRemainingMap);

        return result;
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