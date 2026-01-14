package com.itu.taxi_brousse.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class DashboardController {
    
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard - Taxi Brousse");
        return "dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        model.addAttribute("pageTitle", "Dashboard - Taxi Brousse");
        return "dashboard";
    }
}