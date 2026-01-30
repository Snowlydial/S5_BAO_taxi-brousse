package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.BusVoyage;
import com.itu.taxi_brousse.entity.ProduitCommande;
import com.itu.taxi_brousse.entity.ProduitSociete;
import com.itu.taxi_brousse.repository.BusVoyageRepository;
import com.itu.taxi_brousse.repository.ProduitCommandeRepository;
import com.itu.taxi_brousse.repository.ProduitSocieteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/produitcommande")
@RequiredArgsConstructor
public class ProduitCommandeController {
    
    private final ProduitCommandeRepository produitCommandeRepository;
    private final BusVoyageRepository busVoyageRepository;
    private final ProduitSocieteRepository produitSocieteRepository;

    /**
     * Display list of product commands
     */
    @GetMapping("/list")
    public String listProduitCommandes(Model model) {
        List<ProduitCommande> commandes = produitCommandeRepository.findAll();
        model.addAttribute("commandes", commandes);
        model.addAttribute("pageTitle", "Commandes Produits");
        return "produitcommande/list";
    }

    /**
     * Display create product command form
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        List<BusVoyage> busVoyages = busVoyageRepository.findAll();
        List<ProduitSociete> produitSocietes = produitSocieteRepository.findAll();
        
        model.addAttribute("busVoyages", busVoyages);
        model.addAttribute("produitSocietes", produitSocietes);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("pageTitle", "Ajouter Commande Produit");
        
        return "produitcommande/create";
    }

    /**
     * Create a new product command
     */
    @PostMapping("/create")
    public String createProduitCommande(
            @RequestParam Integer busVoyageId,
            @RequestParam Integer produitSocieteId,
            @RequestParam Integer quantite,
            @RequestParam LocalDate dateCommande,
            RedirectAttributes redirectAttributes) {
        
        try {
            BusVoyage busVoyage = busVoyageRepository.findById(busVoyageId)
                .orElseThrow(() -> new RuntimeException("Bus Voyage not found"));
            
            ProduitSociete produitSociete = produitSocieteRepository.findById(produitSocieteId)
                .orElseThrow(() -> new RuntimeException("Produit Societe not found"));
            
            // Check if command already exists
            List<ProduitCommande> existingCommandes = produitCommandeRepository.findByBusVoyage(busVoyage);
            boolean commandeExists = existingCommandes.stream()
                .anyMatch(c -> c.getProduitSociete().getId().equals(produitSocieteId));
            
            if (commandeExists) {
                redirectAttributes.addFlashAttribute("error", 
                    "Une commande pour ce produit existe déjà pour ce bus voyage.");
                return "redirect:/produitcommande/create";
            }
            
            ProduitCommande commande = ProduitCommande.builder()
                .busVoyage(busVoyage)
                .produitSociete(produitSociete)
                .quantite(quantite)
                .dateCommande(dateCommande)
                .build();
            
            produitCommandeRepository.save(commande);
            
            redirectAttributes.addFlashAttribute("success", 
                "Commande produit enregistrée avec succès!");
            return "redirect:/produitcommande/list";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de l'enregistrement: " + e.getMessage());
            return "redirect:/produitcommande/create";
        }
    }

    /**
     * Delete a product command
     */
    @PostMapping("/delete/{id}")
    public String deleteProduitCommande(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            produitCommandeRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Commande supprimée avec succès!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/produitcommande/list";
    }
}
