package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.dto.BusVoyageWithAvailability;
import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.Gare;
import com.itu.taxi_brousse.repository.BusRepository;
import com.itu.taxi_brousse.repository.BusVoyageRepository;
import com.itu.taxi_brousse.repository.GareRepository;
import com.itu.taxi_brousse.repository.VoyageRepository;
import com.itu.taxi_brousse.service.BusVoyageService;
import com.itu.taxi_brousse.service.FactureService;
import com.itu.taxi_brousse.service.PricingService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/busvoyage")
@RequiredArgsConstructor
public class BusVoyageController {
    
    private final BusRepository busRepository;
    private final VoyageRepository voyageRepository;
    private final BusVoyageRepository busVoyageRepository;
    private final GareRepository gareRepository;
    private final BusVoyageService busVoyageService;
    private final PricingService pricingService;
    private final FactureService factureService;
    
    @GetMapping("/search")
    public String searchPage(Model model) {
        int today = LocalDate.now().getYear();
        List<BusVoyageWithAvailability> defaultResults = busVoyageService.searchByYear(
            null, null, today, null, 0.00, 200000.00
        );
        defaultResults = defaultResults.stream()
            .filter(bv -> bv.getBusVoyage() == null || !factureService.hasFactureForBusVoyage(bv.getBusVoyage()))
            .toList();
        
        model.addAttribute("pageTitle", "Rechercher un Voyage");
        model.addAttribute("gares", gareRepository.findAll());
        model.addAttribute("results", defaultResults);
        model.addAttribute("searchPerformed", true);
        model.addAttribute("selectedDate", today);
        model.addAttribute("searchByYear", false);
        
        return "busvoyage/search";
    }
    
    @GetMapping("/search/results")
    public String searchResults(
            @RequestParam(required = false) Integer gareDepart,
            @RequestParam(required = false) Integer gareArrivee,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepart,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureMin,
            @RequestParam(required = false) Double prixMin,
            @RequestParam(required = false) Double prixMax,
            Model model) {
        
        Gare depart = gareDepart != null ? gareRepository.findById(gareDepart).orElse(null) : null;
        Gare arrivee = gareArrivee != null ? gareRepository.findById(gareArrivee).orElse(null) : null;
        
        List<BusVoyageWithAvailability> results;
        boolean searchByYear = false;
        
        // Determine if searching by year or specific date
        if (year != null) {
            // Search by year - get all voyages for that year
            searchByYear = true;
            results = busVoyageService.searchByYear(
                depart, arrivee, year, heureMin, prixMin, prixMax
            );
            model.addAttribute("selectedYear", year);
        } else {
            // Search by specific date
            LocalDate searchDate = dateDepart != null ? dateDepart : LocalDate.now();
            results = busVoyageService.searchWithAvailability(
                depart, arrivee, searchDate, heureMin, prixMin, prixMax
            );
            model.addAttribute("selectedDate", searchDate);
        }
        results = results.stream()
            .filter(bv -> bv.getBusVoyage() == null || !factureService.hasFactureForBusVoyage(bv.getBusVoyage()))
            .toList();
        
        model.addAttribute("pageTitle", "Résultats de Recherche");
        model.addAttribute("gares", gareRepository.findAll());
        model.addAttribute("results", results);
        model.addAttribute("searchPerformed", true);
        model.addAttribute("searchByYear", searchByYear);
        model.addAttribute("selectedGareDepart", gareDepart);
        model.addAttribute("selectedGareArrivee", gareArrivee);
        model.addAttribute("selectedHeureMin", heureMin);
        model.addAttribute("selectedPrixMin", prixMin);
        model.addAttribute("selectedPrixMax", prixMax);
        
        return "busvoyage/search";
    }

    // List all bus voyages
    @GetMapping("/manage")
    public String manageBusVoyages(Model model) {
        List<BusVoyage> busVoyages = busVoyageRepository.findAll();
        
        model.addAttribute("pageTitle", "Gestion des Bus Voyages");
        model.addAttribute("busVoyages", busVoyages);
        model.addAttribute("pricingService", pricingService);
        model.addAttribute("gares", gareRepository.findAll());
        
        return "busvoyage/manage";
    }
    
    // Show create form
    @GetMapping("/create-busvoyage")
    public String createBusVoyageForm(Model model) {
        model.addAttribute("pageTitle", "Nouveau Bus Voyage");
        model.addAttribute("buses", busRepository.findAll());
        model.addAttribute("voyages", voyageRepository.findAll());
        
        return "busvoyage/create";
    }
    
    // Create bus voyage
    @PostMapping("/create-busvoyage")
    public String createBusVoyage(@RequestParam Integer busId,
                                  @RequestParam Integer voyageId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepart,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureDepart,
                                  @RequestParam(required = false) Double prixSpecifique,
                                  RedirectAttributes redirectAttributes) {
        try {
            BusVoyage busVoyage = BusVoyage.builder()
                    .bus(busRepository.findById(busId)
                            .orElseThrow(() -> new RuntimeException("Bus introuvable")))
                    .voyage(voyageRepository.findById(voyageId)
                            .orElseThrow(() -> new RuntimeException("Voyage introuvable")))
                    .dateDepart(dateDepart)
                    .heureDepart(heureDepart)
                    .prixSpecifique(prixSpecifique)
                    .build();
            
            busVoyageService.createBusVoyage(busVoyage);
            
            redirectAttributes.addFlashAttribute("success", "Bus Voyage créé avec succès!");
            return "redirect:/busvoyage/manage";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/busvoyage/create-busvoyage";
        }
    }
    
    // Show edit form
    @GetMapping("/edit-busvoyage/{id}")
    public String editBusVoyageForm(@PathVariable Integer id, Model model) {
        BusVoyage busVoyage = busVoyageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bus Voyage introuvable"));
        
        model.addAttribute("pageTitle", "Modifier Bus Voyage");
        model.addAttribute("busVoyage", busVoyage);
        model.addAttribute("buses", busRepository.findAll());
        model.addAttribute("voyages", voyageRepository.findAll());
        
        return "busvoyage/edit";
    }
    
    // Update bus voyage
    @PostMapping("/edit-busvoyage/{id}")
    public String updateBusVoyage(@PathVariable Integer id,
                                  @RequestParam Integer busId,
                                  @RequestParam Integer voyageId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepart,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureDepart,
                                  @RequestParam(required = false) Double prixSpecifique,
                                  RedirectAttributes redirectAttributes) {
        try {
            BusVoyage busVoyage = busVoyageRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Bus Voyage introuvable"));
            
            busVoyage.setBus(busRepository.findById(busId)
                    .orElseThrow(() -> new RuntimeException("Bus introuvable")));
            busVoyage.setVoyage(voyageRepository.findById(voyageId)
                    .orElseThrow(() -> new RuntimeException("Voyage introuvable")));
            busVoyage.setDateDepart(dateDepart);
            busVoyage.setHeureDepart(heureDepart);
            busVoyage.setPrixSpecifique(prixSpecifique);
            
            busVoyageRepository.save(busVoyage);
            
            redirectAttributes.addFlashAttribute("success", "Bus Voyage modifié avec succès!");
            return "redirect:/busvoyage/manage";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/busvoyage/edit-busvoyage/" + id;
        }
    }
    
    // Delete bus voyage
    @PostMapping("/delete-busvoyage/{id}")
    public String deleteBusVoyage(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            BusVoyage busVoyage = busVoyageRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Bus Voyage introuvable"));
            
            busVoyageRepository.delete(busVoyage);
            
            redirectAttributes.addFlashAttribute("success", "Bus Voyage supprimé avec succès!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la suppression: " + e.getMessage() + 
                ". Ce Bus Voyage a peut-être des réservations.");
        }
        
        return "redirect:/busvoyage/manage";
    }
}