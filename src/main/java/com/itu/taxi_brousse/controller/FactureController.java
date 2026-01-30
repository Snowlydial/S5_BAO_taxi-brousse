package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Facture;
import com.itu.taxi_brousse.dto.views.FactureTotalsView;
import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.Diffusion;
import com.itu.taxi_brousse.service.DiffusionService;
import com.itu.taxi_brousse.repository.DiffusionRepository;
import com.itu.taxi_brousse.service.FactureService;
import com.itu.taxi_brousse.repository.BusVoyageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/facture")
@RequiredArgsConstructor
public class FactureController {
    
    private final FactureService factureService;
    private final BusVoyageRepository busVoyageRepository;
    private final DiffusionRepository diffusionRepository;
    private final DiffusionService diffusionService;
    
    //?=== List all factures
    @GetMapping({"/list", ""})
    public String listFactures(Model model) {
        List<Facture> factures = factureService.getAllFactures();
        Map<Integer, FactureTotalsView> totalsMap = factureService.getFactureTotalsMap();

        Map<Integer, Double> reservationCAMap = new LinkedHashMap<>();
        Map<Integer, Double> diffusionPaidMap = new LinkedHashMap<>();
        Map<Integer, Double> diffusionRemainingMap = new LinkedHashMap<>();
        Map<Integer, Double> productCAMap = new LinkedHashMap<>();

        Double totalCAReservations = 0.0;
        Double totalCADiffusions = 0.0;
        Double totalRemainingDiffusions = 0.0;
        Double totalCAProducts = 0.0;
        Double totalGeneral = 0.0;

        for (Facture f : factures) {
            FactureTotalsView p = totalsMap.get(f.getId());

            Double caRes = p != null && p.getTotalReservations() != null ? p.getTotalReservations() : factureService.getTotalReservationsForFacture(f);
            Double paid = p != null && p.getTotalPaidDiffusions() != null ? p.getTotalPaidDiffusions() : factureService.getTotalPaidForDiffusions(f);
            Double remaining = p != null && p.getTotalRemainingDiffusions() != null ? p.getTotalRemainingDiffusions() : factureService.getRemainingForDiffusions(f);
            Double products = factureService.getTotalProductsForFacture(f);
            Double montant = p != null && p.getMontantTotal() != null ? p.getMontantTotal() : (caRes + paid + products);

            reservationCAMap.put(f.getId(), caRes != null ? caRes : 0.0);
            diffusionPaidMap.put(f.getId(), paid != null ? paid : 0.0);
            diffusionRemainingMap.put(f.getId(), remaining != null ? remaining : 0.0);
            productCAMap.put(f.getId(), products != null ? products : 0.0);

            totalCAReservations += (caRes != null ? caRes : 0.0);
            totalCADiffusions += (paid != null ? paid : 0.0);
            totalRemainingDiffusions += (remaining != null ? remaining : 0.0);
            totalCAProducts += (products != null ? products : 0.0);
            totalGeneral += (montant != null ? montant : 0.0);
        }

        model.addAttribute("pageTitle", "Liste des Factures");
        model.addAttribute("factures", factures);
        model.addAttribute("totalCAReservations", totalCAReservations);
        model.addAttribute("totalCADiffusions", totalCADiffusions);
        model.addAttribute("totalCAProducts", totalCAProducts);
        model.addAttribute("totalGeneral", totalGeneral);
        model.addAttribute("reservationCAMap", reservationCAMap);
        model.addAttribute("diffusionPaidMap", diffusionPaidMap);
        model.addAttribute("diffusionRemainingMap", diffusionRemainingMap);
        model.addAttribute("productCAMap", productCAMap);
        model.addAttribute("totalRemainingDiffusions", totalRemainingDiffusions);
        List<BusVoyage> availableBusVoyages = busVoyageRepository.findAll().stream()
            .filter(bv -> !factureService.hasFactureForBusVoyage(bv))
            .toList();
        model.addAttribute("availableBusVoyages", availableBusVoyages);
        
        return "facture/list";
    }

    //?=== View facture details
    @GetMapping("/view/{id}")
    public String viewFacture(@PathVariable Integer id, Model model) {
        Facture facture = factureService.getFactureById(id)
            .orElseThrow(() -> new RuntimeException("Facture not found"));
        
        model.addAttribute("pageTitle", "Détails Facture - " + facture.getNumeroFacture());
        model.addAttribute("facture", facture);

        // Provide full diffusion list for the facture's bus voyage and per-diffusion paid/price/remaining data
        List<Diffusion> diffusions = new ArrayList<>();
        if (facture.getBusVoyage() != null && facture.getBusVoyage().getId() != null) {
            diffusions = diffusionRepository.findByBusVoyageId(facture.getBusVoyage().getId());
        }

        Map<Integer, Double> diffusionPaidMap = new LinkedHashMap<>();
        Map<Integer, Double> diffusionPriceMap = new LinkedHashMap<>();
        Map<Integer, Double> diffusionRemainingMap = new LinkedHashMap<>();

        double diffusionTotalPrice = 0.0;
        double diffusionTotalPaid = 0.0;
        double diffusionTotalRemaining = 0.0;

        for (Diffusion d : diffusions) {
            double paid = diffusionService.getPaidAmountForDiffusion(d);
            double price = diffusionService.getPrixDiffusion(d.getDateDiffusion());
            double remaining = price - paid;
            if (remaining < 0) remaining = 0.0;

            diffusionPaidMap.put(d.getId(), paid);
            diffusionPriceMap.put(d.getId(), price);
            diffusionRemainingMap.put(d.getId(), remaining);

            diffusionTotalPrice += price;
            diffusionTotalPaid += paid;
            diffusionTotalRemaining += remaining;
        }

        // Provide computed diffusion aggregates and maps to the view
        model.addAttribute("diffusions", diffusions);
        model.addAttribute("diffusionPaidMap", diffusionPaidMap);
        model.addAttribute("diffusionPriceMap", diffusionPriceMap);
        model.addAttribute("diffusionRemainingMap", diffusionRemainingMap);
        model.addAttribute("diffusionTotalPrice", diffusionTotalPrice);
        model.addAttribute("diffusionTotalPaid", diffusionTotalPaid);
        model.addAttribute("diffusionTotalRemaining", diffusionTotalRemaining);

        // Reservation CA (assumed paid) and facture expected total (reservations + total diffusion price + products)
        double caReservations = factureService.getTotalReservationsForFacture(facture);
        double caProducts = factureService.getTotalProductsForFacture(facture);
        model.addAttribute("caReservations", caReservations);
        model.addAttribute("caDiffusions", diffusionTotalPrice);
        model.addAttribute("caProducts", caProducts);
        model.addAttribute("montantTotal", (caReservations + diffusionTotalPrice + caProducts));
        
        return "facture/view";
    }
    
    //?=== For List: Return current summary for paid/remaining amounts
    @GetMapping("/api/summary")
    @ResponseBody
    public Map<String, Object> getFactureSummary() {
        List<Facture> factures = factureService.getAllFactures();

        Map<Integer, FactureTotalsView> totalsMap = factureService.getFactureTotalsMap();

        Map<Integer, Double> reservationCAMap = new LinkedHashMap<>();
        Map<Integer, Double> diffusionPaidMap = new LinkedHashMap<>();
        Map<Integer, Double> diffusionRemainingMap = new LinkedHashMap<>();
        Map<Integer, Double> productCAMap = new LinkedHashMap<>();

        Double totalCAReservations = 0.0;
        Double totalCADiffusions = 0.0;
        Double totalRemainingDiffusions = 0.0;
        Double totalCAProducts = 0.0;
        Double totalGeneral = 0.0;

        for (Facture f : factures) {
            FactureTotalsView p = totalsMap.get(f.getId());

            Double caRes = p != null && p.getTotalReservations() != null ? p.getTotalReservations() : factureService.getTotalReservationsForFacture(f);
            Double paid = p != null && p.getTotalPaidDiffusions() != null ? p.getTotalPaidDiffusions() : factureService.getTotalPaidForDiffusions(f);
            Double remaining = p != null && p.getTotalRemainingDiffusions() != null ? p.getTotalRemainingDiffusions() : factureService.getRemainingForDiffusions(f);
            Double products = factureService.getTotalProductsForFacture(f);
            Double montant = p != null && p.getMontantTotal() != null ? p.getMontantTotal() : (caRes + paid + products);

            reservationCAMap.put(f.getId(), caRes != null ? caRes : 0.0);
            diffusionPaidMap.put(f.getId(), paid != null ? paid : 0.0);
            diffusionRemainingMap.put(f.getId(), remaining != null ? remaining : 0.0);
            productCAMap.put(f.getId(), products != null ? products : 0.0);

            totalCAReservations += (caRes != null ? caRes : 0.0);
            totalCADiffusions += (paid != null ? paid : 0.0);
            totalRemainingDiffusions += (remaining != null ? remaining : 0.0);
            totalCAProducts += (products != null ? products : 0.0);
            totalGeneral += (montant != null ? montant : 0.0);
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("reservationCAMap", reservationCAMap);
        result.put("diffusionPaidMap", diffusionPaidMap);
        result.put("diffusionRemainingMap", diffusionRemainingMap);
        result.put("productCAMap", productCAMap);
        result.put("totalCAReservations", totalCAReservations);
        result.put("totalCADiffusions", totalCADiffusions);
        result.put("totalRemainingDiffusions", totalRemainingDiffusions);
        result.put("totalCAProducts", totalCAProducts);
        result.put("totalGeneral", totalGeneral);

        return result;
    }
    
    //?=== Generate facture for a specific bus voyage
    @PostMapping("/generate/{busVoyageId}")
    public String generateFacture(@PathVariable Integer busVoyageId,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEmission,
                                 RedirectAttributes redirectAttributes) {
        try {
            BusVoyage busVoyage = busVoyageRepository.findById(busVoyageId)
                .orElseThrow(() -> new RuntimeException("Bus voyage not found"));
            
            Facture facture = factureService.generateFacture(busVoyage, dateEmission);
            
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
    
    //?=== Refresh facture (recalculate)
    @PostMapping("/refresh/{id}")
    public String refreshFacture(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
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