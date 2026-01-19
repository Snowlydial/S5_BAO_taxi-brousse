package com.itu.taxi_brousse.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Tableau de Bord - Statistiques");
        model.addAttribute("currentYear", LocalDate.now().getYear());
        return "dashboard";
    }

}