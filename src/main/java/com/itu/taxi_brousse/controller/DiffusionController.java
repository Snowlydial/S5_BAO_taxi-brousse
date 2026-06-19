package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Diffusion;
import com.itu.taxi_brousse.entity.Bus;
import com.itu.taxi_brousse.entity.Voyage;
import com.itu.taxi_brousse.service.DiffusionService;
import com.itu.taxi_brousse.service.FactureService;

import lombok.RequiredArgsConstructor;
import com.itu.taxi_brousse.repository.SocieteRepository;
import com.itu.taxi_brousse.repository.BusVoyageRepository;
import com.itu.taxi_brousse.dto.BulkDiffusionRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/diffusion")
@RequiredArgsConstructor
public class DiffusionController {
	private final DiffusionService diffusionService;
	private final SocieteRepository societeRepository;
	private final BusVoyageRepository busVoyageRepository;
	private final FactureService factureService;

	@GetMapping({"/list", ""})
    public String list(Model model) {
        List<Diffusion> diffs = diffusionService.getListDiffusion();

        //*-- Get unique values for filters
        Set<Bus> uniqueBuses = diffs.stream()
            .filter(d -> d.getBusVoyage() != null && d.getBusVoyage().getBus() != null)
            .map(d -> d.getBusVoyage().getBus())
            .collect(Collectors.toSet());

        Set<Voyage> uniqueVoyages = diffs.stream()
            .filter(d -> d.getBusVoyage() != null && d.getBusVoyage().getVoyage() != null)
            .map(d -> d.getBusVoyage().getVoyage())
            .collect(Collectors.toSet());

        //*-- Get DYNAMIC prices (Chiffre d'Affaires - using current config)
        Map<Integer, Double> dynamicPrices = new HashMap<>();
        for (Diffusion d : diffs) {
            dynamicPrices.put(d.getId(), diffusionService.getPrixDiffusion(d.getDateDiffusion()));
        }

        //*-- Get ACTUAL paid amounts (Montant Encaissé)
        Map<Integer, Double> paidMap = new HashMap<>();
        for (Diffusion d : diffs) {
            paidMap.put(d.getId(), diffusionService.getPaidAmountForDiffusion(d));
        }

        //*-- Calculate totals
        Double totalDynamic = dynamicPrices.values().stream().mapToDouble(Double::doubleValue).sum();
        Double totalPaid = paidMap.values().stream().mapToDouble(Double::doubleValue).sum();
        Double remainingForAll = totalDynamic - totalPaid;

        model.addAttribute("pageTitle", "Liste des Diffusions");
        model.addAttribute("diffusions", diffs);
        model.addAttribute("dynamicPrices", dynamicPrices);
        model.addAttribute("paidMap", paidMap);
        model.addAttribute("totalDynamic", totalDynamic);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("remainingForAll", remainingForAll);
        model.addAttribute("uniqueBuses", uniqueBuses);
        model.addAttribute("uniqueVoyages", uniqueVoyages);

        return "diffusion/list";
    }

	@GetMapping("/create-bulk")
	public String createBulkForm(Model model) {
		model.addAttribute("pageTitle", "Créer Diffusion en Masse");
		model.addAttribute("societes", societeRepository.findAll());
		model.addAttribute("busVoyages", busVoyageRepository.findAll().stream()
			.filter(bv -> !factureService.hasFactureForBusVoyage(bv))
			.toList());
		model.addAttribute("bulkRequest", new BulkDiffusionRequest());
		return "diffusion/create-bulk";
	}

	@PostMapping("/create-bulk")
	public String createBulkSubmit(@ModelAttribute BulkDiffusionRequest bulkRequest, RedirectAttributes redir) {
		try {
			if (bulkRequest.getBusVoyageId() != null) {
				busVoyageRepository.findById(bulkRequest.getBusVoyageId()).ifPresent(bv -> {
					if (factureService.hasFactureForBusVoyage(bv)) {
						throw new RuntimeException("Impossible d'ajouter une diffusion: la facture pour ce voyage est déjà générée (verrouillé).");
					}
				});
			}
			diffusionService.createBulkDiffusions(bulkRequest);
			redir.addFlashAttribute("success", "Bulk diffusions created successfully.");
		} catch (Exception e) {
			redir.addFlashAttribute("error", "Error: " + e.getMessage());
		}
		return "redirect:/diffusion/list";
	}

	@GetMapping("/regulate")
	public String regulateForm(@RequestParam(value = "societeId", required = false) Integer societeId, Model model) {
		model.addAttribute("pageTitle", "Régler Diffusions");
		model.addAttribute("societes", societeRepository.findAll());
		if (societeId != null) {
			double remaining = diffusionService.getRemainingForSociety(societeId);
			model.addAttribute("remainingInfo", remaining);
		}
		return "diffusion/regulate";
	}

	@PostMapping("/regulate")
	public String regulateSubmit(@RequestParam("societeId") Integer societeId,
								 @RequestParam("amount") Double amount,
								 RedirectAttributes redir) {
		try {
			diffusionService.applyPaymentToSociety(societeId, amount);
			redir.addFlashAttribute("success", "Paiement appliqué avec succès.");
		} catch (Exception e) {
			redir.addFlashAttribute("error", "Erreur: " + e.getMessage());
		}
		return "redirect:/diffusion/regulate";
	}

	@GetMapping("/api/remaining")
	public ResponseEntity<Map<String, Double>> getRemaining(
			@RequestParam(value = "societeId", required = false) Integer societeId) {
		if (societeId == null) {
			return ResponseEntity.ok(Map.of("remaining", 0.0));
		}
		double remaining = diffusionService.getRemainingForSociety(societeId);
		return ResponseEntity.ok(Map.of("remaining", remaining));
	}
}
