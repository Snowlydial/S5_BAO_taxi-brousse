package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Diffusion;
import com.itu.taxi_brousse.service.DiffusionService;
import com.itu.taxi_brousse.service.SocieteService;
import com.itu.taxi_brousse.service.BusVoyageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/diffusions")
@RequiredArgsConstructor
public class DiffusionController {
    
    private final DiffusionService diffusionService;
    private final SocieteService societeService;
    private final BusVoyageService busVoyageService;
    
    @GetMapping
    public String listDiffusions(@RequestParam(required = false) String search, Model model) {
        List<Diffusion> diffusions = diffusionService.getAllDiffusions();
        
        model.addAttribute("pageTitle", "Liste des Diffusions");
        model.addAttribute("diffusions", diffusions);
        return "diffusion/list";
    }
    
    @GetMapping("/new")
    public String createDiffusionForm(Model model) {
        model.addAttribute("pageTitle", "Nouvelle Diffusion");
        model.addAttribute("diffusion", new Diffusion());
        model.addAttribute("societes", societeService.getAllSocietes());
        model.addAttribute("busVoyages", busVoyageService.getAllBusVoyages());
        return "diffusion/create";
    }
    
    @PostMapping("/new")
    public String createDiffusion(@ModelAttribute Diffusion diffusion) {
        diffusionService.saveDiffusion(diffusion);
        return "redirect:/diffusions";
    }
    
    @GetMapping("/{id}")
    public String viewDiffusion(@PathVariable Integer id, Model model) {
        Diffusion diffusion = diffusionService.getDiffusionById(id)
                .orElseThrow(() -> new RuntimeException("Diffusion non trouvée"));
        
        model.addAttribute("pageTitle", "Détails de la Diffusion");
        model.addAttribute("diffusion", diffusion);
        return "diffusion/details";
    }
    
    @GetMapping("/{id}/edit")
    public String editDiffusionForm(@PathVariable Integer id, Model model) {
        Diffusion diffusion = diffusionService.getDiffusionById(id)
                .orElseThrow(() -> new RuntimeException("Diffusion non trouvée"));
        
        model.addAttribute("pageTitle", "Modifier la Diffusion");
        model.addAttribute("diffusion", diffusion);
        model.addAttribute("societes", societeService.getAllSocietes());
        model.addAttribute("busVoyages", busVoyageService.getAllBusVoyages());
        return "diffusion/edit";
    }
    
    @PostMapping("/{id}/edit")
    public String updateDiffusion(@PathVariable Integer id, @ModelAttribute Diffusion diffusion) {
        diffusion.setId(id);
        diffusionService.saveDiffusion(diffusion);
        return "redirect:/diffusions";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteDiffusion(@PathVariable Integer id) {
        diffusionService.deleteDiffusion(id);
        return "redirect:/diffusions";
    }
    
    @GetMapping("/by-date")
    public String getDiffusionsByDate(@RequestParam LocalDate date, Model model) {
        List<Diffusion> diffusions = diffusionService.getDiffusionsByDate(date);
        
        model.addAttribute("pageTitle", "Diffusions du " + date);
        model.addAttribute("diffusions", diffusions);
        model.addAttribute("selectedDate", date);
        return "diffusion/list";
    }
    
    @GetMapping("/by-societe/{societeId}")
    public String getDiffusionsBySociete(@PathVariable Integer societeId, Model model) {
        List<Diffusion> diffusions = diffusionService.getDiffusionsBySociete(societeId);
        
        model.addAttribute("pageTitle", "Diffusions par Société");
        model.addAttribute("diffusions", diffusions);
        return "diffusion/list";
    }
}
