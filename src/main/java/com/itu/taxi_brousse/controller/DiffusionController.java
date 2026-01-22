package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Diffusion;
import com.itu.taxi_brousse.entity.Bus;
import com.itu.taxi_brousse.entity.Voyage;
import com.itu.taxi_brousse.repository.DiffusionPaiementRepository;
import com.itu.taxi_brousse.service.DiffusionService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/diffusion")
@RequiredArgsConstructor
public class DiffusionController {
	private final DiffusionService diffusionService;
	private final DiffusionPaiementRepository diffusionPaiementRepository;

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
		model.addAttribute("dynamicPrices", dynamicPrices);
		model.addAttribute("totalDynamic", totalDynamic);
		model.addAttribute("totalPaid", totalPaid);
		model.addAttribute("uniqueBuses", uniqueBuses);
		model.addAttribute("uniqueVoyages", uniqueVoyages);

		return "diffusion/list";
	}
}
