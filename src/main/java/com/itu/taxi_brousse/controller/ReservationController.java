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
    private final CategorieGroupeAgeClassePlaceOverrideRepository overrideRepository;
    
    @GetMapping("/list")
    public String listReservations(Model model) {
        List<Reservation> allReservations = reservationRepository.findAll();
        
        //*-- Get unique values for filters
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
        
        //*-- Get ACTUAL paid amounts for each reservation
        Map<Integer, Double> reservationPaidAmounts = new HashMap<>();
        for (Reservation reservation : allReservations) {
            Double totalPaid = paiementService.getTotalPaid(reservation);
            reservationPaidAmounts.put(reservation.getId(), totalPaid);
        }
        
        model.addAttribute("pageTitle", "Liste des Reservations");
        model.addAttribute("reservations", allReservations);
        model.addAttribute("reservationStatutService", reservationStatutService);
        model.addAttribute("reservationPaidAmounts", reservationPaidAmounts);
        model.addAttribute("paiementService", paiementService);
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
        
        //*-- Get available seats by type dynamically
        Map<String, Integer> availableSeatsByType = disponibilitePlaceService.getAvailableSeatsByType(busVoyage);
        
        //*-- Get place types configured for this bus
        Map<String, Integer> busPlaceTypes = disponibilitePlaceService.getPlaceTypeCapacities(busVoyage.getBus().getId());
        
        //*-- Get all ClassePlace entities that match the bus configuration (not just available ones)
        List<ClassePlace> availableClassePlaces = classePlaceRepository.findAll().stream()
            .filter(cp -> busPlaceTypes.containsKey(cp.getLibelle()))
            .collect(Collectors.toList());
        
        //*-- Get all clients with their age category information
        List<Client> clients = clientRepository.findAllWithCategorieGroupeAge();
        
        //*-- Get all override prices for the template
        List<CategorieGroupeAgeClassePlaceOverride> allOverrides = overrideRepository.findAll();
        Map<String, Double> overridePriceMap = new HashMap<>();
        for (CategorieGroupeAgeClassePlaceOverride override : allOverrides) {
            String key = override.getCategorieGroupeAge().getId() + "_" + override.getClassePlace().getId();
            overridePriceMap.put(key, override.getPrixOverride());
        }
        
        System.out.println("=== DEBUG: Reservation Create Form ===");
        System.out.println("Bus Place Types Config: " + busPlaceTypes);
        System.out.println("Available Seats By Type: " + availableSeatsByType);
        System.out.println("All ClassePlace in DB: " + classePlaceRepository.findAll().stream()
            .map(cp -> cp.getLibelle()).collect(Collectors.toList()));
        System.out.println("ClassePlace options to show: " + availableClassePlaces.stream()
            .map(ClassePlace::getLibelle).collect(Collectors.toList()));
        System.out.println("Override prices: " + overridePriceMap);
        
        model.addAttribute("pageTitle", "Nouvelle Réservation");
        model.addAttribute("busVoyage", busVoyage);
        model.addAttribute("availableSeats", availableSeats);
        model.addAttribute("capacity", capacity);
        model.addAttribute("availableSeatsByType", availableSeatsByType);
        model.addAttribute("classePlaces", availableClassePlaces);
        model.addAttribute("clients", clients);
        model.addAttribute("overridePriceMap", overridePriceMap);
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
            
            //*-- Validate reservation date
            if (dateReservation.isAfter(busVoyage.getDateDepart())) {
                throw new RuntimeException("La date de réservation ne peut pas être après la date de départ");
            }
            
            //*-- Create seat to ClassePlace mapping
            Map<Integer, ClassePlace> seatClasseMap = new HashMap<>();
            for (int i = 0; i < selectedSeats.size(); i++) {
                Integer seatNumber = selectedSeats.get(i);
                Integer classePlaceId = seatClasses.get(i);
                ClassePlace classePlace = classePlaceRepository.findById(classePlaceId)
                    .orElseThrow(() -> new RuntimeException("ClassePlace not found"));
                seatClasseMap.put(seatNumber, classePlace);
            }
            
            //*-- Validate seat selection dynamically
            if (!disponibilitePlaceService.validateSeatSelection(busVoyage, seatClasseMap)) {
                throw new RuntimeException("Sélection de places invalide - certains types de places ne sont plus disponibles");
            }
            
            //*-- Create payment datetime
            LocalTime timeToUse = heureReservation != null ? heureReservation : LocalTime.now();
            LocalDateTime datePaiement = LocalDateTime.of(dateReservation, timeToUse);
            
            //*-- Create reservations
            List<Reservation> reservations = reservationService.createMultipleReservations(
                client, busVoyage, selectedSeats, seatClasseMap
            );
            
            //*-- Calculate total price (PricingService will handle age group discounts)
            Double totalPrice = reservations.stream()
                .mapToDouble(r -> pricingService.calculatePrice(r))
                .sum();
            
            //*-- Check for any discounted seats
            boolean hasDiscountedSeats = reservations.stream()
                .anyMatch(r -> {
                    if (r.getClient() != null && r.getClient().getCategorieGroupeAge() != null && 
                        r.getClassePlace() != null) {
                        return overrideRepository.findByCategorieGroupeAgeAndClassePlace(
                            r.getClient().getCategorieGroupeAge(),
                            r.getClassePlace()
                        ).isPresent();
                    }
                    return false;
                });
            
            //*-- Clean up payment inputs
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
            
            //*-- Process payments
            if (Boolean.TRUE.equals(multiplePayment) && validMontants.size() > 1) {
                // Multiple payment methods
                List<Caisse> caisses = new ArrayList<>();
                for (Integer caisseId : validCaisseIds) {
                    caisseRepository.findById(caisseId).ifPresent(caisses::add);
                }
                
                Double totalPaid = validMontants.stream().mapToDouble(Double::doubleValue).sum();
                
                if (Math.abs(totalPaid - totalPrice) > 0.01) {
                    throw new RuntimeException(
                        String.format("Le montant total %.2f ne correspond pas au prix total %.2f", 
                            totalPaid, totalPrice)
                    );
                }
                
                //*-- Distribute payments proportionally
                for (Reservation reservation : reservations) {
                    Double reservationPrice = pricingService.calculatePrice(reservation);
                    List<Double> proportionalMontants = new ArrayList<>();
                    for (Double montant : validMontants) {
                        proportionalMontants.add(montant * (reservationPrice / totalPrice));
                    }
                    paiementService.createMultiplePayments(reservation, caisses, proportionalMontants, datePaiement);
                }
            } else {
                // Single payment method
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
            
            String successMessage = selectedSeats.size() + " réservation(s) créée(s) avec succès!";
            if (hasDiscountedSeats) {
                successMessage += " (Tarifs spéciaux appliqués selon catégorie d'âge)";
            }
            
            redirectAttributes.addFlashAttribute("success", successMessage);
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