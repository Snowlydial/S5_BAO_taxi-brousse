package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.Bus;
import com.itu.taxi_brousse.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/buses")
@RequiredArgsConstructor
public class BusController {
    
    private final BusRepository busRepository;
    
    @GetMapping
    public String listBuses(Model model) {
        List<Bus> buses = busRepository.findAll();
        model.addAttribute("pageTitle", "Liste des Bus");
        model.addAttribute("buses", buses);
        return "bus/list";
    }
    
    @GetMapping("/{id}")
    public String viewBus(@PathVariable Integer id, Model model) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bus non trouvé"));
        
        model.addAttribute("pageTitle", "Détails du Bus");
        model.addAttribute("bus", bus);
        return "bus/details";
    }
}