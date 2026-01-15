package com.itu.taxi_brousse.controller;

import com.itu.taxi_brousse.entity.*;
import com.itu.taxi_brousse.repository.*;
import com.itu.taxi_brousse.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {
    
    private final ReservationService reservationService;
    private final PaiementService paiementService;
    private final ReservationStatutService reservationStatutService;
    private final ClientRepository clientRepository;
    private final BusVoyageRepository busVoyageRepository;
    private final CaisseRepository caisseRepository;
    private final ReservationRepository reservationRepository;
    private final PricingService pricingService;
    private final DisponibilitePlaceService disponibilitePlaceService;
    private final CategorieGenreRepository categorieGenreRepository;
    private final CategorieGroupeAgeRepository categorieGroupeAgeRepository;
    
    @GetMapping("/list")
    public String listReservations(Model model) {
        List<Reservation> allReservations = reservationRepository.findAll();
        
        //*-- Get unique values for filters (avoiding duplicates)
        List<Bus> uniqueBuses = allReservations.stream()
            .map(r -> r.getBusVoyage().getBus())
            .distinct()
            .sorted((b1, b2) -> b1.getImmatriculation().compareTo(b2.getImmatriculation()))
            .collect(Collectors.toList());
        
        List<Voyage> uniqueVoyages = allReservations.stream()
            .map(r -> r.getBusVoyage().getVoyage())
            .distinct()
            .collect(Collectors.toList());
        
        List<BusClasse> uniqueClasses = allReservations.stream()
            .map(r -> r.getBusVoyage().getBus().getBusClasse())
            .distinct()
            .sorted((c1, c2) -> c1.getLibelle().compareTo(c2.getLibelle()))
            .collect(Collectors.toList());
        
        model.addAttribute("pageTitle", "Liste des Reservations");
        model.addAttribute("reservations", allReservations);
        model.addAttribute("reservationStatutService", reservationStatutService);
        model.addAttribute("uniqueBuses", uniqueBuses);
        model.addAttribute("uniqueVoyages", uniqueVoyages);
        model.addAttribute("uniqueClasses", uniqueClasses);
        
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
        model.addAttribute("genres", categorieGenreRepository.findAll());
        model.addAttribute("groupesAge", categorieGroupeAgeRepository.findAll());
        
        return "reservation/create";
    }
    
    @PostMapping("/create")
    public String createReservation(@RequestParam Integer busVoyageId, 
                                    @RequestParam Integer clientId,
                                    @RequestParam List<Integer> selectedSeats, 
                                    @RequestParam LocalDate dateReservation,
                                    @RequestParam(required = false) LocalTime heureReservation,
                                    @RequestParam(required = false) Boolean multiplePayment,
                                    @RequestParam(required = false) List<Integer> caisseIds, 
                                    @RequestParam(required = false) List<Double> montants,
                                    RedirectAttributes redirectAttributes) {
        
        try {
            Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
            BusVoyage busVoyage = busVoyageRepository.findById(busVoyageId)
                .orElseThrow(() -> new RuntimeException("Bus voyage not found"));
            
            //*-- Validate reservation date is before departure date
            if (dateReservation.isAfter(busVoyage.getDateDepart())) {
                throw new RuntimeException("La date de réservation ne peut pas être après la date de départ");
            }
            
            //*-- Create LocalDateTime for payment
            LocalTime timeToUse = heureReservation != null ? heureReservation : LocalTime.now();
            LocalDateTime datePaiement = LocalDateTime.of(dateReservation, timeToUse);
            
            // Create reservations for each selected seat
            List<Reservation> reservations = reservationService.createMultipleReservations(
                client, busVoyage, selectedSeats
            );
            
            // Process payment for each reservation
            Double pricePerSeat = pricingService.calculatePrice(busVoyage);
            
            // Clean up caisseIds and montants - remove nulls and empty values
            List<Integer> validCaisseIds = new ArrayList<>();
            List<Double> validMontants = new ArrayList<>();
            
            if (caisseIds != null) {
                for (int i = 0; i < caisseIds.size(); i++) {
                    Integer caisseId = caisseIds.get(i);
                    if (caisseId != null && caisseId > 0) {
                        validCaisseIds.add(caisseId);
                        if (montants != null && i < montants.size()) {
                            validMontants.add(montants.get(i));
                        }
                    }
                }
            }
            
            if (Boolean.TRUE.equals(multiplePayment) && validMontants.size() > 1) {
                // Multiple payment methods - split across ALL reservations
                List<Caisse> caisses = new ArrayList<>();
                for (Integer caisseId : validCaisseIds) {
                    caisseRepository.findById(caisseId).ifPresent(caisses::add);
                }
                
                //*-- Calculate total price for all seats
                Double totalPrice = pricePerSeat * reservations.size();
                Double totalPaid = validMontants.stream().mapToDouble(Double::doubleValue).sum();
                
                //*-- Validate total matches
                if (Math.abs(totalPaid - totalPrice) > 0.01) {
                    throw new RuntimeException(
                        String.format("Le montant total %.2f ne correspond pas au prix total %.2f", 
                            totalPaid, totalPrice)
                    );
                }
                
                //*-- Distribute payments proportionally across reservations
                for (int i = 0; i < reservations.size(); i++) {
                    Reservation reservation = reservations.get(i);
                    
                    //*-- Each reservation gets the split payment proportionally
                    List<Double> proportionalMontants = new ArrayList<>();
                    for (Double montant : validMontants) {
                        proportionalMontants.add(montant / reservations.size());
                    }
                    
                    paiementService.createMultiplePayments(reservation, caisses, proportionalMontants, datePaiement);
                }
            } else {
                // Single payment method for each reservation
                Integer caisseId = validCaisseIds.isEmpty() ? null : validCaisseIds.get(0);
                if (caisseId == null) {
                    throw new RuntimeException("Aucun mode de paiement sélectionné");
                }
                Caisse caisse = caisseRepository.findById(caisseId)
                    .orElseThrow(() -> new RuntimeException("Caisse not found"));
                for (Reservation reservation : reservations) {
                    paiementService.createSinglePayment(reservation, caisse, datePaiement);
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
    public String cancelReservation(@PathVariable Integer id, 
                                     @RequestParam LocalDate dateAnnulation,
                                     RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(id, dateAnnulation);
            redirectAttributes.addFlashAttribute("success", "Réservation annulée avec succès!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/reservation/list";
    }
}