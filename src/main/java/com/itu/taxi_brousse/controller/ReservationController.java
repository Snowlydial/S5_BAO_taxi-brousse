package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.*;
import com.itu.taxi_brousse.repository.*;
import com.itu.taxi_brousse.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {
    
    private final ReservationService reservationService;
    private final PaiementService paiementService;
    private final ClientRepository clientRepository;
    private final BusVoyageRepository busVoyageRepository;
    private final CaisseRepository caisseRepository;
    private final ReservationRepository reservationRepository;
    private final PricingService pricingService;
    private final DisponibilitePlaceService disponibilitePlaceService;
    
    @GetMapping("/list")
    public String listReservations(Model model) {
        model.addAttribute("pageTitle", "Liste des Réservations");
        model.addAttribute("reservations", reservationRepository.findAll());
        return "reservation/list";
    }
    
    @GetMapping("/create")
    public String createForm(@RequestParam Integer busVoyageId, Model model) {
        BusVoyage busVoyage = busVoyageRepository.findById(busVoyageId)
            .orElseThrow(() -> new RuntimeException("Bus voyage not found"));
        
        Double price = pricingService.calculatePrice(busVoyage);
        List<Integer> availableSeats = disponibilitePlaceService.getAvailableSeats(busVoyage);
        Integer capacity = disponibilitePlaceService.getBusCapacity(busVoyage.getBus().getId());
        
        model.addAttribute("pageTitle", "Nouvelle Réservation");
        model.addAttribute("busVoyage", busVoyage);
        model.addAttribute("price", price);
        model.addAttribute("availableSeats", availableSeats);
        model.addAttribute("capacity", capacity);
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("caisses", caisseRepository.findAll());
        
        return "reservation/create";
    }
    
    @PostMapping("/create")
    public String createReservation(
            @RequestParam Integer busVoyageId,
            @RequestParam Integer clientId,
            @RequestParam List<Integer> selectedSeats,
            @RequestParam(required = false) Boolean multiplePayment,
            @RequestParam List<Integer> caisseIds,
            @RequestParam(required = false) List<Double> montants,
            RedirectAttributes redirectAttributes) {
        
        try {
            Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
            BusVoyage busVoyage = busVoyageRepository.findById(busVoyageId)
                .orElseThrow(() -> new RuntimeException("Bus voyage not found"));
            
            // Create reservations for each selected seat
            List<Reservation> reservations = reservationService.createMultipleReservations(
                client, busVoyage, selectedSeats
            );
            
            // Process payment for each reservation
            Double pricePerSeat = pricingService.calculatePrice(busVoyage);
            
            if (Boolean.TRUE.equals(multiplePayment) && montants != null && !montants.isEmpty()) {
                // Multiple payment methods for each reservation
                for (Reservation reservation : reservations) {
                    List<Caisse> caisses = new ArrayList<>();
                    for (Integer caisseId : caisseIds) {
                        caisseRepository.findById(caisseId).ifPresent(caisses::add);
                    }
                    paiementService.createMultiplePayments(reservation, caisses, montants);
                }
            } else {
                // Single payment method for each reservation
                Caisse caisse = caisseRepository.findById(caisseIds.get(0))
                    .orElseThrow(() -> new RuntimeException("Caisse not found"));
                for (Reservation reservation : reservations) {
                    paiementService.createSinglePayment(reservation, caisse);
                }
            }
            
            redirectAttributes.addFlashAttribute("success", 
                selectedSeats.size() + " réservation(s) créée(s) avec succès!");
            return "redirect:/reservation/list";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la création: " + e.getMessage());
            return "redirect:/busvoyage/search";
        }
    }
    
    @PostMapping("/cancel/{id}")
    public String cancelReservation(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("success", "Réservation annulée avec succès!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/reservation/list";
    }
}