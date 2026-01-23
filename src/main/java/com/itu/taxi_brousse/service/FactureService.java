package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.*;
import com.itu.taxi_brousse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FactureService {
    
    private final FactureRepository factureRepository;
    private final FactureLigneRepository factureLigneRepository;
    private final ReservationRepository reservationRepository;
    private final DiffusionRepository diffusionRepository;
    private final PricingService pricingService;
    private final DiffusionService diffusionService;
    
    //?=== Generate facture for a specific Bus_Voyage
    @Transactional
    public Facture generateFacture(BusVoyage busVoyage) {
        //*-- Check if facture already exists
        Optional<Facture> existing = factureRepository.findByBusVoyage(busVoyage);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        //*-- Generate unique numero facture
        String numeroFacture = generateNumeroFacture(busVoyage);
        
        //*-- Create facture mere
        Facture facture = Facture.builder()
            .numeroFacture(numeroFacture)
            .dateEmission(LocalDate.now())
            .busVoyage(busVoyage)
            .caReservations(0.0)
            .caDiffusions(0.0)
            .montantTotal(0.0)
            .build();
        
        facture = factureRepository.save(facture);
        
        //*-- Create facture lignes for reservations (use CA from pricing)
        List<Reservation> reservations = reservationRepository.findByBusVoyage(busVoyage);
        for (Reservation reservation : reservations) {
            Double caPrice = pricingService.calculatePrice(reservation, busVoyage.getDateDepart());
            
            FactureLigne ligne = FactureLigne.builder()
                .typeLigne("RESERVATION")
                .montant(caPrice)
                .description("Place " + reservation.getNumeroPlace() + 
                           " - " + reservation.getClient().getNom() + " " + reservation.getClient().getPrenom())
                .facture(facture)
                .reservation(reservation)
                .build();
            
            factureLigneRepository.save(ligne);
            facture.getLignes().add(ligne);
        }
        
        //*-- Create facture lignes for diffusions (use DiffusionPaiement)
        List<Diffusion> diffusions = diffusionRepository.findByBusVoyageId(busVoyage.getId());
        for (Diffusion diffusion : diffusions) {
            //*-- Get total paid for this diffusion from DiffusionPaiement
            Double paidAmount = diffusionService.getPaidAmountForDiffusion(diffusion);
            
            if (paidAmount > 0) {
                FactureLigne ligne = FactureLigne.builder()
                    .typeLigne("DIFFUSION")
                    .montant(paidAmount)
                    .description("Diffusion - " + diffusion.getSociete().getNom() + 
                               " (" + diffusion.getDescription() + ")")
                    .facture(facture)
                    .diffusion(diffusion)
                    .build();
                
                factureLigneRepository.save(ligne);
                facture.getLignes().add(ligne);
            }
        }
        
        //*-- Calculate totals
        facture.calculateTotals();
        
        return factureRepository.save(facture);
    }
    
    //?=== Generate factures for all Bus_Voyages
    @Transactional
    public List<Facture> generateAllFactures() {
        List<BusVoyage> allBusVoyages = factureRepository.findAll().stream()
            .map(Facture::getBusVoyage)
            .toList();
        
        return allBusVoyages.stream()
            .map(this::generateFacture)
            .toList();
    }
    
    //?=== Get all factures ordered by date
    public List<Facture> getAllFactures() {
        return factureRepository.findAllByOrderByDateEmissionDesc();
    }
    
    //?=== Get total paid amount for diffusions in a facture
    public Double getTotalPaidForDiffusions(Facture facture) {
        List<Diffusion> diffusions = diffusionRepository.findByBusVoyageId(facture.getBusVoyage().getId());
        return diffusions.stream()
            .mapToDouble(diffusionService::getPaidAmountForDiffusion)
            .sum();
    }
    
    //?=== Get total due amount for diffusions in a facture
    public Double getTotalDueForDiffusions(Facture facture) {
        List<Diffusion> diffusions = diffusionRepository.findByBusVoyageId(facture.getBusVoyage().getId());
        return diffusions.stream()
            .mapToDouble(d -> diffusionService.getPrixDiffusion(d.getDateDiffusion()))
            .sum();
    }
    
    //?=== Get remaining amount for diffusions in a facture
    public Double getRemainingForDiffusions(Facture facture) {
        return getTotalDueForDiffusions(facture) - getTotalPaidForDiffusions(facture);
    }
    
    //?=== Get facture by ID
    public Optional<Facture> getFactureById(Integer id) {
        return factureRepository.findById(id);
    }
    
    //?=== Refresh facture (recalculate all lignes)
    @Transactional
    public Facture refreshFacture(Integer factureId) {
        Facture facture = factureRepository.findById(factureId)
            .orElseThrow(() -> new RuntimeException("Facture not found"));
        
        //*-- Delete existing lignes
        factureLigneRepository.deleteAll(facture.getLignes());
        facture.getLignes().clear();
        
        //*-- Recreate all lignes
        BusVoyage busVoyage = facture.getBusVoyage();
        
        //*-- Reservations
        List<Reservation> reservations = reservationRepository.findByBusVoyage(busVoyage);
        for (Reservation reservation : reservations) {
            Double caPrice = pricingService.calculatePrice(reservation, busVoyage.getDateDepart());
            
            FactureLigne ligne = FactureLigne.builder()
                .typeLigne("RESERVATION")
                .montant(caPrice)
                .description("Place " + reservation.getNumeroPlace() + 
                           " - " + reservation.getClient().getNom() + " " + reservation.getClient().getPrenom())
                .facture(facture)
                .reservation(reservation)
                .build();
            
            factureLigneRepository.save(ligne);
            facture.getLignes().add(ligne);
        }
        
        //*-- Diffusions
        List<Diffusion> diffusions = diffusionRepository.findByBusVoyageId(busVoyage.getId());
        for (Diffusion diffusion : diffusions) {
            Double paidAmount = diffusionService.getPaidAmountForDiffusion(diffusion);
            
            if (paidAmount > 0) {
                FactureLigne ligne = FactureLigne.builder()
                    .typeLigne("DIFFUSION")
                    .montant(paidAmount)
                    .description("Diffusion - " + diffusion.getSociete().getNom() + 
                               " (" + diffusion.getDescription() + ")")
                    .facture(facture)
                    .diffusion(diffusion)
                    .build();
                
                factureLigneRepository.save(ligne);
                facture.getLignes().add(ligne);
            }
        }
        
        facture.calculateTotals();
        return factureRepository.save(facture);
    }
    
    //?=== Generate unique numero facture
    private String generateNumeroFacture(BusVoyage busVoyage) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = busVoyage.getDateDepart().format(formatter);
        String busImmat = busVoyage.getBus().getImmatriculation().replace(" ", "");
        
        //*-- Format: FAC-YYYYMMDD-IMMAT-SEQ
        long count = factureRepository.count() + 1;
        
        return String.format("FAC-%s-%s-%03d", dateStr, busImmat, count);
    }
}