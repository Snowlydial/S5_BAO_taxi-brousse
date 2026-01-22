package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Diffusion;
import com.itu.taxi_brousse.service.DiffusionService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/diffusion")
@RequiredArgsConstructor
public class DiffusionController {
	private final DiffusionService diffusionService;

	@GetMapping({"/list", ""})
	public String list(Model model) {
		List<Diffusion> diffs = diffusionService.getListDiffusion();
		model.addAttribute("diffusions", diffs);
		return "diffusion/list";
	}
}
