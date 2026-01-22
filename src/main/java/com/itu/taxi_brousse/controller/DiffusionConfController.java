package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.DiffusionConf;
import com.itu.taxi_brousse.service.DiffusionConfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/diffusion-confs")
@RequiredArgsConstructor
public class DiffusionConfController {
    
    private final DiffusionConfService diffusionConfService;
    
    @GetMapping
    public String listDiffusionConfs(Model model) {
        List<DiffusionConf> diffusionConfs = diffusionConfService.getAllDiffusionConfs();
        
        model.addAttribute("pageTitle", "Configurations de Diffusion");
        model.addAttribute("diffusionConfs", diffusionConfs);
        return "diffusionconf/list";
    }
    
    @GetMapping("/new")
    public String createDiffusionConfForm(Model model) {
        model.addAttribute("pageTitle", "Nouvelle Configuration de Diffusion");
        model.addAttribute("diffusionConf", new DiffusionConf());
        return "diffusionconf/create";
    }
    
    @PostMapping("/new")
    public String createDiffusionConf(@ModelAttribute DiffusionConf diffusionConf) {
        diffusionConfService.saveDiffusionConf(diffusionConf);
        return "redirect:/diffusion-confs";
    }
    
    @GetMapping("/{id}")
    public String viewDiffusionConf(@PathVariable Integer id, Model model) {
        DiffusionConf diffusionConf = diffusionConfService.getDiffusionConfById(id)
                .orElseThrow(() -> new RuntimeException("Configuration de diffusion non trouvée"));
        
        model.addAttribute("pageTitle", "Détails de la Configuration");
        model.addAttribute("diffusionConf", diffusionConf);
        return "diffusionconf/details";
    }
    
    @GetMapping("/{id}/edit")
    public String editDiffusionConfForm(@PathVariable Integer id, Model model) {
        DiffusionConf diffusionConf = diffusionConfService.getDiffusionConfById(id)
                .orElseThrow(() -> new RuntimeException("Configuration de diffusion non trouvée"));
        
        model.addAttribute("pageTitle", "Modifier la Configuration");
        model.addAttribute("diffusionConf", diffusionConf);
        return "diffusionconf/edit";
    }
    
    @PostMapping("/{id}/edit")
    public String updateDiffusionConf(@PathVariable Integer id, @ModelAttribute DiffusionConf diffusionConf) {
        diffusionConf.setId(id);
        diffusionConfService.saveDiffusionConf(diffusionConf);
        return "redirect:/diffusion-confs";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteDiffusionConf(@PathVariable Integer id) {
        diffusionConfService.deleteDiffusionConf(id);
        return "redirect:/diffusion-confs";
    }
}
