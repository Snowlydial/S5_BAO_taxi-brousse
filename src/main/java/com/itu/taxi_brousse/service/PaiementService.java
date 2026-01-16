package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.*;
import com.itu.taxi_brousse.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaiementService {
    
    private final PaiementRepository paiementRepository;
    private final PricingService pricingService;
    
    //?=== Create single payment for a reservation with custom date
    @Transactional
    public Paiement createSinglePayment(Reservation reservation, Caisse caisse, LocalDateTime datePaiement) {
        //*-- Calculate price at payment time
        Double prixTotal = pricingService.calculatePrice(reservation.getBusVoyage());
        
        Paiement paiement = Paiement.builder()
                .datePaiement(datePaiement)
                .montantPaye(prixTotal)
                .caisse(caisse)
                .reservation(reservation)
                .build();
        
        return paiementRepository.save(paiement);
    }
    
    //?=== Create multiple payments for a reservation (split payment) with custom date
    @Transactional
    public List<Paiement> createMultiplePayments(Reservation reservation, List<Caisse> caisses, List<Double> montants, LocalDateTime datePaiement) {
        if (caisses.size() != montants.size()) {
            throw new RuntimeException("Number of payment methods must match number of amounts");
        }
        
        //*-- Calculate expected total price (now supports ClassePlace)
        Double prixTotal = pricingService.calculatePrice(reservation);
        Double totalMontant = montants.stream().mapToDouble(Double::doubleValue).sum();
        
        //*-- Validate total amount
        if (Math.abs(totalMontant - prixTotal) > 0.01) {
            throw new RuntimeException(
                String.format("Total payment %.2f does not match calculated price %.2f", 
                    totalMontant, prixTotal)
            );
        }
        
        List<Paiement> paiements = new ArrayList<>();
        
        for (int i = 0; i < caisses.size(); i++) {
            Paiement paiement = Paiement.builder()
                    .datePaiement(datePaiement)
                    .montantPaye(montants.get(i))
                    .caisse(caisses.get(i))
                    .reservation(reservation)
                    .build();
            
            paiements.add(paiement);
        }
        
        return paiementRepository.saveAll(paiements);
    }
    
    //?=== Calculate total amount paid for a reservation
    public Double getTotalPaid(Reservation reservation) {
        List<Paiement> paiements = paiementRepository.findByReservation(reservation);
        return paiements.stream()
                .mapToDouble(Paiement::getMontantPaye)
                .sum();
    }
    
    //?=== Check if reservation is fully paid
    public boolean isReservationFullyPaid(Reservation reservation) {
        Double totalPaid = getTotalPaid(reservation);
        Double prixTotal = pricingService.calculatePrice(reservation);
        
        return Math.abs(totalPaid - prixTotal) <= 0.01;
    }
    
    //?=== Calculate remaining amount to pay
    public Double getRemainingAmount(Reservation reservation) {
        Double totalPaid = getTotalPaid(reservation);
        Double prixTotal = pricingService.calculatePrice(reservation);
        
        return Math.max(0, prixTotal - totalPaid);
    }
    
    //?=== Get payments for auto-complete in UI
    public Map<String, Double> suggestSplitPayments(Reservation reservation, List<Caisse> selectedCaisses) {
        Double prixTotal = pricingService.calculatePrice(reservation);
        Map<String, Double> suggestions = new LinkedHashMap<>();
        
        if (selectedCaisses.isEmpty()) {
            return suggestions;
        }
        
        //*-- Simple equal split
        Double equalAmount = prixTotal / selectedCaisses.size();
        for (Caisse caisse : selectedCaisses) {
            suggestions.put(caisse.getLibelle(), equalAmount);
        }
        
        //*-- Adjust for rounding
        if (!suggestions.isEmpty()) {
            Double totalSuggested = suggestions.values().stream().mapToDouble(Double::doubleValue).sum();
            Double difference = prixTotal - totalSuggested;
            if (Math.abs(difference) > 0.01) {
                // Add difference to first payment method
                String firstKey = suggestions.keySet().iterator().next();
                suggestions.put(firstKey, suggestions.get(firstKey) + difference);
            }
        }
        
        return suggestions;
    }
}