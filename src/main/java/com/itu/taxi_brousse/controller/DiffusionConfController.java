package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.DiffusionConf;
import com.itu.taxi_brousse.service.DiffusionConfService;
import com.itu.taxi_brousse.repository.DiffusionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/diffusionconf")
@RequiredArgsConstructor
public class DiffusionConfController {
    private final DiffusionConfService diffusionConfService;
    private final DiffusionRepository diffusionRepository;

    @GetMapping({"/list", ""})
    public String list(Model model) {
        List<DiffusionConf> configs = diffusionConfService.getAllDiffusionConfs();
        model.addAttribute("pageTitle", "Configuration des Tarifs - Diffusion");
        model.addAttribute("configs", configs);
        return "diffusionconf/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "Nouveau Tarif Diffusion");
        model.addAttribute("diffusionConf", new DiffusionConf());
        return "diffusionconf/create";
    }

    @PostMapping("/create")
    public String create(@RequestParam Double prix,
                        @RequestParam LocalDate dateDebut,
                        @RequestParam LocalDate dateFin,
                        RedirectAttributes redirectAttributes) {
        try {
            // Validation: prix must be > 0
            if (prix == null || prix <= 0) {
                throw new RuntimeException("Le prix doit être supérieur à 0");
            }

            // Validation: dates must be valid
            if (dateDebut == null || dateFin == null) {
                throw new RuntimeException("Les dates de début et fin sont obligatoires");
            }

            if (dateDebut.isAfter(dateFin)) {
                throw new RuntimeException("La date de début doit être avant la date de fin");
            }

            // Validation: check for overlapping date ranges
            List<DiffusionConf> existingConfigs = diffusionConfService.getAllDiffusionConfs();
            for (DiffusionConf existing : existingConfigs) {
                boolean overlaps = !(dateFin.isBefore(existing.getDateDebut()) ||
                                   dateDebut.isAfter(existing.getDateFin()));
                if (overlaps) {
                    throw new RuntimeException("Les dates chevaucheront la configuration existante du " +
                        existing.getDateDebut() + " au " + existing.getDateFin());
                }
            }

            DiffusionConf diffusionConf = DiffusionConf.builder()
                .prix(prix)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .build();

            diffusionConfService.saveDiffusionConf(diffusionConf);
            redirectAttributes.addFlashAttribute("success", "Tarif créé avec succès!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/diffusionconf/create";
        }

        return "redirect:/diffusionconf/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        try {
            DiffusionConf diffusionConf = diffusionConfService.getDiffusionConfById(id)
                .orElseThrow(() -> new RuntimeException("Tarif non trouvé"));

            model.addAttribute("pageTitle", "Modifier Tarif Diffusion");
            model.addAttribute("diffusionConf", diffusionConf);

            return "diffusionconf/edit";

        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/diffusionconf/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Integer id,
                        @RequestParam Double prix,
                        @RequestParam LocalDate dateDebut,
                        @RequestParam LocalDate dateFin,
                        RedirectAttributes redirectAttributes) {
        try {
            DiffusionConf diffusionConf = diffusionConfService.getDiffusionConfById(id)
                .orElseThrow(() -> new RuntimeException("Tarif non trouvé"));

            // Validation: prix must be > 0
            if (prix == null || prix <= 0) {
                throw new RuntimeException("Le prix doit être supérieur à 0");
            }

            // Validation: dates must be valid
            if (dateDebut == null || dateFin == null) {
                throw new RuntimeException("Les dates de début et fin sont obligatoires");
            }

            if (dateDebut.isAfter(dateFin)) {
                throw new RuntimeException("La date de début doit être avant la date de fin");
            }

            // Validation: check for overlapping date ranges (excluding self)
            List<DiffusionConf> existingConfigs = diffusionConfService.getAllDiffusionConfs();
            for (DiffusionConf existing : existingConfigs) {
                if (existing.getId().equals(id)) {
                    continue; // Skip self
                }
                boolean overlaps = !(dateFin.isBefore(existing.getDateDebut()) ||
                                   dateDebut.isAfter(existing.getDateFin()));
                if (overlaps) {
                    throw new RuntimeException("Les dates chevaucheront la configuration existante du " +
                        existing.getDateDebut() + " au " + existing.getDateFin());
                }
            }

            diffusionConf.setPrix(prix);
            diffusionConf.setDateDebut(dateDebut);
            diffusionConf.setDateFin(dateFin);

            diffusionConfService.saveDiffusionConf(diffusionConf);
            redirectAttributes.addFlashAttribute("success", "Tarif modifié avec succès!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/diffusionconf/edit/" + id;
        }

        return "redirect:/diffusionconf/list";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            DiffusionConf diffusionConf = diffusionConfService.getDiffusionConfById(id)
                .orElseThrow(() -> new RuntimeException("Tarif non trouvé"));

            // Check if any diffusions use this config
            var diffusionsUsingConfig = diffusionRepository.findByDateDiffusionBetween(
                diffusionConf.getDateDebut(),
                diffusionConf.getDateFin());

            if (!diffusionsUsingConfig.isEmpty()) {
                throw new RuntimeException("Impossible de supprimer: " + diffusionsUsingConfig.size() +
                    " diffusion(s) utilise(nt) cette configuration");
            }

            diffusionConfService.deleteDiffusionConf(id);
            redirectAttributes.addFlashAttribute("success", "Tarif supprimé avec succès!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/diffusionconf/list";
    }
}
