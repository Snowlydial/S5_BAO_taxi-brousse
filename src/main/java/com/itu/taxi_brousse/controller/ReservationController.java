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
import java.util.Map;
import java.util.HashMap;
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
    private final ClassePlaceRepository classePlaceRepository;
    
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
        
        //*-- Calculate prices for each reservation (respects ClassePlace)
        Map<Integer, Double> reservationPrices = new HashMap<>();
        for (Reservation reservation : allReservations) {
            Double price = pricingService.calculatePrice(reservation); // This is not accurate, we need to get the amount paid, not calculate based on the reservation anymore
            reservationPrices.put(reservation.getId(), price);
        }
        
        model.addAttribute("pageTitle", "Liste des Reservations");
        model.addAttribute("reservations", allReservations);
        model.addAttribute("reservationStatutService", reservationStatutService);
        model.addAttribute("reservationPrices", reservationPrices);
        model.addAttribute("uniqueBuses", uniqueBuses);
        model.addAttribute("uniqueVoyages", uniqueVoyages);
        model.addAttribute("uniqueClasses", uniqueClasses);
        
        return "reservation/list";
    }
    
    @GetMapping("/create")
    public String createForm(@RequestParam Integer busVoyageId, Model model) {
        BusVoyage busVoyage = busVoyageRepository.findById(busVoyageId)
            .orElseThrow(() -> new RuntimeException("Bus voyage not found"));
        
        List<Integer> availableSeats = disponibilitePlaceService.getAvailableSeats(busVoyage);
        Integer capacity = disponibilitePlaceService.getBusCapacity(busVoyage.getBus().getId());
        
        // Get seat class availability
        Integer availablePremium = disponibilitePlaceService.getAvailablePremiumSeats(busVoyage);
        Integer availableStandard = disponibilitePlaceService.getAvailableStandardSeats(busVoyage);
        
        // Get ClassePlace entities
        List<ClassePlace> classePlaces = classePlaceRepository.findAll();
        
        model.addAttribute("pageTitle", "Nouvelle Réservation");
        model.addAttribute("busVoyage", busVoyage);
        model.addAttribute("availableSeats", availableSeats);
        model.addAttribute("capacity", capacity);
        model.addAttribute("availablePremium", availablePremium);
        model.addAttribute("availableStandard", availableStandard);
        model.addAttribute("classePlaces", classePlaces);
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
                                    @RequestParam List<Integer> seatClasses,
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
            
            //*-- Validate seat counts
            Integer availablePremium = disponibilitePlaceService.getAvailablePremiumSeats(busVoyage);
            Integer availableStandard = disponibilitePlaceService.getAvailableStandardSeats(busVoyage);
            
            ClassePlace premiumClasse = classePlaceRepository.findByLibelleIgnoreCase("Premium").orElse(null);
            ClassePlace standardClasse = classePlaceRepository.findByLibelleIgnoreCase("Standard").orElse(null);
            
            long premiumCount = seatClasses.stream().filter(id -> premiumClasse != null && id.equals(premiumClasse.getId())).count();
            long standardCount = seatClasses.stream().filter(id -> standardClasse != null && id.equals(standardClasse.getId())).count();
            
            if (premiumCount > availablePremium) {
                throw new RuntimeException(
                    String.format("Pas assez de places Premium disponibles (%d demandées, %d disponibles)", 
                        premiumCount, availablePremium)
                );
            }
            
            if (standardCount > availableStandard) {
                throw new RuntimeException(
                    String.format("Pas assez de places Standard disponibles (%d demandées, %d disponibles)", 
                        standardCount, availableStandard)
                );
            }
            
            //*-- Create LocalDateTime for payment
            LocalTime timeToUse = heureReservation != null ? heureReservation : LocalTime.now();
            LocalDateTime datePaiement = LocalDateTime.of(dateReservation, timeToUse);
            
            // Create seat to ClassePlace mapping
            Map<Integer, ClassePlace> seatClasseMap = new HashMap<>();
            for (int i = 0; i < selectedSeats.size(); i++) {
                Integer seatNumber = selectedSeats.get(i);
                Integer classePlaceId = seatClasses.get(i);
                ClassePlace classePlace = classePlaceRepository.findById(classePlaceId)
                    .orElse(null);
                seatClasseMap.put(seatNumber, classePlace);
            }
            
            // Create reservations for each selected seat
            List<Reservation> reservations = reservationService.createMultipleReservations(
                client, busVoyage, selectedSeats, seatClasseMap
            );
            
            // Calculate total price based on ClassePlace
            Double totalPrice = reservations.stream()
                .mapToDouble(r -> pricingService.calculatePrice(r))
                .sum();
            
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
                
                Double totalPaid = validMontants.stream().mapToDouble(Double::doubleValue).sum();
                
                //*-- Validate total matches
                if (Math.abs(totalPaid - totalPrice) > 0.01) {
                    throw new RuntimeException(
                        String.format("Le montant total %.2f ne correspond pas au prix total %.2f", 
                            totalPaid, totalPrice)
                    );
                }
                
                //*-- Distribute payments proportionally across reservations based on each reservation's price
                for (Reservation reservation : reservations) {
                    //*-- Calculate this reservation's price (includes ClassePlace)
                    Double reservationPrice = pricingService.calculatePrice(reservation);
                    
                    //*-- Calculate proportional amounts for this reservation
                    List<Double> proportionalMontants = new ArrayList<>();
                    for (Double montant : validMontants) {
                        proportionalMontants.add(montant * (reservationPrice / totalPrice));
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