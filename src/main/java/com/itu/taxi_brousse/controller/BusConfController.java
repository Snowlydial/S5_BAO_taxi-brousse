package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.BusConf;
import com.itu.taxi_brousse.repository.BusConfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/busconf")
@RequiredArgsConstructor
public class BusConfController {
    
    private final BusConfRepository busConfRepository;
    
    //?=== List all configurations
    @GetMapping("/list")
    public String listConfigurations(Model model) {
        List<BusConf> configurations = busConfRepository.findAll();
        
        model.addAttribute("pageTitle", "Gestion des Configurations Bus");
        model.addAttribute("configurations", configurations);
        
        return "busconf/list";
    }
    
    //?=== Show create form
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "Nouvelle Configuration");
        model.addAttribute("busConf", new BusConf());
        
        return "busconf/create";
    }
    
    //?=== Create configuration
    @PostMapping("/create")
    public String createConfiguration(@RequestParam String libelle,
                                      @RequestParam String valeur,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Validation
            if (libelle == null || libelle.trim().isEmpty()) {
                throw new RuntimeException("Le libellé est obligatoire");
            }
            
            if (valeur == null || valeur.trim().isEmpty()) {
                throw new RuntimeException("La valeur est obligatoire");
            }
            
            BusConf busConf = BusConf.builder()
                    .libelle(libelle.trim().toLowerCase())
                    .valeur(valeur.trim())
                    .build();
            
            busConfRepository.save(busConf);
            
            redirectAttributes.addFlashAttribute("success", "Configuration créée avec succès!");
            return "redirect:/busconf/list";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/busconf/create";
        }
    }
    
    //?=== Show edit form
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        BusConf busConf = busConfRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration introuvable"));
        
        model.addAttribute("pageTitle", "Modifier Configuration");
        model.addAttribute("busConf", busConf);
        
        return "busconf/edit";
    }
    
    //?=== Update configuration
    @PostMapping("/edit/{id}")
    public String updateConfiguration(@PathVariable Integer id,
                                      @RequestParam String libelle,
                                      @RequestParam String valeur,
                                      RedirectAttributes redirectAttributes) {
        try {
            BusConf busConf = busConfRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Configuration introuvable"));
            
            // Validation
            if (libelle == null || libelle.trim().isEmpty()) {
                throw new RuntimeException("Le libellé est obligatoire");
            }
            
            if (valeur == null || valeur.trim().isEmpty()) {
                throw new RuntimeException("La valeur est obligatoire");
            }
            
            busConf.setLibelle(libelle.trim().toLowerCase());
            busConf.setValeur(valeur.trim());
            
            busConfRepository.save(busConf);
            
            redirectAttributes.addFlashAttribute("success", "Configuration modifiée avec succès!");
            return "redirect:/busconf/list";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/busconf/edit/" + id;
        }
    }
    
    //?=== Delete configuration
    @PostMapping("/delete/{id}")
    public String deleteConfiguration(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            BusConf busConf = busConfRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Configuration introuvable"));
            
            busConfRepository.delete(busConf);
            
            redirectAttributes.addFlashAttribute("success", "Configuration supprimée avec succès!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la suppression: " + e.getMessage() + 
                ". Cette configuration est peut-être utilisée par des bus.");
        }
        
        return "redirect:/busconf/list";
    }
}