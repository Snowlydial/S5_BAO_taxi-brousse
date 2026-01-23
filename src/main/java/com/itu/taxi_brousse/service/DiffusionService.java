package com.itu.taxi_brousse.service;

import com.itu.taxi_brousse.entity.Diffusion;
import com.itu.taxi_brousse.entity.DiffusionConf;
import com.itu.taxi_brousse.repository.DiffusionConfRepository;
import com.itu.taxi_brousse.repository.DiffusionRepository;
import com.itu.taxi_brousse.repository.DiffusionPaiementRepository;
import com.itu.taxi_brousse.repository.SocieteRepository;
import com.itu.taxi_brousse.repository.BusVoyageRepository;
import com.itu.taxi_brousse.dto.BulkDiffusionRequest;
import com.itu.taxi_brousse.entity.DiffusionPaiement;
import com.itu.taxi_brousse.entity.Societe;
import com.itu.taxi_brousse.entity.BusVoyage;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

@Transactional(readOnly = true)
public class DiffusionService {
    private final DiffusionRepository diffusionRepository;
    private final DiffusionConfRepository diffusionConfRepository;
    private final DiffusionPaiementRepository diffusionPaiementRepository;
    private final SocieteRepository societeRepository;
    private final BusVoyageRepository busVoyageRepository;

    public List<Diffusion> getListDiffusion() {
        return diffusionRepository.findAll();
    }

    public List<Diffusion> getListDiffusionByIdSociete(Integer idSociete) {
        return diffusionRepository.findBySocieteId(idSociete);
    }

    public List<Diffusion> filterDiffusionByHeure(LocalTime heureMin, LocalTime heureMax, List<Diffusion> diffs) {
        if (heureMin == null && heureMax == null) return diffs;
        return diffs.stream()
                .filter(d -> {
                    LocalTime h = d.getHeureDiffusion();
                    if (h == null) return false;
                    if (heureMin != null && h.isBefore(heureMin)) return false;
                    if (heureMax != null && h.isAfter(heureMax)) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    public List<Diffusion> filterDiffusionByDate(LocalDate dateMin, LocalDate dateMax, List<Diffusion> diffs) {
        if (dateMin == null && dateMax == null) return diffs;
        return diffs.stream()
                .filter(d -> {
                    LocalDate dt = d.getDateDiffusion();
                    if (dt == null) return false;
                    if (dateMin != null && dt.isBefore(dateMin)) return false;
                    if (dateMax != null && dt.isAfter(dateMax)) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    public Double getPrixDiffusion(LocalDate date) {
        if (date == null) return 0.0;
        Optional<DiffusionConf> conf = diffusionConfRepository.findTopByDateDebutLessThanEqualAndDateFinGreaterThanEqualOrderByDateDebutDesc(date, date);
        return conf.map(DiffusionConf::getPrix).orElse(0.0);
    }

    public Double getChiffreAffaireDiffusion(List<Diffusion> filteredDiffs) {
        if (filteredDiffs == null || filteredDiffs.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Diffusion d : filteredDiffs) {
            LocalDate date = d.getDateDiffusion();
            sum += getPrixDiffusion(date);
        }
        return sum;
    }

    //?==== Alea Week 3
    @Transactional
    public List<Diffusion> createBulkDiffusions(BulkDiffusionRequest req) {
        if (req.getQuantity() == null || req.getQuantity() <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        Societe societe = societeRepository.findById(req.getSocieteId())
                .orElseThrow(() -> new IllegalArgumentException("Societe not found"));
        BusVoyage busVoyage = busVoyageRepository.findById(req.getBusVoyageId())
                .orElseThrow(() -> new IllegalArgumentException("BusVoyage not found"));

        // create N diffusions (same date/time as requested)
        List<Diffusion> created = new java.util.ArrayList<>();
        for (int i = 0; i < req.getQuantity(); i++) {
            Diffusion d = Diffusion.builder()
                    .dateDiffusion(req.getDateDiffusion())
                    .heureDiffusion(req.getHeureDiffusion())
                    .description("Bulk: " + (req.getQuantity()) + " diffusions")
                    .societe(societe)
                    .busVoyage(busVoyage)
                    .build();
            diffusionRepository.save(d);
            created.add(d);
        }

        // If payment provided, allocate using Prorata Proportionnel
        Double payment = req.getPaymentAmount();
        if (payment != null && payment > 0) {
            //*-- Calculate INITIAL total due for prorata base
            double initialTotalDue = 0.0;
            Map<Diffusion, Double> diffusionPrices = new LinkedHashMap<>();
            
            for (Diffusion d : created) {
                double price = getPrixDiffusion(d.getDateDiffusion());
                diffusionPrices.put(d, price);
                initialTotalDue += price;
            }
            
            if (payment > initialTotalDue) {
                throw new IllegalArgumentException("Payment cannot exceed total due");
            }

            //*-- Apply prorata proportionnel using INITIAL total
            for (Map.Entry<Diffusion, Double> entry : diffusionPrices.entrySet()) {
                Diffusion d = entry.getKey();
                Double price = entry.getValue();
                
                //*-- Prorata: (individual_price / initial_total) * payment
                Double proportionalPayment = (price / initialTotalDue) * payment;
                
                DiffusionPaiement p = DiffusionPaiement.builder()
                        .montantPaye(proportionalPayment)
                        .datePaiement(LocalDate.now())
                        .diffusion(d)
                        .societe(societe)
                        .build();
                diffusionPaiementRepository.save(p);
            }
        }

        return created;
    }

    public double getPaidAmountForDiffusion(Diffusion d) {
        if (d == null || d.getId() == null) return 0.0;
        return diffusionPaiementRepository.sumPaidAmountByDiffusionId(d.getId());
    }
    
    public double getRemainingForSociety(Integer societeId) {
        if (societeId == null) return 0.0;

        //?=== Single query to get total due
        double totalDue = diffusionRepository.findBySocieteId(societeId).stream()
            .mapToDouble(d -> getPrixDiffusion(d.getDateDiffusion()))
            .sum();

        //?=== Single query to get all payments (both diffusion-specific AND society-level)
        double totalPaid = diffusionPaiementRepository
            .findBySocieteId(societeId)
            .stream()
            .mapToDouble(p -> p.getMontantPaye() != null ? p.getMontantPaye() : 0.0)
            .sum();

        return totalDue - totalPaid;
    }

    //?=== Alea Week 3 Suite - Prorata Proportionnel using INITIAL total
    @Transactional
    public void applyPaymentToSociety(Integer societeId, Double amount) {
        //*-- Validation
        if (societeId == null) throw new IllegalArgumentException("Societe required");
        if (amount == null || amount <= 0) throw new IllegalArgumentException("Amount must be > 0");

        //*-- Get all diffusions for this society
        List<Diffusion> allDiffs = diffusionRepository.findBySocieteIdOrderByIdAsc(societeId);
        
        if (allDiffs.isEmpty()) {
            throw new IllegalArgumentException("Aucune diffusion trouvée pour cette société");
        }

        //*-- Calculate INITIAL total due and individual prices for prorata
        Map<Diffusion, Double> diffusionPrices = new LinkedHashMap<>();
        double initialTotalDue = 0.0;
        
        for (Diffusion d : allDiffs) {
            double price = getPrixDiffusion(d.getDateDiffusion());
            diffusionPrices.put(d, price);
            initialTotalDue += price;
        }
        
        if (initialTotalDue <= 0) {
            throw new IllegalArgumentException("Aucune diffusion à régulariser");
        }

        //*-- Check if payment exceeds remaining amount
        double totalPaid = diffusionPaiementRepository.findBySocieteId(societeId)
            .stream()
            .mapToDouble(p -> p.getMontantPaye() != null ? p.getMontantPaye() : 0.0)
            .sum();
        
        double remaining = initialTotalDue - totalPaid;
        
        if (remaining <= 0) {
            throw new IllegalArgumentException("Cette société n'a aucune diffusion à régulariser");
        }
        
        if (amount > remaining) {
            throw new IllegalArgumentException("Payment cannot exceed remaining total: " + remaining);
        }

        //*-- Fetch society once
        Societe societe = societeRepository.findById(societeId)
            .orElseThrow(() -> new IllegalArgumentException("Societe not found"));

        //*-- Apply PRORATA PROPORTIONNEL using INITIAL total (not remaining)
        for (Map.Entry<Diffusion, Double> entry : diffusionPrices.entrySet()) {
            Diffusion d = entry.getKey();
            Double price = entry.getValue();
            
            //*-- Calculate proportional payment: (individual_price / initial_total) * payment_amount
            Double proportionalPayment = (price / initialTotalDue) * amount;
            
            //*-- Create payment record
            DiffusionPaiement p = DiffusionPaiement.builder()
                .montantPaye(proportionalPayment)
                .datePaiement(LocalDate.now())
                .diffusion(d)
                .societe(societe)
                .build();
            diffusionPaiementRepository.save(p);
        }
    }
}