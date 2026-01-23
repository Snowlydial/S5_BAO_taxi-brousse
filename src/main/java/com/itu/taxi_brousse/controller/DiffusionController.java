package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Diffusion;
import com.itu.taxi_brousse.entity.Bus;
import com.itu.taxi_brousse.entity.Voyage;
import com.itu.taxi_brousse.repository.DiffusionPaiementRepository;
import com.itu.taxi_brousse.service.DiffusionService;

import lombok.RequiredArgsConstructor;
import com.itu.taxi_brousse.repository.SocieteRepository;
import com.itu.taxi_brousse.repository.BusVoyageRepository;
import com.itu.taxi_brousse.dto.BulkDiffusionRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/diffusion")
@RequiredArgsConstructor
public class DiffusionController {
	private final DiffusionService diffusionService;
	private final DiffusionPaiementRepository diffusionPaiementRepository;
	private final SocieteRepository societeRepository;
	private final BusVoyageRepository busVoyageRepository;

	@GetMapping({"/list", ""})
	public String list(Model model) {
		List<Diffusion> diffs = diffusionService.getListDiffusion();

		Double totalDynamic = diffusionService.getChiffreAffaireDiffusion(diffs);

		// Montant réellement payé
		Set<Integer> diffusionIds = diffs.stream().map(Diffusion::getId).collect(Collectors.toSet());
		double totalPaid = diffusionPaiementRepository.findAll().stream()
				.filter(p -> p.getDiffusion() != null && diffusionIds.contains(p.getDiffusion().getId()))
				.mapToDouble(p -> p.getMontantPaye() != null ? p.getMontantPaye() : 0.0)
				.sum();

		// Unique buses and voyages for client-side filters
		Set<Bus> uniqueBuses = diffs.stream()
				.filter(d -> d.getBusVoyage() != null && d.getBusVoyage().getBus() != null)
				.map(d -> d.getBusVoyage().getBus())
				.collect(Collectors.toSet());

		Set<Voyage> uniqueVoyages = diffs.stream()
				.filter(d -> d.getBusVoyage() != null && d.getBusVoyage().getVoyage() != null)
				.map(d -> d.getBusVoyage().getVoyage())
				.collect(Collectors.toSet());


		model.addAttribute("pageTitle", "Liste des Diffusions");
		model.addAttribute("diffusions", diffs);
		var dynamicPrices = diffs.stream().collect(Collectors.toMap(Diffusion::getId, d -> diffusionService.getPrixDiffusion(d.getDateDiffusion())));
		// per-diffusion paid amounts
		java.util.Map<Integer, Double> paidMap = new java.util.HashMap<>();
		for (Diffusion d : diffs) {
			paidMap.put(d.getId(), diffusionService.getPaidAmountForDiffusion(d));
		}
		model.addAttribute("paidMap", paidMap);
		model.addAttribute("dynamicPrices", dynamicPrices);
		model.addAttribute("totalDynamic", totalDynamic);
		model.addAttribute("totalPaid", totalPaid);
		model.addAttribute("uniqueBuses", uniqueBuses);
		model.addAttribute("uniqueVoyages", uniqueVoyages);
		model.addAttribute("remainingForAll", diffs.stream().mapToDouble(d -> {
			double price = dynamicPrices.get(d.getId());
			double paid = paidMap.getOrDefault(d.getId(), 0.0);
			return price - paid;
		}).sum());

		return "diffusion/list";
	}

	@GetMapping("/create-bulk")
	public String createBulkForm(Model model) {
		model.addAttribute("pageTitle", "Créer Diffusion en Masse");
		model.addAttribute("societes", societeRepository.findAll());
		model.addAttribute("busVoyages", busVoyageRepository.findAll());
		model.addAttribute("bulkRequest", new BulkDiffusionRequest());
		return "diffusion/create-bulk";
	}

	@PostMapping("/create-bulk")
	public String createBulkSubmit(@ModelAttribute BulkDiffusionRequest bulkRequest, RedirectAttributes redir) {
		try {
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
}
