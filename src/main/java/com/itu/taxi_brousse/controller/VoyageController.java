package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Voyage;
import com.itu.taxi_brousse.repository.GareRepository;
import com.itu.taxi_brousse.repository.VoyageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/voyage")
@RequiredArgsConstructor
public class VoyageController {
    
    private final VoyageRepository voyageRepository;
    private final GareRepository gareRepository;
    
    //?=== List all voyages
    @GetMapping("/list")
    public String listVoyages(Model model) {
        List<Voyage> voyages = voyageRepository.findAll();
        
        model.addAttribute("pageTitle", "Gestion des Voyages");
        model.addAttribute("voyages", voyages);
        
        return "voyage/list";
    }
    
    //?=== Show create form
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "Nouveau Voyage");
        model.addAttribute("gares", gareRepository.findAll());
        model.addAttribute("voyage", new Voyage());
        
        return "voyage/create";
    }
    
    //?=== Create voyage
    @PostMapping("/create")
    public String createVoyage(@RequestParam Integer gareDepart,
                               @RequestParam Integer gareArrivee,
                               @RequestParam Double duree,
                               @RequestParam(required = false) Double prixVoyage,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validation
            if (gareDepart.equals(gareArrivee)) {
                throw new RuntimeException("La gare de départ et d'arrivée doivent être différentes");
            }
            
            if (duree <= 0) {
                throw new RuntimeException("La durée doit être supérieure à 0");
            }
            
            if (prixVoyage != null && prixVoyage < 0) {
                throw new RuntimeException("Le prix ne peut pas être négatif");
            }
            
            Voyage voyage = Voyage.builder()
                    .gareDepart(gareRepository.findById(gareDepart)
                            .orElseThrow(() -> new RuntimeException("Gare de départ introuvable")))
                    .gareArrivee(gareRepository.findById(gareArrivee)
                            .orElseThrow(() -> new RuntimeException("Gare d'arrivée introuvable")))
                    .duree(duree)
                    .prixVoyage(prixVoyage)
                    .build();
            
            voyageRepository.save(voyage);
            
            redirectAttributes.addFlashAttribute("success", "Voyage créé avec succès!");
            return "redirect:/voyage/list";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/voyage/create";
        }
    }
    
    //?=== Show edit form
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Voyage voyage = voyageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voyage introuvable"));
        
        model.addAttribute("pageTitle", "Modifier Voyage");
        model.addAttribute("gares", gareRepository.findAll());
        model.addAttribute("voyage", voyage);
        
        return "voyage/edit";
    }
    
    //?=== Update voyage
    @PostMapping("/edit/{id}")
    public String updateVoyage(@PathVariable Integer id,
                               @RequestParam Integer gareDepart,
                               @RequestParam Integer gareArrivee,
                               @RequestParam Double duree,
                               @RequestParam(required = false) Double prixVoyage,
                               RedirectAttributes redirectAttributes) {
        try {
            Voyage voyage = voyageRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Voyage introuvable"));
            
            // Validation
            if (gareDepart.equals(gareArrivee)) {
                throw new RuntimeException("La gare de départ et d'arrivée doivent être différentes");
            }
            
            if (duree <= 0) {
                throw new RuntimeException("La durée doit être supérieure à 0");
            }
            
            if (prixVoyage != null && prixVoyage < 0) {
                throw new RuntimeException("Le prix ne peut pas être négatif");
            }
            
            voyage.setGareDepart(gareRepository.findById(gareDepart)
                    .orElseThrow(() -> new RuntimeException("Gare de départ introuvable")));
            voyage.setGareArrivee(gareRepository.findById(gareArrivee)
                    .orElseThrow(() -> new RuntimeException("Gare d'arrivée introuvable")));
            voyage.setDuree(duree);
            voyage.setPrixVoyage(prixVoyage);
            
            voyageRepository.save(voyage);
            
            redirectAttributes.addFlashAttribute("success", "Voyage modifié avec succès!");
            return "redirect:/voyage/list";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/voyage/edit/" + id;
        }
    }
    
    //?=== Delete voyage
    @PostMapping("/delete/{id}")
    public String deleteVoyage(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Voyage voyage = voyageRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Voyage introuvable"));
            
            voyageRepository.delete(voyage);
            
            redirectAttributes.addFlashAttribute("success", "Voyage supprimé avec succès!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la suppression: " + e.getMessage() + 
                ". Ce voyage est peut-être utilisé dans des Bus_Voyage.");
        }
        
        return "redirect:/voyage/list";
    }
}