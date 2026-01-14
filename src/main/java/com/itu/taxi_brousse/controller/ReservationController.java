package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.*;
import com.itu.taxi_brousse.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {
    
    private final ReservationService reservationService;
    private final ClientService clientService;
    private final DisponibilitePlaceService availabilityService;
    private final PricingService pricingService;
    private final PaiementService paiementService;
    
    @GetMapping
    public String listReservations(Model model) {
        // For now, we'll show all reservations
        // In a real app, you might want pagination or filters
        model.addAttribute("pageTitle", "Liste des Réservations");
        return "reservation/list";
    }
    
    @GetMapping("/new")
    public String createReservationForm(@RequestParam Integer busVoyageId, Model model) {
        // This would fetch bus voyage details and show reservation form
        // For now, we'll just show the form structure
        
        List<Client> clients = clientService.getAllClients();
        
        model.addAttribute("pageTitle", "Nouvelle Réservation");
        model.addAttribute("busVoyageId", busVoyageId);
        model.addAttribute("clients", clients);
        model.addAttribute("multiplePayment", false);
        
        return "reservation/create";
    }
    
    @PostMapping("/new")
    public String createReservation(
            @RequestParam Integer busVoyageId,
            @RequestParam Integer clientId,
            @RequestParam Integer seatNumber,
            @RequestParam(required = false) String paymentMode,
            Model model) {
        
        // This would create the reservation
        // For now, we'll just redirect to list
        
        return "redirect:/reservations";
    }
    
    @GetMapping("/{id}")
    public String viewReservation(@PathVariable Integer id, Model model) {
        // This would show reservation details
        model.addAttribute("pageTitle", "Détails de la Réservation");
        model.addAttribute("reservationId", id);
        return "reservation/details";
    }
    
    @PostMapping("/{id}/cancel")
    public String cancelReservation(@PathVariable Integer id) {
        reservationService.cancelReservation(id);
        return "redirect:/reservations";
    }
}